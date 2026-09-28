package com.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公告表
 */
@Data
@TableName("announcement")
public class Announcement implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 是否置顶 0 否 1 是 */
    @TableField("is_top")
    private Integer isTop;

    /** 0 下架 1 发布 */
    private Integer status;

    /** 发布人（管理员 ID） */
    private Long publisherId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
