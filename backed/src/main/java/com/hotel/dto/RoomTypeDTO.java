package com.hotel.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 房型新增/更新请求
 */
@Data
public class RoomTypeDTO implements Serializable {

    private Long id;
    private String name;
    private BigDecimal price;
    private BigDecimal area;
    private String bedType;
    private Integer maxPeople;
    private String images;
    private String description;
    private Integer status;
}
