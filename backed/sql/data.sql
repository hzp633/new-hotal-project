-- ============================================================
-- 酒店预约管理系统 — 基础数据初始化脚本
-- 说明：
--   1) 账号（sys_user / sys_admin）使用明文密码，方便开发调试。
--      默认账号：管理员 super_admin/super123、admin/admin123；
--               用户 13800000001/123456、13800000002/123456、13800000003/123456。
--   2) 日期使用 CURDATE()/NOW() 表达式，保证数据相对运行日期始终新鲜。
--   3) 显式指定主键 id，保证跨表引用稳定。
-- ============================================================
USE `hotel_reservation`;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `favorite`;
TRUNCATE TABLE `review`;
TRUNCATE TABLE `payment`;
TRUNCATE TABLE `check_in_record`;
TRUNCATE TABLE `reservation`;
TRUNCATE TABLE `guest`;
TRUNCATE TABLE `room`;
TRUNCATE TABLE `room_type`;
TRUNCATE TABLE `announcement`;
TRUNCATE TABLE `hotel`;
TRUNCATE TABLE `sys_user`;
TRUNCATE TABLE `sys_admin`;
SET FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------
-- 默认管理员（明文密码）
-- ------------------------------------------------------------
INSERT INTO `sys_admin` (`id`, `username`, `password`, `nickname`, `role`, `status`) VALUES
(1, 'super_admin', 'super123', '超级管理员', 'super_admin', 1),
(2, 'admin', 'admin123', '运营管理员', 'admin', 1);

