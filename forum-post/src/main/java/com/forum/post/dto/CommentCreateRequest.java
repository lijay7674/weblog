package com.forum.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 评论创建请求 DTO
 */
@Data
public class CommentCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 博客ID
     */
    @NotNull(message = "博客ID不能为空")
    private Long blogId;

    /**
     * 父评论ID，0表示一级评论
     */
    private Long parentId = 0L;

    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容不能超过1000个字符")
    private String content;
}
