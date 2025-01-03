package com.epoch.mrs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epoch.mrs.domain.dto.PageDTO;
import com.epoch.mrs.domain.po.Comment;
import com.epoch.mrs.domain.query.CommentQuery;
import com.epoch.mrs.domain.vo.CommentVo;
import org.springframework.stereotype.Service;

public interface ICommentService extends IService<Comment> {
    PageDTO<CommentVo> queryCommentsPage(CommentQuery commentQuery);
}
