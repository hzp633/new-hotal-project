package com.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 入住退住记录表
 *
 * <p>状态：1 在住 / 2 已退住
 */
@Data
@TableName("check_in_record")
public class CheckInRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联订单 ID */
    private Long reservationId;

    /** 用户 ID */
    private Long userId;

    /** 酒店 ID */
    private Long hotelId;

    /** 实际分配房间 ID */
    private Long roomId;

    /** 入住时间 */
    private LocalDateTime checkInTime;

    /** 退住时间 */
    private LocalDateTime checkOutTime;

    /** 额外费用 */
    private BigDecimal extraFee;

    /** 1 在住 2 已退住 */
    private Integer status;

    /** 办理人（管理员 ID） */
    private Long operatorId;

    /** 备注 */
    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
