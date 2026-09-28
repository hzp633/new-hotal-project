package com.hotel.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * C 端房型筛选条件
 */
@Data
public class RoomTypeSearchDTO {

    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String bedType;
    /** "price_asc" / "price_desc" / null */
    private String orderBy;
}