-- ------------------------------------------------------------
-- 默认用户（明文密码）
-- ------------------------------------------------------------
INSERT INTO `sys_user` (`id`, `phone`, `password`, `nickname`, `avatar`, `gender`, `status`) VALUES
(1, '13800000001', '123456', '张三', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=professional%20avatar%20portrait%20young%20asian%20man%20friendly&image_size=square', 1, 1),
(2, '13800000002', '123456', '李四', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=professional%20avatar%20portrait%20young%20asian%20man%20business&image_size=square', 1, 1),
(3, '13800000003', '123456', '王五', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=professional%20avatar%20portrait%20young%20asian%20woman%20friendly&image_size=square', 2, 1);

-- ------------------------------------------------------------
-- 酒店信息（单酒店系统，固定 1 条）
-- ------------------------------------------------------------
INSERT INTO `hotel` (`id`, `name`, `star`, `address`, `phone`, `intro`, `cover_image`, `images`, `facilities`) VALUES
(1, '璞石精品酒店', 5, '上海市黄浦区中山东一路 88 号', '021-68888000',
 '璞石精品酒店坐落于外滩核心地段，融合海派风情与现代设计，提供高品质入住体验与贴心管家服务。',
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=luxury%20boutique%20hotel%20exterior%20shanghai%20bund%20night%20elegant&image_size=landscape_16_9',
 '["https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=luxury%20hotel%20lobby%20marble%20chandelier%20elegant&image_size=landscape_16_9","https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=luxury%20hotel%20indoor%20swimming%20pool%20relaxing&image_size=landscape_16_9","https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=luxury%20hotel%20restaurant%20fine%20dining%20elegant&image_size=landscape_16_9"]',
 '免费WiFi,停车场,健身房,游泳池,餐厅,会议室,行李寄存,24小时前台');

-- ------------------------------------------------------------
-- 房型（4 类）
--   id1 大床房 388 / id2 双床房 458 / id3 豪华大床房 588 / id4 豪华套房 888
-- ------------------------------------------------------------
INSERT INTO `room_type` (`id`, `hotel_id`, `name`, `price`, `area`, `bed_type`, `max_people`, `images`, `description`, `status`) VALUES
(1, 1, '高级大床房', 388.00, 28.0, '1.8m 大床', 2,
 '["https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=cozy%20hotel%20queen%20bed%20room%20modern%20warm%20lighting&image_size=landscape_4_3","https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=modern%20hotel%20bedroom%20large%20bed%20city%20view&image_size=landscape_4_3"]',
 '温馨大床房，配备现代化设施，适合商务出行与情侣入住。', 1),
(2, 1, '高级双床房', 458.00, 32.0, '两张 1.2m 单床', 2,
 '["https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=hotel%20twin%20bed%20room%20two%20single%20beds%20clean%20bright&image_size=landscape_4_3","https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=spacious%20hotel%20room%20twin%20beds%20family%20friendly&image_size=landscape_4_3"]',
 '宽敞双床房，适合家庭出游与朋友结伴旅行。', 1),
(3, 1, '豪华大床房', 588.00, 40.0, '1.8m 大床', 3,
 '["https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=luxury%20hotel%20king%20bed%20room%20river%20view%20bathtub&image_size=landscape_4_3","https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=premium%20hotel%20bedroom%20king%20size%20bed%20elegant%20decor&image_size=landscape_4_3"]',
 '豪华大床房，外滩江景视野，配备浴缸与独立衣帽间。', 1),
(4, 1, '豪华江景套房', 888.00, 68.0, '1.8m 大床 + 客厅沙发床', 4,
 '["https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=luxury%20hotel%20suite%20living%20room%20panoramic%20river%20view&image_size=landscape_4_3","https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=opulent%20hotel%20presidential%20suite%20elegant%20furniture&image_size=landscape_4_3"]',
 '顶级套房，270° 江景客厅，专属管家服务，含双人早餐。', 1);

-- ------------------------------------------------------------
-- 房间（8 间）
--   1F:101/102(大床) 2F:201/202(双床) 3F:301/302(豪华大床) 5F:501/502(套房)
--   状态：201 已入住(1) / 302 维护中(2) / 其余空闲(0)
-- ------------------------------------------------------------
INSERT INTO `room` (`id`, `hotel_id`, `room_type_id`, `room_no`, `floor`, `status`, `remark`) VALUES
(1, 1, 1, '101', '1F', 0, NULL),
(2, 1, 1, '102', '1F', 0, NULL),
(3, 1, 2, '201', '2F', 1, NULL),          -- 当前在住（对应订单2）
(4, 1, 2, '202', '2F', 0, NULL),
(5, 1, 3, '301', '3F', 0, NULL),
(6, 1, 3, '302', '3F', 2, '空调检修中'),  -- 维护中
(7, 1, 4, '501', '5F', 0, NULL),
(8, 1, 4, '502', '5F', 0, NULL);

-- ------------------------------------------------------------
-- 公告（2 条：1 条置顶已发布 + 1 条普通已发布）
--   publisher_id=1 由 DataInitializer 创建的 super_admin
-- ------------------------------------------------------------
INSERT INTO `announcement` (`id`, `title`, `content`, `is_top`, `status`, `publisher_id`) VALUES
(1, '国庆中秋双节优惠活动', '即日起至 10 月 7 日，豪华江景套房立减 200 元，更有连住 3 晚享 8 折优惠，欢迎预订体验。', 1, 1, 1),
(2, '客房维护通知', '3F 302 房间因空调检修暂时停用，预计 3 天内恢复，感谢您的理解。', 0, 1, 1);

-- ------------------------------------------------------------
-- 常用入住人（3 条，user_id 1/2 由 DataInitializer 创建）
-- ------------------------------------------------------------
INSERT INTO `guest` (`id`, `user_id`, `name`, `phone`, `id_card`, `gender`, `is_default`) VALUES
(1, 1, '张三', '13800000001', '310101199001011234', 1, 1),
(2, 1, '李梅', '13800000011', '310101199503056789', 2, 0),
(3, 2, '李四', '13800000002', '310101199203034321', 1, 1);

-- ------------------------------------------------------------
-- 订单（3 单，覆盖三种状态：已支付待入住 / 已入住 / 已完成）
--   R1: user1, 豪华大床房(588), 今日+2 入住 +1 晚 → 已支付
--   R2: user2, 双床房(458), 昨日入住住3晚 → 已入住（对应房间 201）
--   R3: user1, 大床房(388), 5天前入住院2晚 → 已完成（房间 101 已退住释放）
-- ------------------------------------------------------------
INSERT INTO `reservation`
  (`id`, `order_no`, `user_id`, `hotel_id`, `room_type_id`, `check_in_date`, `check_out_date`, `days`,
   `room_count`, `total_price`, `actual_amount`, `contact_name`, `contact_phone`, `guest_info`, `status`,
   `pay_time`, `cancel_time`, `remark`)
VALUES
(1, CONCAT('HO', DATE_FORMAT(CURDATE(), '%Y%m%d'), '0001'), 1, 1, 3,
 DATE_ADD(CURDATE(), INTERVAL 2 DAY), DATE_ADD(CURDATE(), INTERVAL 3 DAY), 1,
 1, 588.00, 588.00, '张三', '13800000001',
 '[{"name":"张三","phone":"13800000001","id_card":"310101199001011234"}]', 1,
 DATE_SUB(NOW(), INTERVAL 1 HOUR), NULL, '出行纪念日'),
(2, CONCAT('HO', DATE_FORMAT(CURDATE(), '%Y%m%d'), '0002'), 2, 1, 2,
 DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 3,
 1, 1374.00, 1374.00, '李四', '13800000002',
 '[{"name":"李四","phone":"13800000002","id_card":"310101199203034321"}]', 2,
 DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, NULL),
(3, CONCAT('HO', DATE_FORMAT(CURDATE(), '%Y%m%d'), '0003'), 1, 1, 1,
 DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_SUB(CURDATE(), INTERVAL 3 DAY), 2,
 1, 776.00, 776.00, '张三', '13800000001',
 '[{"name":"张三","phone":"13800000001","id_card":"310101199001011234"}]', 3,
 DATE_SUB(NOW(), INTERVAL 6 DAY), NULL, NULL);

-- ------------------------------------------------------------
-- 支付记录（3 条，与订单一一对应，均支付成功）
-- ------------------------------------------------------------
INSERT INTO `payment`
  (`id`, `reservation_id`, `user_id`, `pay_no`, `amount`, `pay_method`, `status`, `pay_time`,
   `refund_amount`, `refund_reason`, `refund_time`, `refund_operator`)
VALUES
(1, 1, 1, CONCAT('PAY', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'), '001'), 588.00, 'alipay', 1, DATE_SUB(NOW(), INTERVAL 1 HOUR),
 0.00, NULL, NULL, NULL),
(2, 2, 2, CONCAT('PAY', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'), '002'), 1374.00, 'wechat', 1, DATE_SUB(NOW(), INTERVAL 1 DAY),
 0.00, NULL, NULL, NULL),
(3, 3, 1, CONCAT('PAY', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'), '003'), 776.00, 'alipay', 1, DATE_SUB(NOW(), INTERVAL 6 DAY),
 0.00, NULL, NULL, NULL);

-- ------------------------------------------------------------
-- 入住退住记录（2 条）
--   CIR1: 订单2 在住 → 房间 201(已入住)
--   CIR2: 订单3 已退住 → 房间 101(已释放)
-- ------------------------------------------------------------
INSERT INTO `check_in_record`
  (`id`, `reservation_id`, `user_id`, `hotel_id`, `room_id`, `check_in_time`, `check_out_time`,
   `extra_fee`, `status`, `operator_id`, `remark`)
VALUES
(1, 2, 2, 1, 3, DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, 0.00, 1, 2, NULL),
(2, 3, 1, 1, 1, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), 0.00, 2, 2, '正常退住');

-- ------------------------------------------------------------
-- 评论（1 条，对应已完成订单3）
-- ------------------------------------------------------------
INSERT INTO `review`
  (`id`, `user_id`, `hotel_id`, `room_type_id`, `reservation_id`, `rating`, `content`, `images`,
   `reply`, `reply_time`, `status`)
VALUES
(1, 1, 1, 1, 3, 5, '位置绝佳，房间干净整洁，前台服务热情周到，外滩夜景尽收眼底，强烈推荐！',
 '["https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=beautiful%20hotel%20room%20interior%20cozy%20atmosphere%20window%20view&image_size=square"]',
 '感谢您的肯定与推荐，期待您再次下榻璞石！', DATE_SUB(NOW(), INTERVAL 4 DAY), 1);

-- ------------------------------------------------------------
-- 收藏（1 条：用户2 收藏豪华大床房）
-- ------------------------------------------------------------
INSERT INTO `favorite` (`id`, `user_id`, `room_type_id`) VALUES
(1, 2, 3);
