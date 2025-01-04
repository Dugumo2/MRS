package com.epoch.mrs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.epoch.mrs.domain.po.Film;
import com.epoch.mrs.domain.vo.FilmInfoVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MovieMapper extends BaseMapper<Film> {
    /**
     * 分页查询电影信息
     *
     * @param page 分页对象
     * @param type 电影类型，可选
     * @return 分页的 FilmInfoVo 列表
     */
    IPage<FilmInfoVo> selectFilmListVoPage(IPage<FilmInfoVo> page, @Param("type") String type);
}
