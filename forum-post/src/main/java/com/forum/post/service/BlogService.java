package com.forum.post.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forum.common.result.PageResult;
import com.forum.post.dto.BlogCreateRequest;
import com.forum.post.dto.BlogQueryRequest;
import com.forum.post.dto.BlogUpdateRequest;
import com.forum.post.entity.Blog;
import com.forum.post.vo.BlogDetailVO;
import com.forum.post.vo.BlogListVO;

/**
 * 博客服务接口
 */
public interface BlogService {

    /**
     * 创建博客
     * @param userId 用户ID
     * @param request 创建请求
     * @return 博客ID
     */
    Long createBlog(Long userId, BlogCreateRequest request);

    /**
     * 更新博客
     * @param userId 用户ID
     * @param blogId 博客ID
     * @param request 更新请求
     */
    void updateBlog(Long userId, Long blogId, BlogUpdateRequest request);

    /**
     * 删除博客
     * @param userId 用户ID
     * @param blogId 博客ID
     */
    void deleteBlog(Long userId, Long blogId);

    /**
     * 获取博客详情
     * @param blogId 博客ID
     * @param userId 当前用户ID（可为null）
     * @return 博客详情
     */
    BlogDetailVO getBlogDetail(Long blogId, Long userId);

    /**
     * 获取博客列表
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<BlogListVO> getBlogList(BlogQueryRequest request);

    /**
     * 获取用户的博客列表
     * @param userId 用户ID
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<BlogListVO> getUserBlogList(Long userId, BlogQueryRequest request);

    /**
     * 增加浏览量
     * @param blogId 博客ID
     */
    void incrementViewCount(Long blogId);

    /**
     * 增加点赞数
     * @param blogId 博客ID
     * @param increment 增量
     */
    void incrementLikeCount(Long blogId, Integer increment);

    /**
     * 增加评论数
     * @param blogId 博客ID
     * @param increment 增量
     */
    void incrementCommentCount(Long blogId, Integer increment);

    /**
     * 根据ID获取博客实体
     * @param blogId 博客ID
     * @return 博客实体
     */
    Blog getById(Long blogId);
}
