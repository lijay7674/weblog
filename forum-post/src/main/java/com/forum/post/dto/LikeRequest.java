package com.forum.post.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 点赞请求 DTO
 */
@Data
public class LikeRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 目标ID
     */
    @NotNull(message = "目标ID不能为空")
    private Long targetId;

    /**
     * 目标类型 1博客 2评论
     */
    @NotNull(message = "目标类型不能为空")
    private Integer targetType;

    /**
     * true点赞 false取消
     */
    @NotNull(message = "点赞状态不能为空")
    private Boolean liked;
}
