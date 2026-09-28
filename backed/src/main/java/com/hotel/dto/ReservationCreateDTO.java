package com.hotel.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * C 端创建订单请求
 */
@Data
public class ReservationCreateDTO {

    @NotNull(message = "房型不能为空")
    private Long roomTypeId;

    @NotNull(message = "入住日期不能为空")
    private LocalDate checkInDate;

    @NotNull(message = "离店日期不能为空")
    private LocalDate checkOutDate;

    @NotNull(message = "房间数量不能为空")
    @Min(value = 1, message = "房间数量至少为 1")
    private Integer roomCount;

    private String contactName;
    private String contactPhone;
    /** 入住人信息（JSON 字符串） */
    private String guestInfo;
    private String remark;
}
