package com.hotel.security;

import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;

/**
 * B 端管理员 JWT 拦截器（admin 角色）。
 */
@Component
public class AdminJwtInterceptor extends BaseJwtInterceptor {

    @Override
    protected String role() {
        return "admin";
    }

    @Override
    protected Claims parse(String token) {
        return jwtUtil.parseAdminClaims(token);
    }

    @Override
    protected void setContext(Long id) {
        BaseContext.setAdminId(id);
    }
}
