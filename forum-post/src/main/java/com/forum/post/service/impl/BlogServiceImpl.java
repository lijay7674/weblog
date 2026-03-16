package com.forum.post.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forum.common.exception.BusinessException;
import com.forum.common.result.PageResult;
import com.forum.post.config.RabbitMQConfig;
import com.forum.post.constants.PostConstants;
import com.forum.post.dto.BlogCreateRequest;
import com.forum.post.dto.BlogQueryRequest;
import com.forum.post.dto.BlogUpdateRequest;
import com.forum.post.entity.Blog;
import com.forum.post.mapper.BlogMapper;
import com.forum.post.mq.ViewEvent;
import com.forum.post.service.BlogService;
import com.forum.post.service.LikeService;
import com.forum.post.vo.BlogDetailVO;
import com.forum.post.vo.BlogListVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 博客服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {

    private final BlogMapper blogMapper;
    private final LikeService likeService;
    private final StringRedisTemplate stringRedisTemplate;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBlog(Long userId, BlogCreateRequest request) {
        Blog blog = new Blog();
        blog.setUserId(userId);
        blog.setTitle(request.getTitle());
        blog.setContent(request.getContent());
        blog.setSummary(generateSummary(request.getContent(), request.getSummary()));
        blog.setCoverImage(request.getCoverImage());
        blog.setStatus(request.getStatus());
        blog.setViewCount(0);
        blog.setLikeCount(0);
        blog.setCommentCount(0);
        blog.setDeleted(0);
        blog.setVersion(0);
        
        blogMapper.insert(blog);
        log.info("创建博客成功，userId: {}, blogId: {}", userId, blog.getId());
        return blog.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBlog(Long userId, Long blogId, BlogUpdateRequest request) {
        Blog blog = checkBlogOwner(userId, blogId);
        
        blog.setTitle(request.getTitle());
        blog.setContent(request.getContent());
        blog.setSummary(generateSummary(request.getContent(), request.getSummary()));
        blog.setCoverImage(request.getCoverImage());
        if (request.getStatus() != null) {
            blog.setStatus(request.getStatus());
        }
        
        // 删除缓存
        deleteBlogDetailCache(blogId);
        
        blogMapper.updateById(blog);
        log.info("更新博客成功，userId: {}, blogId: {}", userId, blogId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBlog(Long userId, Long blogId) {
        checkBlogOwner(userId, blogId);
        
        // 删除缓存
        deleteBlogDetailCache(blogId);
        
        // 逻辑删除
        blogMapper.deleteById(blogId);
        log.info("删除博客成功，userId: {}, blogId: {}", userId, blogId);
    }

    @Override
    public BlogDetailVO getBlogDetail(Long blogId, Long userId) {
        // 先从缓存获取
        String cacheKey = PostConstants.REDIS_KEY_BLOG_DETAIL + blogId;
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        
        // 获取博客信息
        Blog blog = blogMapper.selectById(blogId);
        if (blog == null || blog.getDeleted() == 1) {
            throw new BusinessException("博客不存在");
        }
        
        // 转换为VO
        BlogDetailVO vo = convertToDetailVO(blog);
        
        // 查询点赞状态
        if (userId != null) {
            Boolean isLiked = likeService.checkUserLiked(userId, blogId, PostConstants.TARGET_TYPE_BLOG);
            vo.setIsLiked(isLiked);
        } else {
            vo.setIsLiked(false);
        }
        
        // 增加浏览量（异步）
        incrementViewCount(blogId);
        
        return vo;
    }

    @Override
    public PageResult<BlogListVO> getBlogList(BlogQueryRequest request) {
        Page<Blog> page = new Page<>(request.getCurrent(), request.getSize());
        
        LambdaQueryWrapper<Blog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Blog::getDeleted, 0)
               .eq(Blog::getStatus, PostConstants.BLOG_STATUS_PUBLISHED);
        
        // 关键词搜索
        if (StrUtil.isNotBlank(request.getKeyword())) {
            wrapper.like(Blog::getTitle, request.getKeyword());
        }
        
        // 用户筛选
        if (request.getUserId() != null) {
            wrapper.eq(Blog::getUserId, request.getUserId());
        }
        
        // 排序
        if ("hot".equals(request.getSort())) {
            wrapper.orderByDesc(Blog::getLikeCount).orderByDesc(Blog::getCreateTime);
        } else {
            wrapper.orderByDesc(Blog::getCreateTime);
        }
        
        Page<Blog> blogPage = blogMapper.selectPage(page, wrapper);
        
        List<BlogListVO> voList = new ArrayList<>();
        for (Blog blog : blogPage.getRecords()) {
            voList.add(convertToListVO(blog));
        }
        
        return PageResult.of(
            blogPage.getTotal(),
            blogPage.getPages(),
            blogPage.getCurrent(),
            blogPage.getSize(),
            voList
        );
    }

    @Override
    public PageResult<BlogListVO> getUserBlogList(Long userId, BlogQueryRequest request) {
        Page<Blog> page = new Page<>(request.getCurrent(), request.getSize());
        
        LambdaQueryWrapper<Blog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Blog::getDeleted, 0)
               .eq(Blog::getUserId, userId);
        
        // 状态筛选
        if (request.getStatus() != null) {
            wrapper.eq(Blog::getStatus, request.getStatus());
        }
        
        wrapper.orderByDesc(Blog::getCreateTime);
        
        Page<Blog> blogPage = blogMapper.selectPage(page, wrapper);
        
        List<BlogListVO> voList = new ArrayList<>();
        for (Blog blog : blogPage.getRecords()) {
            voList.add(convertToListVO(blog));
        }
        
        return PageResult.of(
            blogPage.getTotal(),
            blogPage.getPages(),
            blogPage.getCurrent(),
            blogPage.getSize(),
            voList
        );
    }

    @Override
    public void incrementViewCount(Long blogId) {
        // Redis 浏览量计数器增加
        String countKey = PostConstants.REDIS_KEY_BLOG_VIEW_COUNT + blogId;
        stringRedisTemplate.opsForValue().increment(countKey);
        
        // 记录待同步浏览量
        String pendingKey = PostConstants.REDIS_KEY_BLOG_VIEW_PENDING;
        stringRedisTemplate.opsForHash().increment(pendingKey, blogId.toString(), 1);
        
        // 发送浏览事件（异步处理）
        ViewEvent event = new ViewEvent(blogId, null, null);
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_FORUM_POST,
            RabbitMQConfig.ROUTING_KEY_VIEW_SYNC,
            event
        );
    }

    @Override
    public void incrementLikeCount(Long blogId, Integer increment) {
        blogMapper.incrementLikeCount(blogId, increment);
    }

    @Override
    public void incrementCommentCount(Long blogId, Integer increment) {
        blogMapper.incrementCommentCount(blogId, increment);
    }

    @Override
    public Blog getById(Long blogId) {
        return blogMapper.selectById(blogId);
    }

    // ==================== 私有方法 ====================

    /**
     * 检查博客所有权
     */
    private Blog checkBlogOwner(Long userId, Long blogId) {
        Blog blog = blogMapper.selectById(blogId);
        if (blog == null || blog.getDeleted() == 1) {
            throw new BusinessException("博客不存在");
        }
        if (!blog.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此博客");
        }
        return blog;
    }

    /**
     * 生成摘要
     */
    private String generateSummary(String content, String summary) {
        if (StrUtil.isNotBlank(summary)) {
            return summary;
        }
        if (StrUtil.isBlank(content)) {
            return "";
        }
        // 去除HTML标签，截取前200个字符
        String text = content.replaceAll("<[^>]+>", "").replaceAll("\\s+", " ");
        return text.length() > 200 ? text.substring(0, 200) + "..." : text;
    }

    /**
     * 删除博客详情缓存
     */
    private void deleteBlogDetailCache(Long blogId) {
        String cacheKey = PostConstants.REDIS_KEY_BLOG_DETAIL + blogId;
        stringRedisTemplate.delete(cacheKey);
    }

    /**
     * 转换为详情VO
     */
    private BlogDetailVO convertToDetailVO(Blog blog) {
        BlogDetailVO vo = new BlogDetailVO();
        vo.setId(blog.getId());
        vo.setUserId(blog.getUserId());
        vo.setTitle(blog.getTitle());
        vo.setContent(blog.getContent());
        vo.setSummary(blog.getSummary());
        vo.setCoverImage(blog.getCoverImage());
        vo.setViewCount(blog.getViewCount());
        vo.setLikeCount(blog.getLikeCount());
        vo.setCommentCount(blog.getCommentCount());
        vo.setStatus(blog.getStatus());
        vo.setCreateTime(blog.getCreateTime());
        vo.setUpdateTime(blog.getUpdateTime());
        // username 和 avatar 需要通过用户服务获取，这里暂不设置
        return vo;
    }

    /**
     * 转换为列表VO
     */
    private BlogListVO convertToListVO(Blog blog) {
        BlogListVO vo = new BlogListVO();
        vo.setId(blog.getId());
        vo.setUserId(blog.getUserId());
        vo.setTitle(blog.getTitle());
        vo.setSummary(blog.getSummary());
        vo.setCoverImage(blog.getCoverImage());
        vo.setViewCount(blog.getViewCount());
        vo.setLikeCount(blog.getLikeCount());
        vo.setCommentCount(blog.getCommentCount());
        vo.setCreateTime(blog.getCreateTime());
        // username 和 avatar 需要通过用户服务获取，这里暂不设置
        return vo;
    }
}
