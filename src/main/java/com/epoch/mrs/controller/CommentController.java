package com.epoch.mrs.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.epoch.mrs.annotation.OperationLog;
import com.epoch.mrs.domain.dto.PageDTO;
import com.epoch.mrs.domain.po.Comment;
import com.epoch.mrs.domain.po.Film;
import com.epoch.mrs.domain.query.CommentQuery;
import com.epoch.mrs.domain.vo.CommentVo;
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
    @OperationLog("发表评论")
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



    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @return 操作结果
     */
    @PostMapping("/delete")
    @OperationLog("删除评论")
    public Result deleteComment(@RequestParam int commentId) {
        // 获取当前用户是否为管理员
        boolean isAdmin = StpUtil.hasRole("admin");

        if (!isAdmin) {
            // 如果不是管理员，检查评论是否属于当前用户
            int currentUserId = StpUtil.getLoginIdAsInt();
            Comment comment = commentService.getById(commentId);
            if (comment == null) {
                return Result.fail("评论不存在。");
            }
            if (comment.getUserId() != currentUserId) {
                return Result.fail("你没有权限删除此评论。");
            }
        }

        try {
            Result result = commentService.deleteCommentAndRecalculateAvg(commentId);
            return result;
        } catch (Exception e) {
            log.error("删除评论失败，commentId={}，错误信息={}", commentId, e.getMessage());
            return Result.fail("删除评论失败，请稍后再试。");
        }
    }

    @PostMapping("/list")
    public Result getCommentByFilm(@RequestBody CommentQuery commentQuery) {
        int uerId = StpUtil.getLoginIdAsInt();
        commentQuery.setCurrentUserId(uerId);

        PageDTO<CommentVo> page = commentService.queryCommentsPage(commentQuery);
        return Result.ok(page);
    }

}
