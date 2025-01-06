package com.epoch.mrs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epoch.mrs.domain.dto.PageDTO;
import com.epoch.mrs.domain.po.Film;
import com.epoch.mrs.domain.query.MovieQuery;
import com.epoch.mrs.domain.vo.FilmInfoVo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

public interface IMovieService extends IService<Film> {
    PageDTO<FilmInfoVo> queryFilmsPage(MovieQuery movieQuery);

    /**
     * 更新影片的平均评分
     * @param filmId 影片ID
     * @param newAvgScore 新的平均评分
     * @return 是否更新成功
     */
    boolean updateAvgScore(int filmId, BigDecimal newAvgScore);
}
