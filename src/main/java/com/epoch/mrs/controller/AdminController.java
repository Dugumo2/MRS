package com.epoch.mrs.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.aliyuncs.exceptions.ClientException;
import com.epoch.mrs.annotation.OperationLog;
import com.epoch.mrs.domain.dto.PageDTO;
import com.epoch.mrs.domain.enums.CategoryStatus;
import com.epoch.mrs.domain.enums.UserStatus;
import com.epoch.mrs.domain.po.Film;
import com.epoch.mrs.domain.po.Log;
import com.epoch.mrs.domain.po.User;
import com.epoch.mrs.domain.query.LogQuery;
import com.epoch.mrs.domain.vo.LogVo;
import com.epoch.mrs.domain.vo.Result;
import com.epoch.mrs.domain.vo.ReviewUserVo;
import com.epoch.mrs.service.ICommentService;
import com.epoch.mrs.service.ILogService;
import com.epoch.mrs.service.IMovieService;
import com.epoch.mrs.service.IUserService;
import com.epoch.mrs.utils.AliyunOSSUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    private IUserService userService;

    @Autowired
    private ILogService logService;

    @Autowired
    private IMovieService movieService;

    @Autowired
    private ICommentService commentService;

    /**
     * 获取待审核的用户列表
     *
     * @return 包含用户信息的 Result 对象
     */
    @GetMapping("/reviewList")
    public Result getReviewList() {
        if(!StpUtil.hasRole("admin")){
            return Result.fail("你没有此权限完成该操作");
        }
        // 定义最大返回记录数，例如100
        int MAX_RECORDS = 100;

        // 获取待审核的用户列表
        List<User> pendingUsers  = userService.lambdaQuery().eq(User::getStatus, UserStatus.PENDING)
                .orderByAsc(User::getApplicationTime)
                .last("LIMIT " + MAX_RECORDS)
                .list();

        // 使用 Hutool 的 BeanUtil 进行属性拷贝，将 User 转换为 ReviewUserVo
        List<ReviewUserVo> reviewUserVos = pendingUsers.stream()
                .map(user -> BeanUtil.copyProperties(user, ReviewUserVo.class))
                .collect(Collectors.toList());

        return Result.ok(reviewUserVos);
    }


    @OperationLog("审核用户")
    @PostMapping("/review")
    public Result reviewUser(@RequestParam Integer userId ,@RequestParam Integer status){
        User user = userService.lambdaQuery().eq(User::getId, userId).one();
        if(!(status.equals(1) || status.equals(2))){
            return Result.fail("请输入正确的状态");
        }
        if(status.equals(2)) {
            // 审核拒绝逻辑 - 存入 Redis 并设置 5 分钟有效期
            String redisKey = "user:reject:" + user.getId(); // Redis Key 格式
            stringRedisTemplate.opsForValue().set(redisKey, user.getId().toString(), 5, TimeUnit.MINUTES);
        }
        user.setStatus(UserStatus.of(status))
                .setReviewTime(LocalDateTime.now());
        boolean b = userService.updateById(user);
        if(b){
            log.info("用户ID为{}的用户审核失败",userId);
        }
        log.info("用户ID为{}的用户审核成功",userId);
        return Result.ok("更新用户状态成功");
    }


    @PostMapping("/logs")
    public Result getLogList(@RequestBody LogQuery logQuery) {
        if(!StpUtil.hasRole("admin")){
            return Result.fail("你没有此权限完成该操作");
        }
        PageDTO<LogVo> page = logService.queryLogsPage(logQuery);
        return Result.ok(page);
    }

    @PostMapping("/logDelete")
    public Result deleteLog(@RequestParam int logId){
        if(!StpUtil.hasRole("admin")){
            return Result.fail("你没有此权限完成该操作");
        }
        Log one = logService.lambdaQuery().eq(Log::getId, logId).one();
        logService.removeById(one);
        return Result.ok("日志删除成功");
    }


    @PostMapping("/add")
    @OperationLog("添加影片")
    public Result addFilmInfo(@RequestParam String title,
                              @RequestParam String info,
                              @RequestParam String description,
                              @RequestParam CategoryStatus category,
                              @RequestParam MultipartFile img) {
        if(!StpUtil.hasRole("admin")){
            return Result.fail("你没有此权限完成该操作");
        }

        String imgUrl;
        try {
            imgUrl = AliyunOSSUtil.uploadFile(img);
        } catch (IOException e) {
            log.error("文件上传异常");
            throw new RuntimeException(e);
        } catch (ClientException e) {
            log.error("阿里云oss客户端异常");
            throw new RuntimeException(e);
        }

        try{
            // 创建影片信息对象
            Film film = new Film();
            film.setTitle(title);
            film.setInfo(info);
            film.setDescription(description);
            film.setCategory(category);
            film.setImageUrl(imgUrl);// 设置图片链接

            // 使用IService的save方法保存影片信息
            boolean isSaved =movieService.save(film);
            if (isSaved) {
                log.info("影片添加成功");
                return Result.ok("影片添加成功");
            } else {
                log.warn("影片添加失败");
                return Result.fail("影片添加失败");
            }
        } catch (Exception e) {
            log.error("添加影片时发生错误: " + e.getMessage());
            return Result.fail("添加影片时发生错误: " + e.getMessage());
        }
    }


    @PostMapping("/delete")
    @OperationLog("删除影片")
    public Result deleteFilmInfo(@RequestParam int filmId){
        if(!StpUtil.hasRole("admin")){
            return Result.fail("你没有此权限完成该操作");
        }
        Film film = movieService.getById(filmId);
        if (film == null) {
            return Result.fail("影片信息不存在");
        }

        // 获取影片的评论列表id
        List<Integer> commentIds = commentService.getCommentIdsByFilmId(filmId);
        if (commentIds != null && !commentIds.isEmpty()) {
            // 删除所有相关的评论
            commentService.removeByIds(commentIds);
        }

        // 删除影片信息
        boolean filmDeleted = movieService.removeById(filmId);
        if (filmDeleted) {
            return Result.ok();
        } else {
            return Result.fail("删除影片失败");
        }
    }

}
