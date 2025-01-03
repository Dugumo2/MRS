package com.epoch.mrs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epoch.mrs.domain.dto.PageDTO;
import com.epoch.mrs.domain.po.Film;
import com.epoch.mrs.domain.query.MovieQuery;
import com.epoch.mrs.domain.vo.FilmInfoVo;
import org.springframework.stereotype.Service;

public interface IMovieService extends IService<Film> {
    PageDTO<FilmInfoVo> queryFilmsPage(MovieQuery movieQuery);
}
