package com.epoch.mrs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epoch.mrs.domain.dto.PageDTO;
import com.epoch.mrs.domain.po.Film;
import com.epoch.mrs.domain.query.MovieQuery;
import com.epoch.mrs.domain.vo.FilmInfoVo;
import com.epoch.mrs.mapper.MovieMapper;
import com.epoch.mrs.service.IMovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class MovieServiceImpl extends ServiceImpl<MovieMapper, Film> implements IMovieService {
    @Autowired
    private MovieMapper movieMapper;

    @Override
    public PageDTO<FilmInfoVo> queryFilmsPage(MovieQuery movieQuery) {
        // 创建分页对象，根据前端传递的排序参数
        Page<FilmInfoVo> page = movieQuery.toMpPage();

        // 调用自定义的 Mapper 方法进行分页查询
        IPage<FilmInfoVo> filmVoPage = movieMapper.selectFilmListVoPage(page, movieQuery.getType());

        // 将 MyBatis-Plus 的 Page 转换为自定义的 PageDTO
        return PageDTO.from(filmVoPage);
    }

    @Override
    public boolean updateAvgScore(int filmId, BigDecimal newAvgScore) {
        LambdaQueryWrapper<Film> updateWrapper = new LambdaQueryWrapper<>();
        updateWrapper.eq(Film::getId, filmId);
        Film film = new Film();
        film.setAvgScore(newAvgScore);
        return this.update(film, updateWrapper);
    }
}
