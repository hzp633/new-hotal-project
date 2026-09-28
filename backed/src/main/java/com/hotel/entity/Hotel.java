package com.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 酒店信息表（单酒店系统，固定 1 条记录）
 */
@Data
@TableName("hotel")
public class Hotel implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 酒店名称 */
    private String name;

    /** 星级 1-5 */
    private Integer star;

    /** 详细地址 */
    private String address;

    /** 联系电话 */
    private String phone;

    /** 酒店介绍 */
    private String intro;

    /** 封面图 URL */
    private String coverImage;

    /** 环境图片集（JSON 数组） */
    private String images;

    /** 设施标签（逗号分隔） */
    private String facilities;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
