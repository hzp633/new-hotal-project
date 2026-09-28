package com.hotel.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置：用户端与管理员端使用不同密钥与有效期，满足权限区分。
 * 对应 application.yml 中 hotel.jwt.* 配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "hotel.jwt")
public class JwtProperties {

    private TokenConfig user = new TokenConfig();
    private TokenConfig admin = new TokenConfig();

    @Data
    public static class TokenConfig {
        /** HMAC 密钥（需 ≥ 32 字节） */
        private String secret;
        /** Access Token 有效期（秒） */
        private long accessExpire;
        /** Refresh Token 有效期（秒） */
        private long refreshExpire;
    }
}
