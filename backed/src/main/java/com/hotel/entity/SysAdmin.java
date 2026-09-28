package com.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员表（B 端管理员）
 */
@Data
@TableName("sys_admin")
public class SysAdmin implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录账号 */
    private String username;

    /** 密码（BCrypt 加密） */
    private String password;

    /** 姓名 */
    private String nickname;

    /** 角色 super_admin / admin */
    private String role;

    /** 状态 0 禁用 1 正常 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
