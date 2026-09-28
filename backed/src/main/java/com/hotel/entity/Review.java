package com.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论表
 */
@Data
@TableName("review")
public class Review implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 评论用户 ID */
    private Long userId;

    /** 酒店 ID */
    private Long hotelId;

    /** 关联房型 ID */
    private Long roomTypeId;

    /** 关联订单 ID */
    private Long reservationId;

    /** 评分 1-5 星 */
    private Integer rating;

    /** 评论内容 */
    private String content;

    /** 评论图片（JSON 数组） */
    private String images;

    /** 管理员回复 */
    private String reply;

    /** 回复时间 */
    private LocalDateTime replyTime;

    /** 0 隐藏 1 正常 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
