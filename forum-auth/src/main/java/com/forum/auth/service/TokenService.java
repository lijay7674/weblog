package com.forum.auth.service;

/**
 * Token 服务接口
 * 负责 Token 在 Redis 中的存储、删除、验证等操作
 */
public interface TokenService {

    /**
     * 存储用户的 Access Token
     * @param userId 用户ID
     * @param token JWT Token
     * @param expiration 过期时间（毫秒）
     */
    void storeAccessToken(Long userId, String token, long expiration);

    /**
     * 存储用户的 Refresh Token
     * @param userId 用户ID
     * @param token Refresh Token
     * @param expiration 过期时间（毫秒）
     */
    void storeRefreshToken(Long userId, String token, long expiration);

    /**
     * 验证 Access Token 是否有效（存在于Redis中）
     * @param userId 用户ID
     * @param token JWT Token
     * @return true=有效，false=无效（已退出登录或被踢出）
     */
    boolean validateAccessToken(Long userId, String token);

    /**
     * 验证 Refresh Token 是否有效
     * @param userId 用户ID
     * @param token Refresh Token
     * @return true=有效，false=无效
     */
    boolean validateRefreshToken(Long userId, String token);

    /**
     * 删除用户的所有 Token（退出登录）
     * @param userId 用户ID
     */
    void removeAllTokens(Long userId);

    /**
     * 删除指定的 Access Token
     * @param userId 用户ID
     * @param token JWT Token
     */
    void removeAccessToken(Long userId, String token);

    /**
     * 删除指定的 Refresh Token
     * @param userId 用户ID
     * @param token Refresh Token
     */
    void removeRefreshToken(Long userId, String token);

    /**
     * 获取用户当前有效的 Access Token
     * @param userId 用户ID
     * @return 当前的 Access Token，如果不存在返回 null
     */
    String getCurrentAccessToken(Long userId);

    /**
     * 使用 Refresh Token 刷新 Access Token
     * 删除旧的 Access Token，存储新的 Access Token
     * @param userId 用户ID
     * @param newAccessToken 新的 Access Token
     * @param expiration 过期时间（毫秒）
     */
    void refreshAccessToken(Long userId, String newAccessToken, long expiration);
}
