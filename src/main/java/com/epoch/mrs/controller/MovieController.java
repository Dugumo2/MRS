package com.epoch.mrs.controller;

import com.epoch.mrs.domain.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/movies")
public class MovieController {

    @GetMapping("/list")
    public Result getMovieList() {
        return Result.ok();
    }

    @GetMapping("/{id}")
    public Result getMovieInfo(@RequestParam Integer id){
        return Result.ok();
    }

    @PostMapping("/search")
    public Result searchMovie(@RequestParam String fileName) {
        return Result.ok();
    }

    @GetMapping("/rankList")
    public Result getRankList(@RequestParam String type) {
        return Result.ok();
    }
}
