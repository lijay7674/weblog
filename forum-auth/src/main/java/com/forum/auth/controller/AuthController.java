package com.forum.auth.controller;

import com.forum.auth.dto.LoginRequest;
import com.forum.auth.dto.LoginResponse;
import com.forum.auth.dto.RefreshTokenRequest;
import com.forum.auth.dto.RegisterRequest;
import com.forum.auth.entity.User;
import com.forum.auth.jwt.JwtUtil;
import com.forum.auth.service.TokenService;
import com.forum.auth.service.UserService;
import com.forum.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 处理用户登录、注册等认证相关请求
 */
@Slf4j
@Tag(name = "认证接口", description = "登录、注册、Token刷新等")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final TokenService tokenService;
    private final JwtUtil jwtUtil;

    /**
     * 用户注册
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        return Result.success("注册成功", user);
    }

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.success("登录成功", response);
    }

    /**
     * 获取当前用户信息
     * 需要通过网关携带有效的 Token
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        User user = userService.getById(userId);
        if (user != null) {
            user.setPassword(null); // 不返回密码
        }
        return Result.success(user);
    }

    /**
     * 退出登录
     * 从 Redis 中删除用户的 Token，使其立即失效
     */
    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        
        // 从 Redis 中删除用户的所有 Token
        tokenService.removeAllTokens(userId);
        
        log.info("用户退出登录成功, userId: {}", userId);
        return Result.success("退出成功", null);
    }

    /**
     * 刷新 Token
     * 使用 Refresh Token 获取新的 Access Token
     */
    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public Result<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = userService.refreshToken(request.getRefreshToken());
        return Result.success("刷新成功", response);
    }
}
