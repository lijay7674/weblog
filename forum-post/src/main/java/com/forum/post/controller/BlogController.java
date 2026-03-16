package com.forum.post.controller;

import com.forum.common.result.PageResult;
import com.forum.common.result.Result;
import com.forum.post.dto.BlogCreateRequest;
import com.forum.post.dto.BlogQueryRequest;
import com.forum.post.dto.BlogUpdateRequest;
import com.forum.post.service.BlogService;
import com.forum.post.vo.BlogDetailVO;
import com.forum.post.vo.BlogListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 博客控制器
 */
@Tag(name = "博客管理", description = "博客相关接口")
@RestController
@RequestMapping("/api/post/blog")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    /**
     * 创建博客
     */
    @Operation(summary = "创建博客")
    @PostMapping
    public Result<Long> createBlog(
            @Parameter(description = "用户ID", required = true) @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody BlogCreateRequest request) {
        Long blogId = blogService.createBlog(userId, request);
        return Result.success(blogId);
    }

    /**
     * 更新博客
     */
    @Operation(summary = "更新博客")
    @PutMapping("/{id}")
    public Result<Void> updateBlog(
            @Parameter(description = "用户ID", required = true) @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "博客ID", required = true) @PathVariable Long id,
            @Valid @RequestBody BlogUpdateRequest request) {
        blogService.updateBlog(userId, id, request);
        return Result.success();
    }

    /**
     * 删除博客
     */
    @Operation(summary = "删除博客")
    @DeleteMapping("/{id}")
    public Result<Void> deleteBlog(
            @Parameter(description = "用户ID", required = true) @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "博客ID", required = true) @PathVariable Long id) {
        blogService.deleteBlog(userId, id);
        return Result.success();
    }

    /**
     * 获取博客详情
     */
    @Operation(summary = "获取博客详情")
    @GetMapping("/{id}")
    public Result<BlogDetailVO> getBlogDetail(
            @Parameter(description = "博客ID", required = true) @PathVariable Long id,
            @Parameter(description = "用户ID") @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        BlogDetailVO detail = blogService.getBlogDetail(id, userId);
        return Result.success(detail);
    }

    /**
     * 获取博客列表
     */
    @Operation(summary = "获取博客列表")
    @GetMapping("/list")
    public Result<PageResult<BlogListVO>> getBlogList(BlogQueryRequest request) {
        PageResult<BlogListVO> result = blogService.getBlogList(request);
        return Result.success(result);
    }

    /**
     * 获取用户的博客列表
     */
    @Operation(summary = "获取用户的博客列表")
    @GetMapping("/user/{userId}")
    public Result<PageResult<BlogListVO>> getUserBlogList(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            BlogQueryRequest request) {
        PageResult<BlogListVO> result = blogService.getUserBlogList(userId, request);
        return Result.success(result);
    }
}
