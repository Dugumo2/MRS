package com.epoch.mrs.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.epoch.mrs.domain.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/comments")
public class CommentController {

    @PostMapping("/post")
    public Result postComment(@RequestParam int filmId) {
        return Result.ok();
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
