package com.hotel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 刷新令牌请求
 */
@Data
public class RefreshTokenDTO implements Serializable {

    @NotBlank(message = "refreshToken 不能为空")
    private String refreshToken;
}
