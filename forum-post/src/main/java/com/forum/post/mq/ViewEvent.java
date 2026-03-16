package com.forum.post.mq;

import lombok.Data;

import java.io.Serializable;

/**
 * 浏览量事件
 * 用于消息队列传递浏览量信息
 */
@Data
public class ViewEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 博客ID
     */
    private Long blogId;

    /**
     * 浏览者ID（未登录为null）
     */
    private Long userId;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 浏览时间戳
     */
    private Long timestamp;

    public ViewEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    public ViewEvent(Long blogId, Long userId, String ip) {
        this.blogId = blogId;
        this.userId = userId;
        this.ip = ip;
        this.timestamp = System.currentTimeMillis();
    }
}
