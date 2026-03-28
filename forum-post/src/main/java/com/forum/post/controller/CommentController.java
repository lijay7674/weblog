package com.forum.post.controller;

import com.forum.common.result.PageResult;
import com.forum.common.result.Result;
import com.forum.post.dto.CommentCreateRequest;
import com.forum.post.dto.CommentQueryRequest;
import com.forum.post.service.CommentService;
import com.forum.post.vo.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 评论控制器
 */
@Tag(name = "评论管理", description = "评论相关接口")
@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 创建评论
     */
    @Operation(summary = "创建评论")
    @PostMapping
    public Result<Long> createComment(
            @Parameter(description = "用户ID", required = true) @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "用户名") @RequestHeader(value = "X-User-Name", required = false) String username,
            @Valid @RequestBody CommentCreateRequest request) {
        Long commentId = commentService.createComment(userId, username, request);
        return Result.success(commentId);
    }

    /**
     * 删除评论
     */
    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(
            @Parameter(description = "用户ID", required = true) @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "评论ID", required = true) @PathVariable Long id) {
        commentService.deleteComment(userId, id);
        return Result.success();
    }

    /**
     * 获取评论列表
     */
    @Operation(summary = "获取评论列表")
    @GetMapping("/list")
    public Result<PageResult<CommentVO>> getCommentList(
            CommentQueryRequest request,
            @Parameter(description = "用户ID") @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        PageResult<CommentVO> result = commentService.getCommentList(request, userId);
        return Result.success(result);
    }

    /**
     * 获取评论的回复列表
     */
    @Operation(summary = "获取评论的回复列表")
    @GetMapping("/reply/{parentId}")
    public Result<PageResult<CommentVO>> getReplyList(
            @Parameter(description = "父评论ID", required = true) @PathVariable Long parentId,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "用户ID") @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        PageResult<CommentVO> result = commentService.getReplyList(parentId, current, size, userId);
        return Result.success(result);
    }
}
