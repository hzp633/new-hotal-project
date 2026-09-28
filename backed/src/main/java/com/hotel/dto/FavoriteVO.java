package com.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收藏列表项 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteVO implements Serializable {

    private Long id;

    private Long roomTypeId;

    private String roomTypeName;

    private BigDecimal price;

    private String images;

    private String bedType;

    private BigDecimal area;

    private LocalDateTime createTime;
}
