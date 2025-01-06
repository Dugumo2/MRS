package com.epoch.mrs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epoch.mrs.domain.po.Comment;
import com.epoch.mrs.domain.vo.CommentVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    /**
     * 分页查询评论，并关联用户以获取用户名
     *
     * @param page    分页对象
     * @param filmId  电影 ID 作为过滤条件
     * @return 分页的 CommentVo 列表
     */
    IPage<CommentVo> selectCommentVoPage(@Param("page") Page<?> page, @Param("filmId") Integer filmId);

    List<Integer> getCommentIdsByFilmId(@Param("filmId") int filmId);


}
