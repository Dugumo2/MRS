package com.epoch.mrs.controller;

import com.epoch.mrs.domain.dto.PageDTO;
import com.epoch.mrs.domain.po.Film;
import com.epoch.mrs.domain.query.MovieQuery;
import com.epoch.mrs.domain.vo.FilmInfoVo;
import com.epoch.mrs.domain.vo.Result;
import com.epoch.mrs.service.IMovieService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/movies")
public class MovieController {

    @Autowired
    private IMovieService movieService;

    @PostMapping("/list")
    public Result getMovieList(@RequestBody MovieQuery movieQuery) {
        PageDTO<FilmInfoVo> page = movieService.queryFilmsPage(movieQuery);
        return Result.ok(page);
    }

    @GetMapping("/{id}")
    public Result getMovieInfo(@RequestParam int id){
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
