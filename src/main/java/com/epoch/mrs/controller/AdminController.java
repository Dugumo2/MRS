package com.epoch.mrs.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.epoch.mrs.domain.enums.CategoryStatus;
import com.epoch.mrs.domain.enums.UserStatus;
import com.epoch.mrs.domain.po.User;
import com.epoch.mrs.domain.vo.Result;
import com.epoch.mrs.domain.vo.ReviewUserVo;
import com.epoch.mrs.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private IUserService userService;

    /**
     * 获取待审核的用户列表
     *
     * @return 包含用户信息的 Result 对象
     */
    @GetMapping("/reviewList")
    public Result getReviewList() {
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

    @PostMapping("/review")
    public Result reviewUser(@RequestParam int status, @RequestParam int userId) {
        User user = userService.lambdaQuery().eq(User::getId, userId).one();
        user.setStatus(UserStatus.of(status));
        userService.updateById(user);
        return Result.ok();
    }

    @GetMapping("/logList")
    public Result getLogList() {
        return Result.ok();
    }


    @PostMapping("/add")
    public Result addFilmInfo(@RequestParam String title,
                              @RequestParam String info,
                              @RequestParam String description,
                              @RequestParam CategoryStatus category,
                              @RequestParam MultipartFile img) {
        if(!StpUtil.hasRole("admin")){
            return Result.fail("你没有此权限完成该操作");
        }
        return Result.ok();
    }

    @PostMapping("/delete")
    public Result deleteFilmInfo(@RequestParam int filmId){
        if(!StpUtil.hasRole("admin")){
            return Result.fail("你没有此权限完成该操作");
        }
        return Result.ok();
    }

}
