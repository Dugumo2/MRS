package com.epoch.mrs.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.epoch.mrs.domain.po.Comment;
import com.epoch.mrs.domain.po.Film;
import com.epoch.mrs.domain.vo.Result;
import com.epoch.mrs.service.ICommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private ICommentService commentService;

    /**
     * 发表评论
     *
     * @param filmId  电影ID
     * @param content 评论内容
     * @param score   评分（1-10）
     * @return 操作结果
     */
    @PostMapping("/post")
    @Transactional
    public Result postComment(@RequestParam int filmId,
                              @RequestParam String content,
                              @RequestParam BigDecimal score) {
        // 获取当前登录的用户ID
        int userId = StpUtil.getLoginIdAsInt();

        // 检查用户是否已经对该电影发表评论
        Comment existingComment = commentService.lambdaQuery()
                .eq(Comment::getUserId, userId)
                .eq(Comment::getFilmId, filmId)
                .one();
        if (existingComment != null) {
            return Result.fail("您已经对该电影发表评论，无法重复评论。");
        }

        // 创建并保存新的评论
        Comment newComment = new Comment()
                .setUserId(userId)
                .setFilmId(filmId)
                .setContent(content)
                .setScore(score)
                .setCreateTime(LocalDateTime.now());
        boolean saveSuccess = commentService.save(newComment);
        if (!saveSuccess) {
            return Result.fail("评论发表失败，请稍后再试。");
        }

        // 计算该电影的新的平均评分
        // 获取所有相关的评分
        // 获取所有相关的评分
        List<BigDecimal> scores = commentService.lambdaQuery()
                .eq(Comment::getFilmId, filmId)
                .select(Comment::getScore)
                .list()
                .stream()
                .map(Comment::getScore)
                .collect(Collectors.toList());

// 检查评分列表是否为空
        if (scores.isEmpty()) {
            // 可以设置默认评分或其他逻辑
            Db.lambdaUpdate(Film.class)
                    .eq(Film::getId, filmId)
                    .set(Film::getAvgScore, BigDecimal.ZERO)
                    .update();
        } else {
            // 计算总分
            BigDecimal total = scores.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 计算平均分，保留一位小数
            BigDecimal average = total.divide(new BigDecimal(scores.size()), 1, RoundingMode.HALF_UP);

            // 更新电影的平均评分
            Db.lambdaUpdate(Film.class)
                    .eq(Film::getId, filmId)
                    .set(Film::getAvgScore, average)
                    .update();
        }

        return Result.ok("评论发表成功。");
    }


    @PostMapping("/delete")
    public Result deleteComment(@RequestParam int commentId) {
        return Result.ok();
    }

    @GetMapping("/list/{filmId}")
    public Result getCommentByFilm(@PathVariable int filmId) {
        int loginId = StpUtil.getLoginIdAsInt();

        return Result.ok();
    }

}
