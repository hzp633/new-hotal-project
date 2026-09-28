package com.hotel.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户个人信息（不含密码）
 */
@Data
public class UserProfileVO implements Serializable {

    private Long id;
    private String phone;
    private String nickname;
    private String avatar;
    private Integer gender;
}
