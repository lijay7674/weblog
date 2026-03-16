package com.forum.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 博客更新请求 DTO
 */
@Data
public class BlogUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;

    /**
     * 内容
     */
    @NotBlank(message = "内容不能为空")
    private String content;

    /**
     * 摘要
     */
    @Size(max = 500, message = "摘要长度不能超过500个字符")
    private String summary;

    /**
     * 封面图
     */
    private String coverImage;

    /**
     * 状态 1发布 0草稿
     */
    private Integer status;
}
