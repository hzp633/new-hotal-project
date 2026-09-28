package com.hotel.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 公告新增/更新请求
 */
@Data
public class AnnouncementDTO implements Serializable {

    private Long id;
    private String title;
    private String content;
    private Integer isTop;
    private Integer status;
}
