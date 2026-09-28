-- ============================================================
-- 酒店预约管理系统 — 数据库建表脚本
-- 数据库：MySQL 8.0  |  存储引擎：InnoDB  |  字符集：utf8mb4
-- 表数量：13 张
-- 说明：依据《数据库表.md》设计；不建物理外键，关键数据通过 status 控制状态
-- ============================================================

DROP DATABASE IF EXISTS `hotel_reservation`;
CREATE DATABASE `hotel_reservation` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `hotel_reservation`;

-- ------------------------------------------------------------
-- 1. sys_user 用户表（C 端注册用户）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户 ID',
  `phone`       VARCHAR(20)  NOT NULL COMMENT '手机号（登录账号）',
  `password`    VARCHAR(100) NOT NULL COMMENT '密码（BCrypt 加密）',
  `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
  `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像 URL',
  `gender`      TINYINT      DEFAULT 0 COMMENT '性别 0 未知 1 男 2 女',
  `status`      TINYINT      DEFAULT 1 COMMENT '状态 0 禁用 1 正常',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- ------------------------------------------------------------
-- 2. sys_admin 管理员表（B 端管理员）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_admin` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '管理员 ID',
  `username`    VARCHAR(50)  NOT NULL COMMENT '登录账号',
  `password`    VARCHAR(100) NOT NULL COMMENT '密码（BCrypt 加密）',
  `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT '姓名',
  `role`        VARCHAR(20)  DEFAULT 'admin' COMMENT '角色 super_admin / admin',
  `status`      TINYINT      DEFAULT 1 COMMENT '状态 0 禁用 1 正常',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='管理员表';

