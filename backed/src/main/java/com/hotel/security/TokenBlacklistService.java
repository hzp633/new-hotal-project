package com.hotel.security;

import com.hotel.common.ResultCode;
import com.hotel.common.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Token 黑名单服务（基于 Redis），用于登出后使 Access Token 立即失效。
 * <p>键约定：jwt:blacklist:{token}，TTL = Token 剩余有效期。
 */
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String PREFIX = "jwt:blacklist:";

    private final StringRedisTemplate redisTemplate;

    /**
     * 将令牌加入黑名单。
     *
     * @param token         Access Token
     * @param ttlMillis     剩余有效毫秒
     */
    public void blacklist(String token, long ttlMillis) {
        redisTemplate.opsForValue().set(PREFIX + token, "1", Duration.ofMillis(ttlMillis));
    }

    /**
     * 判断令牌是否在黑名单中。Redis 不可用时默认放行（不影响主流程）。
     */
    public boolean isBlacklisted(String token) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + token));
        } catch (Exception e) {
            // Redis 异常时不阻断请求，避免影响可用性
            return false;
        }
    }

    public void requireNotBlacklisted(String token) {
        if (isBlacklisted(token)) {
            throw new UnauthorizedException(ResultCode.TOKEN_INVALID);
        }
    }
}
