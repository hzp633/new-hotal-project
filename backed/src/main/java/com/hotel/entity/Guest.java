package com.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 常用入住人表
 */
@Data
@TableName("guest")
public class Guest implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户 ID */
    private Long userId;

    /** 入住人姓名 */
    private String name;

    /** 手机号 */
    private String phone;

    /** 身份证号 */
    private String idCard;

    /** 性别 0 未知 1 男 2 女 */
    private Integer gender;

    /** 是否默认 0 否 1 是 */
    @TableField("is_default")
    private Integer isDefault;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
