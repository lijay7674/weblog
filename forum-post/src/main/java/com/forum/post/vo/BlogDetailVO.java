package com.forum.post.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 博客详情视图对象
 * 用于博客详情展示
 */
@Data
public class BlogDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 博客ID
     */
    private Long id;

    /**
     * 作者ID
     */
    private Long userId;

    /**
     * 作者用户名
     */
    private String username;

    /**
     * 作者头像
     */
    private String avatar;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 摘要
     */
    private String summary;

    /**
     * 封面图
     */
    private String coverImage;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 当前用户是否点赞
     */
    private Boolean isLiked;

    /**
     * 状态 1发布 0草稿
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
