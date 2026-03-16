package com.forum.post.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 博客列表视图对象
 * 用于博客列表展示
 */
@Data
public class BlogListVO implements Serializable {

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
     * 创建时间
     */
    private LocalDateTime createTime;
}
