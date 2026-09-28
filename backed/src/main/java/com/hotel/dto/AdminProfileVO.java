package com.hotel.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 管理员个人信息（不含密码）
 */
@Data
public class AdminProfileVO implements Serializable {

    private Long id;
    private String username;
    private String nickname;
    private String role;
}
