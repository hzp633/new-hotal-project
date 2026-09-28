package com.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 房型表
 */
@Data
@TableName("room_type")
public class RoomType implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属酒店 ID（固定为 1） */
    private Long hotelId;

    /** 房型名称 */
    private String name;

    /** 每日价格 */
    private BigDecimal price;

    /** 房间面积(㎡) */
    private BigDecimal area;

    /** 床型 */
    private String bedType;

    /** 最大入住人数 */
    private Integer maxPeople;

    /** 房型图片（JSON 数组） */
    private String images;

    /** 房型描述 */
    private String description;

    /** 0 停用 1 启用 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
