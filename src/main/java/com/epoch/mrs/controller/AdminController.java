package com.epoch.mrs.controller;

import com.epoch.mrs.domain.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/reviewList")
    public Result getReviewList() {
        return Result.ok();
    }

    @PostMapping("/")
    public Result reviewUser() {
        return Result.ok();
    }

    @GetMapping("/logList")
    public Result getLogList() {
        return Result.ok();
    }

    @PostMapping()
    public Result addFilmInfo() {
        return Result.ok();
    }

    @PostMapping()
    public Result deleteFilmInfo(){
        return Result.ok();
    }

}
