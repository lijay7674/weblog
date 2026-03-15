package com.forum.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forum.auth.dto.LoginRequest;
import com.forum.auth.dto.LoginResponse;
import com.forum.auth.dto.RegisterRequest;
import com.forum.auth.entity.User;
import com.forum.auth.mapper.UserMapper;
import com.forum.auth.service.UserService;
import com.forum.common.exception.BusinessException;
import com.forum.common.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.expiration}")
    private Long tokenExpiration;

    @Override
    public User register(RegisterRequest request) {
        // 1. 检查用户名是否已存在
        User existUser = getByUsername(request.getUsername());
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        // 密码加密存储
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setEmail(request.getEmail());
        user.setStatus(1);  // 默认正常状态
        user.setDeleted(0); // 未删除

        // 3. 保存到数据库
        userMapper.insert(user);
        log.info("用户注册成功: {}", user.getUsername());

        // 4. 返回用户信息（不返回密码）
        user.setPassword(null);
        return user;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        // 1. 查询用户
        User user = getByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 2. 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // 3. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 4. 生成 Token
        String accessToken = jwtUtil.generateToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // 5. 构建响应
        LoginResponse response = new LoginResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(tokenExpiration);

        // 6. 构建用户信息
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setEmail(user.getEmail());
        response.setUser(userInfo);

        log.info("用户登录成功: {}", user.getUsername());
        return response;
    }

    @Override
    public User getByUsername(String username) {
        return userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
        );
    }

    @Override
    public User getById(Long userId) {
        return userMapper.selectById(userId);
    }
}
