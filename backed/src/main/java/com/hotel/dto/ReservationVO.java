package com.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * C 端订单视图
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationVO {

    private Long id;
    private String orderNo;
    private Long roomTypeId;
    private String roomTypeName;
    private BigDecimal roomTypePrice;
    private String roomTypeImages;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer days;
    private Integer roomCount;
    private BigDecimal totalPrice;
    private BigDecimal actualAmount;
    private String contactName;
    private String contactPhone;
    private String guestInfo;
    private Integer status;
    private LocalDateTime payTime;
    private LocalDateTime cancelTime;
    private String remark;
    private LocalDateTime createTime;
}
