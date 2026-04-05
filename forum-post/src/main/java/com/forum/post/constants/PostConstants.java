package com.forum.post.constants;

/**
 * 帖子模块常量定义
 */
public class PostConstants {

    // ==================== 博客状态 ====================
    
    /**
     * 博客状态：已发布
     */
    public static final Integer BLOG_STATUS_PUBLISHED = 1;
    
    /**
     * 博客状态：草稿
     */
    public static final Integer BLOG_STATUS_DRAFT = 0;

    // ==================== 目标类型 ====================
    
    /**
     * 目标类型：博客
     */
    public static final Integer TARGET_TYPE_BLOG = 1;
    
    /**
     * 目标类型：评论
     */
    public static final Integer TARGET_TYPE_COMMENT = 2;

    // ==================== Redis Key 前缀 ====================
    
    /**
     * 用户点赞状态缓存前缀
     * 数据结构：Set
     * 完整Key：like:user:{userId}:{targetType}
     * 用途：存储用户点赞的目标ID集合
     */
    public static final String REDIS_KEY_LIKE_USER = "like:user:";
    
    /**
     * 目标点赞数缓存前缀
     * 数据结构：String
     * 完整 Key：like:count:{targetType}:{targetId}
     * 用途：存储目标的点赞计数
     */
    public static final String REDIS_KEY_LIKE_COUNT = "like:count:";
    
    /**
     * 博客浏览量缓存前缀
     * 数据结构：String
     * 完整Key：blog:view:count:{blogId}
     * 用途：存储博客浏览量计数
     */
    public static final String REDIS_KEY_BLOG_VIEW_COUNT = "blog:view:count:";

    /**
     * 待同步浏览量
     * 数据结构：Hash
     * 完整Key：blog:view:pending
     * 字段：blogId
     * 值：increment
     * 用途：存储待同步到数据库的浏览量增量
     */
    public static final String REDIS_KEY_BLOG_VIEW_PENDING = "blog:view:pending";
    
    /**
     * 博客详情缓存前缀
     * 数据结构：String (JSON)
     * 完整Key：blog:detail:{blogId}
     * 用途：缓存博客详情数据
     */
    public static final String REDIS_KEY_BLOG_DETAIL = "blog:detail:";
    
    /**
     * 分布式锁前缀
     * 完整Key：lock:{type}:{id}
     */
    public static final String REDIS_KEY_LOCK = "lock:";

    // ==================== 缓存过期时间（秒） ====================
    
    /**
     * 博客详情缓存过期时间：10分钟
     */
    public static final Long CACHE_BLOG_DETAIL_TTL = 600L;
    
    /**
     * 博客列表缓存过期时间：5分钟
     */
    public static final Long CACHE_BLOG_LIST_TTL = 300L;
    
    /**
     * 分布式锁过期时间：10秒
     */
    public static final Long LOCK_EXPIRE_TIME = 10L;

    // ==================== 同步配置 ====================
        
    /**
     * 批量同步阈值：100 条（保留用于浏览量同步）
     */
    public static final Integer SYNC_BATCH_SIZE = 100;

    // ==================== 分页默认值 ====================
    
    /**
     * 默认当前页
     */
    public static final Integer DEFAULT_CURRENT = 1;
    
    /**
     * 默认每页大小
     */
    public static final Integer DEFAULT_SIZE = 10;
    
    /**
     * 最大每页大小
     */
    public static final Integer MAX_SIZE = 100;
}
