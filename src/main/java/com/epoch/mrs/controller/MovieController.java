package com.epoch.mrs.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epoch.mrs.annotation.OperationLog;
import com.epoch.mrs.domain.dto.PageDTO;
import com.epoch.mrs.domain.enums.CategoryStatus;
import com.epoch.mrs.domain.po.Film;
import com.epoch.mrs.domain.query.MovieQuery;
import com.epoch.mrs.domain.vo.FilmInfoVo;
import com.epoch.mrs.domain.vo.FilmListVo;
import com.epoch.mrs.domain.vo.Result;
import com.epoch.mrs.service.IMovieService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping("/getInfo")
    @OperationLog("查看电影详情")
    public Result getMovieInfo(@RequestParam int id){
        try {
            Film film = movieService.getById(id);
            if (film == null) {
                return Result.fail("影片不存在。");
            }

            // 使用 Hutool 的 BeanUtil 复制属性到 FilmInfoVo
            FilmInfoVo filmInfoVo = BeanUtil.copyProperties(film, FilmInfoVo.class);

            return Result.ok(filmInfoVo);
        } catch (Exception e) {
            log.error("获取影片信息失败，id={}，错误信息={}", id, e.getMessage());
            return Result.fail("获取影片信息失败，请稍后再试。");
        }
    }


    /**
     * 搜索影片
     *
     * @param fileName 搜索关键字（影片标题的一部分）
     * @return 操作结果
     */
    @PostMapping("/search")
    @OperationLog("搜索电影")
    public Result searchMovie(@RequestParam String fileName) {
        try {
            // 使用 Hutool 的 StrUtil 进行字符串判断
            if (cn.hutool.core.util.StrUtil.isBlank(fileName)) {
                return Result.fail("搜索关键字不能为空。");
            }

            // 使用 MyBatis Plus 的 LambdaQueryWrapper 进行模糊查询
            LambdaQueryWrapper<Film> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.like(Film::getTitle, fileName);

            // 执行查询
            List<Film> films = movieService.list(queryWrapper);

            if (films.isEmpty()) {
                return Result.ok("没有找到匹配的影片。");
            }

            // 将 Film 转换为 FilmListVo
            List<FilmListVo> filmListVos = films.stream()
                    .map(film -> BeanUtil.copyProperties(film, FilmListVo.class))
                    .collect(Collectors.toList());

            return Result.ok(filmListVos);
        } catch (Exception e) {
            log.error("搜索影片失败，fileName={}，错误信息={}", fileName, e.getMessage());
            return Result.fail("搜索影片失败，请稍后再试。");
        }
    }

    /**
     * 获取影片排名列表
     *
     * @param type 影片类型，可选（如 "Action", "Comedy", "Romance", "Anime"）
     * @return 操作结果，包含 FilmListVo 列表
     */
    @GetMapping("/rankList")
    public Result getRankList(@RequestParam(required = false) String type) {
        try {
            // 创建查询条件
            LambdaQueryWrapper<Film> queryWrapper = new LambdaQueryWrapper<>();

            if (StrUtil.isNotBlank(type)) {
                // 尝试将 type 转换为 CategoryStatus 枚举
                CategoryStatus category;
                try {
                    category = CategoryStatus.fromValue(type);
                } catch (IllegalArgumentException e) {
                    return Result.fail("未知的类型: " + type);
                }
                // 添加条件：category 等于指定类型
                queryWrapper.eq(Film::getCategory, category);
            }

            // 按 avgScore 降序排列
            queryWrapper.orderByDesc(Film::getAvgScore);

            // 设置分页参数：当前页1，页面大小15
            Page<Film> page = new Page<>(1, 15);

            // 执行分页查询
            IPage<Film> filmPage = movieService.page(page, queryWrapper);

            List<Film> films = filmPage.getRecords();

            // 转换为 FilmListVo
            List<FilmListVo> filmListVos = films.stream()
                    .map(film -> BeanUtil.copyProperties(film, FilmListVo.class))
                    .collect(Collectors.toList());

            return Result.ok(filmListVos);
        } catch (Exception e) {
            log.error("获取排名列表失败，type={}，错误信息={}", type, e.getMessage());
            return Result.fail("获取排名列表失败，请稍后再试。");
        }
    }
}
