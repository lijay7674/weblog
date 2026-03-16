package com.forum.post.mq;

import lombok.Data;

import java.io.Serializable;

/**
 * 评论事件
 * 用于消息队列传递评论信息
 */
@Data
public class CommentEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    private Long commentId;

    /**
     * 博客ID
     */
    private Long blogId;

    /**
     * 评论者ID
     */
    private Long userId;

    /**
     * 父评论ID（0表示一级评论）
     */
    private Long parentId;

    /**
     * 被回复者ID（通知用）
     */
    private Long toUserId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论时间戳
     */
    private Long timestamp;

    public CommentEvent() {
        this.timestamp = System.currentTimeMillis();
    }
}
