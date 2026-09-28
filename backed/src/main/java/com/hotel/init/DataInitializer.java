package com.hotel.init;

import com.hotel.entity.SysAdmin;
import com.hotel.entity.SysUser;
import com.hotel.service.SysAdminService;
import com.hotel.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 默认账号初始化器：使用明文密码写入默认管理员与用户账号。
 *
 * <p>幂等：仅当对应表为空时才插入，避免重复启动产生脏数据。
 * <p>默认账号：
 * <ul>
 *   <li>管理员：super_admin / super123（超管）、admin / admin123</li>
 *   <li>用户：13800000001、13800000002、13800000003（密码均 123456）</li>
 * </ul>
 *
 * <p>注：业务参考数据（酒店/房型/房间/公告/入住人/订单等）见 sql/data.sql。
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final SysUserService sysUserService;
    private final SysAdminService sysAdminService;

    @Override
    public void run(ApplicationArguments args) {
        initAdmins();
        initUsers();
    }

    private void initAdmins() {
        if (sysAdminService.count() > 0) {
            return;
        }
        SysAdmin superAdmin = new SysAdmin();
        superAdmin.setUsername("super_admin");
        superAdmin.setPassword("super123");
        superAdmin.setNickname("超级管理员");
        superAdmin.setRole("super_admin");
        superAdmin.setStatus(1);

        SysAdmin admin = new SysAdmin();
        admin.setUsername("admin");
        admin.setPassword("admin123");
        admin.setNickname("运营管理员");
        admin.setRole("admin");
        admin.setStatus(1);

        sysAdminService.saveBatch(List.of(superAdmin, admin));
        log.info("初始化默认管理员完成：super_admin/super123、admin/admin123");
    }

    private void initUsers() {
        if (sysUserService.count() > 0) {
            return;
        }
        SysUser u1 = new SysUser();
        u1.setPhone("13800000001");
        u1.setPassword("123456");
        u1.setNickname("张三");
        u1.setGender(1);
        u1.setStatus(1);

        SysUser u2 = new SysUser();
        u2.setPhone("13800000002");
        u2.setPassword("123456");
        u2.setNickname("李四");
        u2.setGender(1);
        u2.setStatus(1);

        SysUser u3 = new SysUser();
        u3.setPhone("13800000003");
        u3.setPassword("123456");
        u3.setNickname("王五");
        u3.setGender(2);
        u3.setStatus(1);

        sysUserService.saveBatch(List.of(u1, u2, u3));
        log.info("初始化默认用户完成：13800000001/02/03，密码均为 123456");
    }
}
