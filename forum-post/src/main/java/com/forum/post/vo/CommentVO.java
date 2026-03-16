package com.forum.post.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论视图对象
 * 用于评论列表展示
 */
@Data
public class CommentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 博客ID
     */
    private Long blogId;

    /**
     * 评论者ID
     */
    private Long userId;

    /**
     * 评论者用户名
     */
    private String username;

    /**
     * 评论者头像
     */
    private String avatar;

    /**
     * 父评论ID
     */
    private Long parentId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 当前用户是否点赞
     */
    private Boolean isLiked;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 回复数量（仅一级评论）
     */
    private Long replyCount;

    /**
     * 最新3条回复（仅一级评论）
     */
    private List<CommentVO> replies;
}
