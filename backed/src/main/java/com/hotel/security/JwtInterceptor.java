package com.hotel.security;

import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;

/**
 * C 端用户 JWT 拦截器（user 角色）。
 */
@Component
public class JwtInterceptor extends BaseJwtInterceptor {

    @Override
    protected String role() {
        return "user";
    }

    @Override
    protected Claims parse(String token) {
        return jwtUtil.parseUserClaims(token);
    }

    @Override
    protected void setContext(Long id) {
        BaseContext.setUserId(id);
    }
}
