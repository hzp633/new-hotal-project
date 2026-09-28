package com.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * B 端用户管理视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserVO implements Serializable {

    private Long id;
    private String phone;
    private String nickname;
    private String avatar;
    private Integer gender;
    private Integer status;
    private LocalDateTime createTime;
}
