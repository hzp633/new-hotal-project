package com.hotel.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 生成与解析工具。
 * <p>Claim 约定：sub=主体ID，type=access|refresh，role=user|admin。
 */
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties props;
    private SecretKey userKey;
    private SecretKey adminKey;

    @PostConstruct
    public void init() {
        userKey = Keys.hmacShaKeyFor(props.getUser().getSecret().getBytes(StandardCharsets.UTF_8));
        adminKey = Keys.hmacShaKeyFor(props.getAdmin().getSecret().getBytes(StandardCharsets.UTF_8));
    }

    // ==================== 用户端 ====================

    public String generateUserAccessToken(Long userId) {
        return build(userId, "user", "access", userKey, props.getUser().getAccessExpire());
    }

    public String generateUserRefreshToken(Long userId) {
        return build(userId, "user", "refresh", userKey, props.getUser().getRefreshExpire());
    }

    public Claims parseUserClaims(String token) {
        return parse(token, userKey);
    }

    // ==================== 管理员端 ====================

    public String generateAdminAccessToken(Long adminId) {
        return build(adminId, "admin", "access", adminKey, props.getAdmin().getAccessExpire());
    }

    public String generateAdminRefreshToken(Long adminId) {
        return build(adminId, "admin", "refresh", adminKey, props.getAdmin().getRefreshExpire());
    }

    public Claims parseAdminClaims(String token) {
        return parse(token, adminKey);
    }

    // ==================== 通用 ====================

    /**
     * 计算令牌剩余有效毫秒（用于登出黑名单 TTL）。
     *
     * @return 剩余毫秒，最小 1
     */
    public long getRemainingMillis(Claims claims) {
        long exp = claims.getExpiration().getTime();
        return Math.max(1, exp - System.currentTimeMillis());
    }

    public static String getSubject(Claims claims) {
        return claims.getSubject();
    }

    public static String getType(Claims claims) {
        return claims.get("type", String.class);
    }

    public static String getRole(Claims claims) {
        return claims.get("role", String.class);
    }

    private String build(Long id, String role, String type, SecretKey key, long expireSeconds) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(id))
                .claim("role", role)
                .claim("type", type)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireSeconds * 1000L))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    private Claims parse(String token, SecretKey key) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
