package com.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 登录成功响应（用户端 / 管理员端通用）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO implements Serializable {

    private String accessToken;

    private String refreshToken;

    /** 用户 ID 或管理员 ID */
    private Long userId;

    private String nickname;

    /** user / admin / super_admin */
    private String role;
}
