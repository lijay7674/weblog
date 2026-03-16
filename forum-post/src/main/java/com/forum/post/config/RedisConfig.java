package com.forum.post.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类
 * 配置 StringRedisTemplate 用于点赞缓存、浏览量统计等
 */
@Configuration
public class RedisConfig {

    /**
     * 配置 StringRedisTemplate
     * 使用 String 序列化器，适合存储字符串类型的数据
     * 用于：
     * - 点赞状态缓存 (Set)
     * - 点赞计数 (String INCR/DECR)
     * - 浏览量计数 (String INCR)
     * - 博客详情缓存 (String JSON)
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(org.springframework.data.redis.connection.RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        
        // Key 序列化器
        template.setKeySerializer(new StringRedisSerializer());
        // Value 序列化器
        template.setValueSerializer(new StringRedisSerializer());
        // Hash Key 序列化器
        template.setHashKeySerializer(new StringRedisSerializer());
        // Hash Value 序列化器
        template.setHashValueSerializer(new StringRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }
}
