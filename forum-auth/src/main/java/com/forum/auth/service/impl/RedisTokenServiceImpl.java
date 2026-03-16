package com.forum.auth.service.impl;

import com.forum.auth.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Token 服务 Redis 实现
 * 使用 Redis 存储 Token 实现登录状态管理
 * 
 * Redis Key 设计：
 * - Access Token: token:access:{userId} -> token值
 * - Refresh Token: token:refresh:{userId} -> token值
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisTokenServiceImpl implements TokenService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String ACCESS_TOKEN_PREFIX = "token:access:";
    private static final String REFRESH_TOKEN_PREFIX = "token:refresh:";

    @Override
    public void storeAccessToken(Long userId, String token, long expiration) {
        String key = ACCESS_TOKEN_PREFIX + userId;
        stringRedisTemplate.opsForValue().set(key, token, expiration, TimeUnit.MILLISECONDS);
        log.debug("存储 Access Token, userId: {}, 过期时间: {}ms", userId, expiration);
    }

    @Override
    public void storeRefreshToken(Long userId, String token, long expiration) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        stringRedisTemplate.opsForValue().set(key, token, expiration, TimeUnit.MILLISECONDS);
        log.debug("存储 Refresh Token, userId: {}, 过期时间: {}ms", userId, expiration);
    }

    @Override
    public boolean validateAccessToken(Long userId, String token) {
        if (userId == null || token == null) {
            return false;
        }
        String key = ACCESS_TOKEN_PREFIX + userId;
        String storedToken = stringRedisTemplate.opsForValue().get(key);
        // 验证存储的 token 与传入的 token 是否一致
        return token.equals(storedToken);
    }

    @Override
    public boolean validateRefreshToken(Long userId, String token) {
        if (userId == null || token == null) {
            return false;
        }
        String key = REFRESH_TOKEN_PREFIX + userId;
        String storedToken = stringRedisTemplate.opsForValue().get(key);
        return token.equals(storedToken);
    }

    @Override
    public void removeAllTokens(Long userId) {
        String accessKey = ACCESS_TOKEN_PREFIX + userId;
        String refreshKey = REFRESH_TOKEN_PREFIX + userId;
        stringRedisTemplate.delete(accessKey);
        stringRedisTemplate.delete(refreshKey);
        log.info("删除用户所有 Token, userId: {}", userId);
    }

    @Override
    public void removeAccessToken(Long userId, String token) {
        String key = ACCESS_TOKEN_PREFIX + userId;
        // 只有当存储的 token 与传入的一致时才删除，防止误删其他设备的登录状态
        String storedToken = stringRedisTemplate.opsForValue().get(key);
        if (token.equals(storedToken)) {
            stringRedisTemplate.delete(key);
            log.info("删除 Access Token, userId: {}", userId);
        }
    }

    @Override
    public void removeRefreshToken(Long userId, String token) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        String storedToken = stringRedisTemplate.opsForValue().get(key);
        if (token.equals(storedToken)) {
            stringRedisTemplate.delete(key);
            log.info("删除 Refresh Token, userId: {}", userId);
        }
    }

    @Override
    public String getCurrentAccessToken(Long userId) {
        String key = ACCESS_TOKEN_PREFIX + userId;
        return stringRedisTemplate.opsForValue().get(key);
    }

    @Override
    public void refreshAccessToken(Long userId, String newAccessToken, long expiration) {
        // 直接覆盖旧的 Access Token
        storeAccessToken(userId, newAccessToken, expiration);
        log.info("刷新 Access Token, userId: {}", userId);
    }
}
