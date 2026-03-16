package com.forum.post.service;

import com.forum.common.result.PageResult;
import com.forum.post.dto.CommentCreateRequest;
import com.forum.post.dto.CommentQueryRequest;
import com.forum.post.entity.Comment;
import com.forum.post.vo.CommentVO;

/**
 * 评论服务接口
 */
public interface CommentService {

    /**
     * 创建评论
     * @param userId 用户ID
     * @param username 用户名
     * @param request 创建请求
     * @return 评论ID
     */
    Long createComment(Long userId, String username, CommentCreateRequest request);

    /**
     * 删除评论
     * @param userId 用户ID
     * @param commentId 评论ID
     */
    void deleteComment(Long userId, Long commentId);

    /**
     * 获取评论列表
     * @param request 查询请求
     * @param currentUserId 当前用户ID（可为null）
     * @return 分页结果
     */
    PageResult<CommentVO> getCommentList(CommentQueryRequest request, Long currentUserId);

    /**
     * 获取评论的回复列表
     * @param parentId 父评论ID
     * @param current 当前页
     * @param size 每页大小
     * @param currentUserId 当前用户ID
     * @return 分页结果
     */
    PageResult<CommentVO> getReplyList(Long parentId, Integer current, Integer size, Long currentUserId);

    /**
     * 增加点赞数
     * @param commentId 评论ID
     * @param increment 增量
     */
    void incrementLikeCount(Long commentId, Integer increment);

    /**
     * 根据ID获取评论实体
     * @param commentId 评论ID
     * @return 评论实体
     */
    Comment getById(Long commentId);
}
