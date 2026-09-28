package com.hotel.security;

/**
 * 请求上下文：在拦截器中解析 Token 后写入当前用户 / 管理员 ID，供业务层使用。
 * 基于 ThreadLocal，请求结束由拦截器清理。
 */
public final class BaseContext {

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> CURRENT_ADMIN_ID = new ThreadLocal<>();

    private BaseContext() {
    }

    public static void setUserId(Long userId) {
        CURRENT_USER_ID.set(userId);
    }

    public static Long getUserId() {
        return CURRENT_USER_ID.get();
    }

    public static void setAdminId(Long adminId) {
        CURRENT_ADMIN_ID.set(adminId);
    }

    public static Long getAdminId() {
        return CURRENT_ADMIN_ID.get();
    }

    public static void clear() {
        CURRENT_USER_ID.remove();
        CURRENT_ADMIN_ID.remove();
    }
}
