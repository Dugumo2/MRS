package com.epoch.mrs.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epoch.mrs.domain.dto.PageDTO;
import com.epoch.mrs.domain.po.Comment;
import com.epoch.mrs.domain.query.CommentQuery;
import com.epoch.mrs.domain.vo.CommentVo;
import com.epoch.mrs.mapper.CommentMapper;
import com.epoch.mrs.service.ICommentService;
import org.springframework.beans.factory.annotation.Autowired;

public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Override
    public PageDTO<CommentVo> queryCommentsPage(CommentQuery commentQuery) {
        // 创建分页对象，根据前端传递的排序参数
        Page<CommentVo> page = commentQuery.toMpPage(
                new OrderItem().setAsc(commentQuery.getIsAsc()).setColumn(commentQuery.getSortBy())
        );

        // 调用自定义的 Mapper 方法进行分页查询
        IPage<CommentVo> commentVoPage = commentMapper.selectCommentVoPage(page, commentQuery.getFilmId());

        // 将 MyBatis-Plus 的 Page 转换为自定义的 PageDTO
        return PageDTO.from(commentVoPage);
    }
}
