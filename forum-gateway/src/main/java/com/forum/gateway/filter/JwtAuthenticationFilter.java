package com.forum.gateway.filter;

import com.forum.gateway.config.WhitelistProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT 认证过滤器
 * 验证 Token 签名有效性，并检查 Redis 中是否存在该 Token（实现登出后 Token 立即失效）
 */
@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.header}")
    private String header;

    @Value("${jwt.prefix}")
    private String prefix;

    private final WhitelistProperties whitelistProperties;
    private final ReactiveStringRedisTemplate reactiveRedisTemplate;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final String ACCESS_TOKEN_PREFIX = "token:access:";

    public JwtAuthenticationFilter(WhitelistProperties whitelistProperties, 
                                   ReactiveStringRedisTemplate reactiveRedisTemplate) {
        this.whitelistProperties = whitelistProperties;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // 放行CORS预检请求(OPTIONS方法)
        if (request.getMethod().name().equals("OPTIONS")) {
            return chain.filter(exchange);
        }

        // 白名单路径直接放行
        if (isWhitelisted(path)) {
            return chain.filter(exchange);
        }

        // 获取 Authorization 头
        String authHeader = request.getHeaders().getFirst(header);
        if (authHeader == null || !authHeader.startsWith(prefix)) {
            return unauthorized(exchange);
        }

        // 去除前缀，得到实际的 Token（使用 final 变量以便在 lambda 中使用）
        final String token = authHeader.substring(prefix.length());
        
        try {
            // 1. 验证 Token 签名并解析
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            final String userId = claims.getSubject();
            final String username = claims.get("username", String.class);

            // 2. 检查 Token 是否在 Redis 中有效（响应式操作）
            String redisKey = ACCESS_TOKEN_PREFIX + userId;
            
            return reactiveRedisTemplate.opsForValue()
                    .get(redisKey)
                    .flatMap(storedToken -> {
                        // 验证存储的 Token 与传入的 Token 是否一致
                        if (token.equals(storedToken)) {
                            // Token 有效，添加用户信息到请求头
                            ServerHttpRequest mutatedRequest = request.mutate()
                                    .header("X-User-Id", userId)
                                    .header("X-User-Name", username)
                                    .build();
                            return chain.filter(exchange.mutate().request(mutatedRequest).build());
                        } else {
                            // Token 不一致（可能是其他设备登录导致的）
                            log.warn("Token 不匹配, userId: {}", userId);
                            return unauthorized(exchange);
                        }
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        // Redis 中不存在该 Token（已退出登录或已过期）
                        log.warn("Token 已失效或不存在于 Redis, userId: {}", userId);
                        return unauthorized(exchange);
                    }));
                    
        } catch (Exception e) {
            log.error("Token 验证失败: {}", e.getMessage());
            return unauthorized(exchange);
        }
    }

    private boolean isWhitelisted(String path) {
        List<String> whitelist = whitelistProperties.getWhitelist();
        if (whitelist == null) {
            return false;
        }
        return whitelist.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
