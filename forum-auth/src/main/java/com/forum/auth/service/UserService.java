package com.forum.auth.service;

import com.forum.auth.dto.LoginRequest;
import com.forum.auth.dto.LoginResponse;
import com.forum.auth.dto.RegisterRequest;
import com.forum.auth.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     * @param request 注册请求
     * @return 用户信息
     */
    User register(RegisterRequest request);

    /**
     * 用户登录
     * @param request 登录请求
     * @return 登录响应（包含Token和用户信息）
     */
    LoginResponse login(LoginRequest request);

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户实体
     */
    User getByUsername(String username);

    /**
     * 根据ID查询用户
     * @param userId 用户ID
     * @return 用户实体
     */
    User getById(Long userId);
}
