package com.forum.post.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 博客查询请求 DTO
 */
@Data
public class BlogQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页
     */
    private Integer current = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;

    /**
     * 用户ID（可选，按用户筛选）
     */
    private Long userId;

    /**
     * 关键词（可选，标题关键词搜索）
     */
    private String keyword;

    /**
     * 排序方式：newest(默认)/hot
     */
    private String sort = "newest";

    /**
     * 状态过滤（可选）
     */
    private Integer status;
}
