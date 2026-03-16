package com.forum.post.job;

import com.forum.post.constants.PostConstants;
import com.forum.post.entity.Blog;
import com.forum.post.mapper.BlogMapper;
import com.forum.post.mapper.LikeRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据一致性检查任务
 * 每日凌晨检查并修复数据不一致
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataConsistencyCheckJob {

    private final StringRedisTemplate stringRedisTemplate;
    private final LikeRecordMapper likeRecordMapper;
    private final BlogMapper blogMapper;

    /**
     * 每天凌晨3点执行点赞数据一致性检查
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void checkAndFixLikeCount() {
        log.info("开始执行点赞数据一致性检查");
        
        try {
            // 获取所有博客
            List<Blog> blogs = blogMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Blog>()
                    .eq(Blog::getDeleted, 0)
            );
            
            int fixedCount = 0;
            
            for (Blog blog : blogs) {
                String countKey = PostConstants.REDIS_KEY_LIKE_COUNT + 
                        PostConstants.TARGET_TYPE_BLOG + ":" + blog.getId();
                String redisCount = stringRedisTemplate.opsForValue().get(countKey);
                
                // 统计数据库中的点赞数
                Long dbCount = likeRecordMapper.countByTarget(blog.getId(), PostConstants.TARGET_TYPE_BLOG);
                
                // 比较并修复
                if (redisCount != null) {
                    long redisCountVal = Long.parseLong(redisCount);
                    if (redisCountVal != dbCount) {
                        log.warn("博客{}点赞数不一致，Redis：{}，数据库：{}", 
                            blog.getId(), redisCountVal, dbCount);
                        // 以数据库为准，更新Redis
                        stringRedisTemplate.opsForValue().set(countKey, dbCount.toString());
                        fixedCount++;
                    }
                } else {
                    // Redis中不存在，写入
                    stringRedisTemplate.opsForValue().set(countKey, dbCount.toString());
                }
            }
            
            log.info("点赞数据一致性检查完成，修复数量: {}", fixedCount);
        } catch (Exception e) {
            log.error("点赞数据一致性检查失败", e);
        }
    }

    /**
     * 每小时同步浏览量到数据库
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void syncViewCountToDatabase() {
        log.info("开始同步浏览量到数据库");
        
        try {
            String pendingKey = PostConstants.REDIS_KEY_BLOG_VIEW_PENDING;
            var pendingData = stringRedisTemplate.opsForHash().entries(pendingKey);
            
            if (pendingData.isEmpty()) {
                log.info("没有待同步的浏览量数据");
                return;
            }
            
            int syncCount = 0;
            
            for (var entry : pendingData.entrySet()) {
                Long blogId = Long.valueOf((String) entry.getKey());
                Integer increment = Integer.valueOf((String) entry.getValue());
                
                // 更新数据库
                blogMapper.incrementViewCount(blogId, increment);
                
                // 删除已同步的数据
                stringRedisTemplate.opsForHash().delete(pendingKey, entry.getKey().toString());
                syncCount++;
            }
            
            log.info("浏览量同步完成，同步数量: {}", syncCount);
        } catch (Exception e) {
            log.error("浏览量同步失败", e);
        }
    }
}
