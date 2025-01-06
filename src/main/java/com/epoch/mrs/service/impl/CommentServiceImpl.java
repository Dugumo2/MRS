package com.epoch.mrs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epoch.mrs.domain.dto.PageDTO;
import com.epoch.mrs.domain.po.Comment;
import com.epoch.mrs.domain.query.CommentQuery;
import com.epoch.mrs.domain.vo.CommentVo;
import com.epoch.mrs.domain.vo.Result;
import com.epoch.mrs.mapper.CommentMapper;
import com.epoch.mrs.service.ICommentService;
import com.epoch.mrs.service.IMovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private IMovieService movieService;

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

    @Override
    public List<Integer> getCommentIdsByFilmId(int filmId) {
        return commentMapper.getCommentIdsByFilmId(filmId);
    }

    /**
     * 删除评论并重新计算影片的平均评分
     */
    @Override
    @Transactional
    public Result deleteCommentAndRecalculateAvg(int commentId) {
        // 1. 获取评论
        Comment comment = this.getById(commentId);
        if (comment == null) {
            return Result.fail("评论不存在。");
        }

        int filmId = comment.getFilmId();

        // 2. 删除评论
        boolean removed = this.removeById(commentId);
        if (!removed) {
            return Result.fail("删除评论失败。");
        }

        // 3. 重新计算影片的平均评分
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getFilmId, filmId);
        List<Comment> remainingComments = this.list(queryWrapper);

        BigDecimal newAvgScore;
        if (remainingComments.isEmpty()) {
            newAvgScore = BigDecimal.ZERO;
        } else {
            BigDecimal total = remainingComments.stream()
                    .map(Comment::getScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            newAvgScore = total.divide(new BigDecimal(remainingComments.size()), 1, RoundingMode.HALF_UP);
        }

        // 4. 更新影片的平均评分
        boolean filmUpdated = movieService.updateAvgScore(filmId, newAvgScore);
        if (!filmUpdated) {
            // 如果更新影片评分失败，可以选择抛出异常以回滚事务
            throw new RuntimeException("更新影片的平均评分失败。");
        }

        return Result.ok("删除评论成功，并重新计算平均评分。");
    }
}
