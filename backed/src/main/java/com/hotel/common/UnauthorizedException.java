package com.hotel.common;

import lombok.Getter;

/**
 * 认证异常：未登录 / Token 无效 / Token 过期。
 * <p>由全局异常处理器映射为 HTTP 401，前端拦截器据此触发 Refresh Token 续期流程。
 */
@Getter
public class UnauthorizedException extends RuntimeException {

    private final Integer code;

    public UnauthorizedException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }
}
