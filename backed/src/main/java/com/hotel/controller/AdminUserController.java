package com.hotel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.common.BusinessException;
import com.hotel.common.PageResult;
import com.hotel.common.Result;
import com.hotel.common.ResultCode;
import com.hotel.dto.AdminUserVO;
import com.hotel.entity.SysUser;
import com.hotel.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * B 端用户管理
 */
@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final SysUserService sysUserService;

    /** 用户分页查询 */
    @GetMapping("/page")
    public Result<PageResult<AdminUserVO>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) Integer status) {
        Page<SysUser> page = new Page<>(current, size);
        Page<SysUser> result = sysUserService.lambdaQuery()
                .like(StringUtils.hasText(phone), SysUser::getPhone, phone)
                .like(StringUtils.hasText(nickname), SysUser::getNickname, nickname)
                .eq(status != null, SysUser::getStatus, status)
                .orderByDesc(SysUser::getCreateTime)
                .page(page);
        List<AdminUserVO> vos = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return Result.ok(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), vos));
    }

    /** 用户详情 */
    @GetMapping("/{id}")
    public Result<AdminUserVO> detail(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return Result.ok(toVO(user));
    }

    /** 启用 / 禁用用户 */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        user.setStatus(status);
        sysUserService.updateById(user);
        return Result.ok();
    }

    private AdminUserVO toVO(SysUser user) {
        return AdminUserVO.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .gender(user.getGender())
                .status(user.getStatus())
                .createTime(user.getCreateTime())
                .build();
    }
}
