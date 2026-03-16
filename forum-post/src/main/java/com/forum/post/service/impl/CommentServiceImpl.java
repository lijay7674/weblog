package com.forum.post.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forum.common.exception.BusinessException;
import com.forum.common.result.PageResult;
import com.forum.post.config.RabbitMQConfig;
import com.forum.post.constants.PostConstants;
import com.forum.post.dto.CommentCreateRequest;
import com.forum.post.dto.CommentQueryRequest;
import com.forum.post.entity.Blog;
import com.forum.post.entity.Comment;
import com.forum.post.mapper.BlogMapper;
import com.forum.post.mapper.CommentMapper;
import com.forum.post.mq.CommentEvent;
import com.forum.post.service.BlogService;
import com.forum.post.service.CommentService;
import com.forum.post.service.LikeService;
import com.forum.post.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 评论服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final BlogMapper blogMapper;
    private final BlogService blogService;
    private final LikeService likeService;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createComment(Long userId, String username, CommentCreateRequest request) {
        // 检查博客是否存在
        Blog blog = blogMapper.selectById(request.getBlogId());
        if (blog == null || blog.getDeleted() == 1) {
            throw new BusinessException("博客不存在");
        }

        Long toUserId = null;
        
        // 如果是回复评论
        if (request.getParentId() != null && request.getParentId() > 0) {
            Comment parentComment = commentMapper.selectById(request.getParentId());
            if (parentComment == null || parentComment.getDeleted() == 1) {
                throw new BusinessException("父评论不存在");
            }
            toUserId = parentComment.getUserId();
        }

        // 创建评论
        Comment comment = new Comment();
        comment.setBlogId(request.getBlogId());
        comment.setUserId(userId);
        comment.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        comment.setContent(request.getContent());
        comment.setLikeCount(0);
        comment.setDeleted(0);

        commentMapper.insert(comment);

        // 更新博客评论数
        blogService.incrementCommentCount(request.getBlogId(), 1);

        // 发送评论通知事件
        CommentEvent event = new CommentEvent();
        event.setCommentId(comment.getId());
        event.setBlogId(request.getBlogId());
        event.setUserId(userId);
        event.setParentId(comment.getParentId());
        event.setToUserId(toUserId);
        event.setContent(request.getContent());

        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_FORUM_POST,
            RabbitMQConfig.ROUTING_KEY_COMMENT_NEW,
            event
        );

        log.info("创建评论成功，userId: {}, commentId: {}, blogId: {}", userId, comment.getId(), request.getBlogId());
        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getDeleted() == 1) {
            throw new BusinessException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此评论");
        }

        // 逻辑删除
        commentMapper.deleteById(commentId);

        // 更新博客评论数
        blogService.incrementCommentCount(comment.getBlogId(), -1);

        log.info("删除评论成功，userId: {}, commentId: {}", userId, commentId);
    }

    @Override
    public PageResult<CommentVO> getCommentList(CommentQueryRequest request, Long currentUserId) {
        Page<Comment> page = new Page<>(request.getCurrent(), request.getSize());

        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getDeleted, 0)
               .eq(Comment::getBlogId, request.getBlogId())
               .eq(Comment::getParentId, request.getParentId() != null ? request.getParentId() : 0L)
               .orderByDesc(Comment::getCreateTime);

        Page<Comment> commentPage = commentMapper.selectPage(page, wrapper);

        List<CommentVO> voList = new ArrayList<>();
        List<Long> commentIds = new ArrayList<>();
        
        for (Comment comment : commentPage.getRecords()) {
            commentIds.add(comment.getId());
            voList.add(convertToVO(comment));
        }

        // 批量查询点赞状态
        if (currentUserId != null && !commentIds.isEmpty()) {
            Map<Long, Boolean> likedMap = likeService.batchCheckUserLiked(
                currentUserId, commentIds, PostConstants.TARGET_TYPE_COMMENT);
            
            for (CommentVO vo : voList) {
                vo.setIsLiked(likedMap.getOrDefault(vo.getId(), false));
            }
        }

        // 为一级评论添加回复数和最新回复
        if (request.getParentId() == null || request.getParentId() == 0) {
            for (CommentVO vo : voList) {
                Long replyCount = commentMapper.countByParentId(vo.getId());
                vo.setReplyCount(replyCount);
                
                // 获取最新3条回复
                if (replyCount > 0) {
                    List<Comment> replies = getLatestReplies(vo.getId(), 3);
                    List<CommentVO> replyVOs = new ArrayList<>();
                    for (Comment reply : replies) {
                        replyVOs.add(convertToVO(reply));
                    }
                    vo.setReplies(replyVOs);
                }
            }
        }

        return PageResult.of(
            commentPage.getTotal(),
            commentPage.getPages(),
            commentPage.getCurrent(),
            commentPage.getSize(),
            voList
        );
    }

    @Override
    public PageResult<CommentVO> getReplyList(Long parentId, Integer current, Integer size, Long currentUserId) {
        Page<Comment> page = new Page<>(current, size);

        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getDeleted, 0)
               .eq(Comment::getParentId, parentId)
               .orderByDesc(Comment::getCreateTime);

        Page<Comment> commentPage = commentMapper.selectPage(page, wrapper);

        List<CommentVO> voList = new ArrayList<>();
        List<Long> commentIds = new ArrayList<>();
        
        for (Comment comment : commentPage.getRecords()) {
            commentIds.add(comment.getId());
            voList.add(convertToVO(comment));
        }

        // 批量查询点赞状态
        if (currentUserId != null && !commentIds.isEmpty()) {
            Map<Long, Boolean> likedMap = likeService.batchCheckUserLiked(
                currentUserId, commentIds, PostConstants.TARGET_TYPE_COMMENT);
            
            for (CommentVO vo : voList) {
                vo.setIsLiked(likedMap.getOrDefault(vo.getId(), false));
            }
        }

        return PageResult.of(
            commentPage.getTotal(),
            commentPage.getPages(),
            commentPage.getCurrent(),
            commentPage.getSize(),
            voList
        );
    }

    @Override
    public void incrementLikeCount(Long commentId, Integer increment) {
        commentMapper.incrementLikeCount(commentId, increment);
    }

    @Override
    public Comment getById(Long commentId) {
        return commentMapper.selectById(commentId);
    }

    // ==================== 私有方法 ====================

    /**
     * 获取最新回复
     */
    private List<Comment> getLatestReplies(Long parentId, int limit) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getDeleted, 0)
               .eq(Comment::getParentId, parentId)
               .orderByDesc(Comment::getCreateTime)
               .last("LIMIT " + limit);
        return commentMapper.selectList(wrapper);
    }

    /**
     * 转换为VO
     */
    private CommentVO convertToVO(Comment comment) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setBlogId(comment.getBlogId());
        vo.setUserId(comment.getUserId());
        vo.setParentId(comment.getParentId());
        vo.setContent(comment.getContent());
        vo.setLikeCount(comment.getLikeCount());
        vo.setCreateTime(comment.getCreateTime());
        vo.setIsLiked(false);
        // username 和 avatar 需要通过用户服务获取，这里暂不设置
        return vo;
    }
}
