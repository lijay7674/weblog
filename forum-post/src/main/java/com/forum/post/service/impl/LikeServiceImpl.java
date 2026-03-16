package com.forum.post.service.impl;

import com.forum.post.constants.PostConstants;
import com.forum.post.entity.LikeRecord;
import com.forum.post.mapper.BlogMapper;
import com.forum.post.mapper.CommentMapper;
import com.forum.post.mapper.LikeRecordMapper;
import com.forum.post.mq.LikeEvent;
import com.forum.post.service.LikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 点赞服务实现类
 * 
 * Redis 数据结构说明：
 * 1. 用户点赞状态：Set
 *    Key: like:user:{userId}:{targetType}
 *    Value: Set<targetId>
 *    使用 Set 的原因：O(1) 查找效率，天然去重
 *    对应 Redis 命令：SADD、SREM、SISMEMBER、SMEMBERS
 * 
 * 2. 目标点赞数：String
 *    Key: like:count:{targetType}:{targetId}
 *    Value: count (数字字符串)
 *    使用 String 的原因：INCR/DECR 是原子操作，适合计数场景
 *    对应 Redis 命令：INCR、DECR、GET、SET
 * 
 * 3. 待同步数据：Hash
 *    Key: like:pending:sync
 *    Field: userId:targetType:targetId
 *    Value: 1 (点赞) 或 -1 (取消)
 *    使用 Hash 的原因：可批量获取所有待同步数据，字段唯一性保证幂等
 *    对应 Redis 命令：HSET、HGETALL、HDEL
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final StringRedisTemplate stringRedisTemplate;
    private final LikeRecordMapper likeRecordMapper;
    private final BlogMapper blogMapper;
    private final CommentMapper commentMapper;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public Boolean like(Long userId, Long targetId, Integer targetType, Boolean liked) {
        // 构建 Redis Key
        // 用户点赞状态 Key：使用 Set 存储用户点赞的目标ID
        // SISMEMBER 命令 O(1) 时间复杂度判断用户是否已点赞
        String userLikeKey = PostConstants.REDIS_KEY_LIKE_USER + userId + ":" + targetType;
        
        // 目标点赞数 Key：使用 String 存储，支持 INCR/DECR 原子操作
        String countKey = PostConstants.REDIS_KEY_LIKE_COUNT + targetType + ":" + targetId;
        
        // 待同步数据 Key：使用 Hash 存储，field 为复合键保证幂等
        String pendingKey = PostConstants.REDIS_KEY_LIKE_PENDING_SYNC;
        String field = userId + ":" + targetType + ":" + targetId;

        // 检查当前点赞状态
        Boolean isLiked = stringRedisTemplate.opsForSet().isMember(userLikeKey, targetId.toString());

        if (Boolean.TRUE.equals(liked)) {
            // 点赞操作
            if (Boolean.TRUE.equals(isLiked)) {
                return true; // 已点赞，幂等处理
            }
            
            // 1. 添加到用户点赞 Set (SADD)
            stringRedisTemplate.opsForSet().add(userLikeKey, targetId.toString());
            
            // 2. 增加点赞计数 (INCR) - 原子操作
            stringRedisTemplate.opsForValue().increment(countKey);
            
            // 3. 记录待同步数据 (HSET)
            stringRedisTemplate.opsForHash().put(pendingKey, field, "1");
            
            log.debug("用户点赞成功，userId: {}, targetId: {}, targetType: {}", userId, targetId, targetType);
        } else {
            // 取消点赞
            if (Boolean.FALSE.equals(isLiked)) {
                return false; // 未点赞，幂等处理
            }
            
            // 1. 从用户点赞 Set 移除 (SREM)
            stringRedisTemplate.opsForSet().remove(userLikeKey, targetId.toString());
            
            // 2. 减少点赞计数 (DECR) - 原子操作
            stringRedisTemplate.opsForValue().decrement(countKey);
            
            // 3. 记录待同步数据 (HSET)
            stringRedisTemplate.opsForHash().put(pendingKey, field, "-1");
            
            log.debug("用户取消点赞成功，userId: {}, targetId: {}, targetType: {}", userId, targetId, targetType);
        }

        // 检查是否需要触发批量同步
        checkAndTriggerSync();

        return liked;
    }

    @Override
    public Boolean checkUserLiked(Long userId, Long targetId, Integer targetType) {
        String userLikeKey = PostConstants.REDIS_KEY_LIKE_USER + userId + ":" + targetType;
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(userLikeKey, targetId.toString());
        return Boolean.TRUE.equals(isMember);
    }

    @Override
    public Map<Long, Boolean> batchCheckUserLiked(Long userId, List<Long> targetIds, Integer targetType) {
        String userLikeKey = PostConstants.REDIS_KEY_LIKE_USER + userId + ":" + targetType;
        Map<Long, Boolean> result = new HashMap<>();
        
        for (Long targetId : targetIds) {
            Boolean isMember = stringRedisTemplate.opsForSet().isMember(userLikeKey, targetId.toString());
            result.put(targetId, Boolean.TRUE.equals(isMember));
        }
        
        return result;
    }

    @Override
    public Long getLikeCount(Long targetId, Integer targetType) {
        String countKey = PostConstants.REDIS_KEY_LIKE_COUNT + targetType + ":" + targetId;
        String count = stringRedisTemplate.opsForValue().get(countKey);
        
        if (count != null) {
            return Long.parseLong(count);
        }
        
        // 缓存未命中，从数据库查询
        Long dbCount = likeRecordMapper.countByTarget(targetId, targetType);
        
        // 写入缓存
        stringRedisTemplate.opsForValue().set(countKey, dbCount.toString());
        
        return dbCount;
    }

    @Override
    public void syncToDatabase(Long userId, Long targetId, Integer targetType, Boolean liked) {
        if (Boolean.TRUE.equals(liked)) {
            // 插入点赞记录
            LikeRecord record = new LikeRecord();
            record.setUserId(userId);
            record.setTargetId(targetId);
            record.setTargetType(targetType);
            try {
                likeRecordMapper.insert(record);
            } catch (Exception e) {
                log.warn("点赞记录已存在，忽略：userId={}, targetId={}", userId, targetId);
            }
        } else {
            // 删除点赞记录
            likeRecordMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<LikeRecord>()
                    .eq(LikeRecord::getUserId, userId)
                    .eq(LikeRecord::getTargetId, targetId)
                    .eq(LikeRecord::getTargetType, targetType));
        }
        
        // 更新目标的点赞数
        updateTargetLikeCount(targetId, targetType);
    }

    /**
     * 定时批量同步点赞数据到数据库
     * 每30秒执行一次
     */
    @Scheduled(fixedDelay = PostConstants.SYNC_INTERVAL)
    @Override
    public void batchSyncToDatabase() {
        String pendingKey = PostConstants.REDIS_KEY_LIKE_PENDING_SYNC;
        
        // 获取所有待同步数据 (HGETALL)
        Map<Object, Object> pendingData = stringRedisTemplate.opsForHash().entries(pendingKey);
        if (pendingData.isEmpty()) {
            return;
        }
        
        log.info("开始批量同步点赞数据，数量: {}", pendingData.size());
        
        List<LikeRecord> toInsert = new ArrayList<>();
        List<String> toDeleteFields = new ArrayList<>();
        
        for (Map.Entry<Object, Object> entry : pendingData.entrySet()) {
            String field = (String) entry.getKey();
            String action = (String) entry.getValue();
            String[] parts = field.split(":");
            
            Long userId = Long.valueOf(parts[0]);
            Integer targetType = Integer.valueOf(parts[1]);
            Long targetId = Long.valueOf(parts[2]);
            
            if ("1".equals(action)) {
                // 点赞 - 插入记录
                LikeRecord record = new LikeRecord();
                record.setUserId(userId);
                record.setTargetId(targetId);
                record.setTargetType(targetType);
                toInsert.add(record);
            } else {
                // 取消点赞 - 删除记录
                likeRecordMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<LikeRecord>()
                        .eq(LikeRecord::getUserId, userId)
                        .eq(LikeRecord::getTargetId, targetId)
                        .eq(LikeRecord::getTargetType, targetType));
            }
            
            toDeleteFields.add(field);
        }
        
        // 批量插入点赞记录
        if (!toInsert.isEmpty()) {
            likeRecordMapper.batchInsertOrIgnore(toInsert);
        }
        
        // 清除已同步数据 (HDEL)
        if (!toDeleteFields.isEmpty()) {
            stringRedisTemplate.opsForHash().delete(pendingKey, toDeleteFields.toArray());
        }
        
        // 更新目标的点赞数
        updateAllTargetLikeCount(pendingData);
        
        log.info("批量同步点赞数据完成，插入: {}，删除: {}", toInsert.size(), 
                pendingData.size() - toInsert.size());
    }

    // ==================== 私有方法 ====================

    /**
     * 检查是否需要触发批量同步
     */
    private void checkAndTriggerSync() {
        String pendingKey = PostConstants.REDIS_KEY_LIKE_PENDING_SYNC;
        Long size = stringRedisTemplate.opsForHash().size(pendingKey);
        
        if (size >= PostConstants.SYNC_BATCH_SIZE) {
            log.info("待同步数据达到阈值 {}，触发批量同步", PostConstants.SYNC_BATCH_SIZE);
            batchSyncToDatabase();
        }
    }

    /**
     * 更新单个目标的点赞数
     */
    private void updateTargetLikeCount(Long targetId, Integer targetType) {
        Long dbCount = likeRecordMapper.countByTarget(targetId, targetType);
        
        if (targetType.equals(PostConstants.TARGET_TYPE_BLOG)) {
            // 更新博客点赞数
            blogMapper.incrementLikeCount(targetId, dbCount.intValue());
        } else if (targetType.equals(PostConstants.TARGET_TYPE_COMMENT)) {
            // 更新评论点赞数
            commentMapper.incrementLikeCount(targetId, dbCount.intValue());
        }
    }

    /**
     * 更新所有目标的点赞数
     */
    private void updateAllTargetLikeCount(Map<Object, Object> pendingData) {
        // 按 target 分组
        Map<String, Integer> targetCountMap = new HashMap<>();
        
        for (Map.Entry<Object, Object> entry : pendingData.entrySet()) {
            String field = (String) entry.getKey();
            String action = (String) entry.getValue();
            String[] parts = field.split(":");
            
            String targetType = parts[1];
            String targetId = parts[2];
            String key = targetType + ":" + targetId;
            
            int delta = "1".equals(action) ? 1 : -1;
            targetCountMap.merge(key, delta, Integer::sum);
        }
        
        // 更新每个目标
        for (Map.Entry<String, Integer> entry : targetCountMap.entrySet()) {
            String[] keyParts = entry.getKey().split(":");
            Integer targetType = Integer.valueOf(keyParts[0]);
            Long targetId = Long.valueOf(keyParts[1]);
            
            if (targetType.equals(PostConstants.TARGET_TYPE_BLOG)) {
                blogMapper.incrementLikeCount(targetId, entry.getValue());
            } else if (targetType.equals(PostConstants.TARGET_TYPE_COMMENT)) {
                commentMapper.incrementLikeCount(targetId, entry.getValue());
            }
        }
    }
}

