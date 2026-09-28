package com.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订单表（核心交易表）
 *
 * <p>状态：0 待支付 / 1 已支付 / 2 已入住 / 3 已完成 / 4 已取消 / 5 已退款
 */
@Data
@TableName("reservation")
public class Reservation implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单编号 */
    private String orderNo;

    /** 下单用户 ID */
    private Long userId;

    /** 酒店 ID */
    private Long hotelId;

    /** 房型 ID */
    private Long roomTypeId;

    /** 入住日期 */
    private LocalDate checkInDate;

    /** 离店日期 */
    private LocalDate checkOutDate;

    /** 入住天数 */
    private Integer days;

    /** 房间数量 */
    private Integer roomCount;

    /** 订单总价 */
    private BigDecimal totalPrice;

    /** 实付金额 */
    private BigDecimal actualAmount;

    /** 联系人姓名 */
    private String contactName;

    /** 联系人电话 */
    private String contactPhone;

    /** 入住人信息（JSON 数组快照） */
    private String guestInfo;

    /** 0 待支付 1 已支付 2 已入住 3 已完成 4 已取消 5 已退款 */
    private Integer status;

    /** 支付时间 */
    private LocalDateTime payTime;

    /** 取消时间 */
    private LocalDateTime cancelTime;

    /** 订单备注 */
    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
