package com.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付退款表
 *
 * <p>状态：0 待支付 / 1 支付成功 / 2 支付失败 / 3 已退款
 */
@Data
@TableName("payment")
public class Payment implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联订单 ID */
    private Long reservationId;

    /** 用户 ID */
    private Long userId;

    /** 支付流水号 */
    private String payNo;

    /** 支付金额 */
    private BigDecimal amount;

    /** 支付方式 alipay/wechat */
    private String payMethod;

    /** 0 待支付 1 支付成功 2 支付失败 3 已退款 */
    private Integer status;

    /** 支付时间 */
    private LocalDateTime payTime;

    /** 退款金额 */
    private BigDecimal refundAmount;

    /** 退款原因 */
    private String refundReason;

    /** 退款时间 */
    private LocalDateTime refundTime;

    /** 退款操作管理员 ID */
    private Long refundOperator;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
