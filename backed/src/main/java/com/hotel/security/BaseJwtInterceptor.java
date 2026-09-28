package com.hotel.security;

import com.hotel.common.ResultCode;
import com.hotel.common.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 拦截器公共逻辑：解析 Access Token → 校验类型/角色/黑名单 → 写入 ThreadLocal。
 */
public abstract class BaseJwtInterceptor implements HandlerInterceptor {

    @Autowired
    protected JwtUtil jwtUtil;

    @Autowired
    protected TokenBlacklistService tokenBlacklistService;

    /** 当前拦截器期望的角色：user / admin */
    protected abstract String role();

    /** 使用对应密钥解析 Token */
    protected abstract Claims parse(String token);

    /** 将主体 ID 写入上下文 */
    protected abstract void setContext(Long id);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = extractToken(request);
        if (token == null) {
            throw new UnauthorizedException(ResultCode.UNAUTHORIZED);
        }

        Claims claims;
        try {
            claims = parse(token);
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException(ResultCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException(ResultCode.TOKEN_INVALID);
        }

        tokenBlacklistService.requireNotBlacklisted(token);

        if (!"access".equals(JwtUtil.getType(claims)) || !role().equals(JwtUtil.getRole(claims))) {
            throw new UnauthorizedException(ResultCode.TOKEN_INVALID);
        }

        setContext(Long.valueOf(claims.getSubject()));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                               Object handler, Exception ex) {
        BaseContext.clear();
    }

    protected String extractToken(HttpServletRequest request) {
        return AuthHeaderUtil.extract(request);
    }
}
