package com.forum.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 负责Token的生成、解析、验证
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshExpiration;

    /**
     * 生成密钥
     * HMAC-SHA 算法要求密钥长度至少 256 位 (32 字节)
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 根据用户信息生成 Token
     * @param userId 用户ID
     * @param username 用户名
     * @return JWT Token
     */
    public String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        return createToken(claims, userId.toString(), expiration);
    }

    /**
     * 生成刷新 Token
     */
    public String generateRefreshToken(Long userId) {
        return createToken(new HashMap<>(), userId.toString(), refreshExpiration);
    }

    /**
     * 创建 Token
     * @param claims 自定义声明
     * @param subject 主题（通常是用户ID）
     * @param expiration 过期时间（毫秒）
     */
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * 从 Token 中获取用户ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims != null ? Long.parseLong(claims.getSubject()) : null;
    }

    /**
     * 从 Token 中获取用户名
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("username", String.class) : null;
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查 Token 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 获取 Token 的剩余过期时间（毫秒）
     * @param token JWT Token
     * @return 剩余过期时间（毫秒），如果 token 无效或已过期返回 0
     */
    public long getRemainingExpiration(String token) {
        try {
            Claims claims = parseToken(token);
            if (claims == null) {
                return 0;
            }
            Date expiration = claims.getExpiration();
            Date now = new Date();
            long remaining = expiration.getTime() - now.getTime();
            return remaining > 0 ? remaining : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取 Access Token 的默认过期时间（毫秒）
     */
    public long getAccessTokenExpiration() {
        return expiration;
    }

    /**
     * 获取 Refresh Token 的默认过期时间（毫秒）
     */
    public long getRefreshTokenExpiration() {
        return refreshExpiration;
    }

    /**
     * 解析 Token 获取 Claims
     * @param token JWT Token
     * @return Claims 对象，解析失败返回 null
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }
}
