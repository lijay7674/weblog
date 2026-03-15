package com.forum.common.utils;

/**
 * 用户上下文工具类
 * 用于在请求上下文中存储和获取当前登录用户信息
 * 
 * 工作原理：
 * 网关解析 JWT 后，将用户信息放入请求头 (X-User-Id, X-User-Name)
 * 各微服务通过拦截器提取请求头，存入 ThreadLocal
 * 业务代码通过此类静态方法获取当前用户
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_NAME = new ThreadLocal<>();

    /**
     * 设置当前用户信息
     */
    public static void setUser(Long userId, String username) {
        USER_ID.set(userId);
        USER_NAME.set(username);
    }

    /**
     * 获取当前用户ID
     */
    public static Long getUserId() {
        return USER_ID.get();
    }

    /**
     * 获取当前用户名
     */
    public static String getUsername() {
        return USER_NAME.get();
    }

    /**
     * 判断当前是否已登录
     */
    public static boolean isLogin() {
        return USER_ID.get() != null;
    }

    /**
     * 清除上下文（请求结束时调用，防止内存泄漏）
     */
    public static void clear() {
        USER_ID.remove();
        USER_NAME.remove();
    }
}
