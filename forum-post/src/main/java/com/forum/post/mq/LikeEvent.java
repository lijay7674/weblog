package com.forum.post.mq;

import lombok.Data;

import java.io.Serializable;

/**
 * 点赞事件
 * 用于消息队列传递点赞信息
 */
@Data
public class LikeEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 操作用户ID
     */
    private Long userId;

    /**
     * 目标ID
     */
    private Long targetId;

    /**
     * 目标类型 1博客 2评论
     */
    private Integer targetType;

    /**
     * true点赞 false取消
     */
    private Boolean liked;

    /**
     * 操作时间戳
     */
    private Long timestamp;

    public LikeEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    public LikeEvent(Long userId, Long targetId, Integer targetType, Boolean liked) {
        this.userId = userId;
        this.targetId = targetId;
        this.targetType = targetType;
        this.liked = liked;
        this.timestamp = System.currentTimeMillis();
    }
}
