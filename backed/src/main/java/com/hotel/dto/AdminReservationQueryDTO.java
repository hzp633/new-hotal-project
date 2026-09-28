package com.hotel.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * B 端订单分页查询参数
 */
@Data
public class AdminReservationQueryDTO implements Serializable {

    private Long current = 1L;
    private Long size = 10L;
    private String orderNo;
    private String phone;
    private Integer status;
    private LocalDate startDate;
    private LocalDate endDate;
}
