package com.hotel.security;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Authorization 请求头解析工具：提取 Bearer Token。
 */
public final class AuthHeaderUtil {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private AuthHeaderUtil() {
    }

    /**
     * 从请求头提取 Access Token，无或格式错误返回 null。
     */
    public static String extract(HttpServletRequest request) {
        String header = request.getHeader(HEADER);
        if (header != null && header.startsWith(PREFIX)) {
            String token = header.substring(PREFIX.length()).trim();
            return token.isEmpty() ? null : token;
        }
        return null;
    }
}
