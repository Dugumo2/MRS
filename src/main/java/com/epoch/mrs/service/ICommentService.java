package com.epoch.mrs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epoch.mrs.domain.dto.PageDTO;
import com.epoch.mrs.domain.po.Comment;
import com.epoch.mrs.domain.query.CommentQuery;
import com.epoch.mrs.domain.vo.CommentVo;
import com.epoch.mrs.domain.vo.Result;
import org.springframework.stereotype.Service;

import java.util.List;

public interface ICommentService extends IService<Comment> {
    PageDTO<CommentVo> queryCommentsPage(CommentQuery commentQuery);
    List<Integer> getCommentIdsByFilmId(int filmId);


    /**
     * 删除评论并重新计算影片的平均评分
     * @param commentId 评论ID
     * @return 操作结果
     */
    Result deleteCommentAndRecalculateAvg(int commentId);
}
