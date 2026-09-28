package com.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 房间表
 */
@Data
@TableName("room")
public class Room implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属酒店 ID */
    private Long hotelId;

    /** 所属房型 ID */
    private Long roomTypeId;

    /** 房间号 */
    private String roomNo;

    /** 楼层 */
    private String floor;

    /** 0 空闲 1 已入住 2 维护中 */
    private Integer status;

    /** 备注 */
    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
