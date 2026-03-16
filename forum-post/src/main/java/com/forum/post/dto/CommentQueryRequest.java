package com.forum.post.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 评论查询请求 DTO
 */
@Data
public class CommentQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 博客ID
     */
    private Long blogId;

    /**
     * 父评论ID，0表示查询一级评论
     */
    private Long parentId = 0L;

    /**
     * 当前页
     */
    private Integer current = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;
}
