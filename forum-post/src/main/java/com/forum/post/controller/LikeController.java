package com.forum.post.controller;

import com.forum.common.result.Result;
import com.forum.post.dto.LikeRequest;
import com.forum.post.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 点赞控制器
 */
@Tag(name = "点赞管理", description = "点赞相关接口")
@RestController
@RequestMapping("/api/post/like")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    /**
     * 点赞/取消点赞
     */
    @Operation(summary = "点赞/取消点赞")
    @PostMapping
    public Result<Boolean> like(
            @Parameter(description = "用户ID", required = true) @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody LikeRequest request) {
        Boolean result = likeService.like(userId, request.getTargetId(), request.getTargetType(), request.getLiked());
        return Result.success(result);
    }

    /**
     * 批量查询点赞状态
     */
    @Operation(summary = "批量查询点赞状态")
    @GetMapping("/status")
    public Result<Map<Long, Boolean>> batchCheckStatus(
            @Parameter(description = "用户ID", required = true) @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "目标ID列表（逗号分隔）", required = true) @RequestParam String targetIds,
            @Parameter(description = "目标类型 1博客 2评论", required = true) @RequestParam Integer targetType) {
        
        List<Long> targetIdList = Arrays.stream(targetIds.split(","))
                .map(String::trim)
                .map(Long::valueOf)
                .collect(Collectors.toList());
        
        Map<Long, Boolean> result = likeService.batchCheckUserLiked(userId, targetIdList, targetType);
        return Result.success(result);
    }

    /**
     * 获取目标的点赞数
     */
    @Operation(summary = "获取目标的点赞数")
    @GetMapping("/count")
    public Result<Long> getLikeCount(
            @Parameter(description = "目标ID", required = true) @RequestParam Long targetId,
            @Parameter(description = "目标类型 1博客 2评论", required = true) @RequestParam Integer targetType) {
        Long count = likeService.getLikeCount(targetId, targetType);
        return Result.success(count);
    }
}
