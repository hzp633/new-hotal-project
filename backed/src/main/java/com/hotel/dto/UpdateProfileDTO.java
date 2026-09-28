package com.hotel.dto;

import lombok.Data;

/**
 * 用户资料更新 DTO（仅非空字段会被更新）
 */
@Data
public class UpdateProfileDTO {

    private String nickname;

    private String phone;

    private String avatar;

    private Integer gender;
}