-- ------------------------------------------------------------
-- 3. hotel 酒店信息表（单酒店系统，仅 1 条记录，id 固定为 1）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `hotel` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '酒店 ID（固定为 1）',
  `name`        VARCHAR(100) NOT NULL COMMENT '酒店名称',
  `star`        TINYINT      DEFAULT 3 COMMENT '星级 1-5',
  `address`     VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
  `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
  `intro`       TEXT         COMMENT '酒店介绍',
  `cover_image` VARCHAR(255) DEFAULT NULL COMMENT '封面图 URL',
  `images`      TEXT         COMMENT '环境图片集（JSON 数组）',
  `facilities`  VARCHAR(500) DEFAULT NULL COMMENT '设施标签（逗号分隔）',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='酒店信息表';

-- ------------------------------------------------------------
-- 4. room_type 房型表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `room_type` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '房型 ID',
  `hotel_id`    BIGINT       NOT NULL DEFAULT 1 COMMENT '所属酒店 ID（固定为 1）',
  `name`        VARCHAR(50)  NOT NULL COMMENT '房型名称',
  `price`       DECIMAL(10,2) NOT NULL COMMENT '每日价格',
  `area`        DECIMAL(6,1) DEFAULT NULL COMMENT '房间面积(㎡)',
  `bed_type`    VARCHAR(30)  DEFAULT NULL COMMENT '床型',
  `max_people`  TINYINT      DEFAULT 2 COMMENT '最大入住人数',
  `images`      TEXT         COMMENT '房型图片（JSON 数组）',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '房型描述',
  `status`      TINYINT      DEFAULT 1 COMMENT '0 停用 1 启用',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_hotel_status` (`hotel_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='房型表';

-- ------------------------------------------------------------
-- 5. room 房间表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `room` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '房间 ID',
  `hotel_id`    BIGINT       NOT NULL DEFAULT 1 COMMENT '所属酒店 ID',
  `room_type_id` BIGINT      NOT NULL COMMENT '所属房型 ID',
  `room_no`     VARCHAR(20)  NOT NULL COMMENT '房间号',
  `floor`       VARCHAR(10)  DEFAULT NULL COMMENT '楼层',
  `status`      TINYINT      DEFAULT 0 COMMENT '0 空闲 1 已入住 2 维护中',
  `remark`      VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_no` (`room_no`),
  KEY `idx_type_status` (`room_type_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='房间表';

-- ------------------------------------------------------------
-- 6. guest 常用入住人表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `guest` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id`     BIGINT       NOT NULL COMMENT '所属用户 ID',
  `name`        VARCHAR(50)  NOT NULL COMMENT '入住人姓名',
  `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
  `id_card`     VARCHAR(20)  DEFAULT NULL COMMENT '身份证号',
  `gender`      TINYINT      DEFAULT 0 COMMENT '性别 0 未知 1 男 2 女',
  `is_default`  TINYINT      DEFAULT 0 COMMENT '是否默认 0 否 1 是',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='常用入住人表';

-- ------------------------------------------------------------
-- 7. reservation 订单表（核心交易表）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `reservation` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '订单 ID',
  `order_no`       VARCHAR(32)  NOT NULL COMMENT '订单编号',
  `user_id`        BIGINT       NOT NULL COMMENT '下单用户 ID',
  `hotel_id`       BIGINT       NOT NULL DEFAULT 1 COMMENT '酒店 ID',
  `room_type_id`   BIGINT       NOT NULL COMMENT '房型 ID',
  `check_in_date`  DATE         NOT NULL COMMENT '入住日期',
  `check_out_date` DATE         NOT NULL COMMENT '离店日期',
  `days`           INT          NOT NULL COMMENT '入住天数',
  `room_count`     INT          DEFAULT 1 COMMENT '房间数量',
  `total_price`    DECIMAL(10,2) NOT NULL COMMENT '订单总价',
  `actual_amount`  DECIMAL(10,2) NOT NULL COMMENT '实付金额',
  `contact_name`   VARCHAR(50)  NOT NULL COMMENT '联系人姓名',
  `contact_phone`  VARCHAR(20)  NOT NULL COMMENT '联系人电话',
  `guest_info`     TEXT         COMMENT '入住人信息（JSON 数组快照）',
  `status`         TINYINT      DEFAULT 0 COMMENT '0 待支付 1 已支付 2 已入住 3 已完成 4 已取消 5 已退款',
  `pay_time`       DATETIME     DEFAULT NULL COMMENT '支付时间',
  `cancel_time`    DATETIME     DEFAULT NULL COMMENT '取消时间',
  `remark`         VARCHAR(255) DEFAULT NULL COMMENT '订单备注',
  `create_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `update_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_date` (`check_in_date`, `check_out_date`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单表';

-- ------------------------------------------------------------
-- 8. check_in_record 入住退住记录表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `check_in_record` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录 ID',
  `reservation_id` BIGINT       NOT NULL COMMENT '关联订单 ID',
  `user_id`        BIGINT       NOT NULL COMMENT '用户 ID',
  `hotel_id`       BIGINT       NOT NULL DEFAULT 1 COMMENT '酒店 ID',
  `room_id`        BIGINT       NOT NULL COMMENT '实际分配房间 ID',
  `check_in_time`  DATETIME     NOT NULL COMMENT '入住时间',
  `check_out_time` DATETIME     DEFAULT NULL COMMENT '退住时间',
  `extra_fee`      DECIMAL(10,2) DEFAULT 0 COMMENT '额外费用',
  `status`         TINYINT      DEFAULT 1 COMMENT '1 在住 2 已退住',
  `operator_id`    BIGINT       DEFAULT NULL COMMENT '办理人（管理员 ID）',
  `remark`         VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_reservation` (`reservation_id`),
  KEY `idx_room_status` (`room_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='入住退住记录表';

-- ------------------------------------------------------------
-- 9. payment 支付退款表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `payment` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '支付 ID',
  `reservation_id`  BIGINT       NOT NULL COMMENT '关联订单 ID',
  `user_id`         BIGINT       NOT NULL COMMENT '用户 ID',
  `pay_no`          VARCHAR(32)  NOT NULL COMMENT '支付流水号',
  `amount`          DECIMAL(10,2) NOT NULL COMMENT '支付金额',
  `pay_method`      VARCHAR(20)  DEFAULT 'alipay' COMMENT '支付方式 alipay/wechat',
  `status`          TINYINT      DEFAULT 0 COMMENT '0 待支付 1 支付成功 2 支付失败 3 已退款',
  `pay_time`        DATETIME     DEFAULT NULL COMMENT '支付时间',
  `refund_amount`   DECIMAL(10,2) DEFAULT 0 COMMENT '退款金额',
  `refund_reason`   VARCHAR(255) DEFAULT NULL COMMENT '退款原因',
  `refund_time`     DATETIME     DEFAULT NULL COMMENT '退款时间',
  `refund_operator` BIGINT       DEFAULT NULL COMMENT '退款操作管理员 ID',
  `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pay_no` (`pay_no`),
  KEY `idx_reservation` (`reservation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='支付退款表';

-- ------------------------------------------------------------
-- 10. review 评论表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `review` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '评论 ID',
  `user_id`        BIGINT       NOT NULL COMMENT '评论用户 ID',
  `hotel_id`       BIGINT       NOT NULL DEFAULT 1 COMMENT '酒店 ID',
  `room_type_id`   BIGINT       DEFAULT NULL COMMENT '关联房型 ID',
  `reservation_id` BIGINT       NOT NULL COMMENT '关联订单 ID',
  `rating`         TINYINT      NOT NULL COMMENT '评分 1-5 星',
  `content`        TEXT         COMMENT '评论内容',
  `images`         TEXT         COMMENT '评论图片（JSON 数组）',
  `reply`          VARCHAR(500) DEFAULT NULL COMMENT '管理员回复',
  `reply_time`     DATETIME     DEFAULT NULL COMMENT '回复时间',
  `status`         TINYINT      DEFAULT 1 COMMENT '0 隐藏 1 正常',
  `create_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  `update_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_hotel_status` (`hotel_id`, `status`),
  KEY `idx_reservation` (`reservation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='评论表';

-- ------------------------------------------------------------
-- 11. favorite 收藏表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `favorite` (
  `id`            BIGINT  NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id`       BIGINT  NOT NULL COMMENT '用户 ID',
  `room_type_id`  BIGINT  NOT NULL COMMENT '收藏的房型 ID',
  `create_time`   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_type` (`user_id`, `room_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='收藏表';

-- ------------------------------------------------------------
-- 12. announcement 公告表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `announcement` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '公告 ID',
  `title`       VARCHAR(100) NOT NULL COMMENT '标题',
  `content`     TEXT         COMMENT '内容',
  `is_top`      TINYINT      DEFAULT 0 COMMENT '是否置顶 0 否 1 是',
  `status`      TINYINT      DEFAULT 1 COMMENT '0 下架 1 发布',
  `publisher_id` BIGINT      DEFAULT NULL COMMENT '发布人（管理员 ID）',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status_top` (`status`, `is_top`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公告表';

-- ------------------------------------------------------------
-- 13. operation_log 操作日志表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `operation_log` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志 ID',
  `admin_id`    BIGINT       DEFAULT NULL COMMENT '操作管理员 ID',
  `module`      VARCHAR(50)  DEFAULT NULL COMMENT '操作模块',
  `action`      VARCHAR(50)  DEFAULT NULL COMMENT '操作类型',
  `method`      VARCHAR(200) DEFAULT NULL COMMENT '请求方法',
  `params`      TEXT         COMMENT '请求参数（JSON）',
  `ip`          VARCHAR(50)  DEFAULT NULL COMMENT '操作 IP',
  `cost_time`   INT          DEFAULT NULL COMMENT '耗时(ms)',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_admin_time` (`admin_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志表';
