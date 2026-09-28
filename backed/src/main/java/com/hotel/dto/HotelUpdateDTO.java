package com.hotel.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 酒店信息更新请求（单酒店系统，固定更新 ID=1）
 */
@Data
public class HotelUpdateDTO implements Serializable {

    private String name;
    private Integer star;
    private String address;
    private String phone;
    private String intro;
    private String coverImage;
    private String images;
    private String facilities;
}
