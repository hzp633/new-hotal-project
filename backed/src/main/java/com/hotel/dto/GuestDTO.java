package com.hotel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 常用入住人请求 DTO
 */
@Data
public class GuestDTO {

    /** 主键 ID（更新时必填） */
    private Long id;

    @NotBlank(message = "入住人姓名不能为空")
    private String name;

    @NotBlank(message = "手机号不能为空")
    private String phone;

    private String idCard;

    private Integer gender;

    private Integer isDefault;
}
