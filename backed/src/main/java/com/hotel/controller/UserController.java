package com.hotel.controller;

import com.hotel.common.BusinessException;
import com.hotel.common.Result;
import com.hotel.common.ResultCode;
import com.hotel.dto.ChangePasswordDTO;
import com.hotel.dto.UpdateProfileDTO;
import com.hotel.dto.UserProfileVO;
import com.hotel.entity.SysUser;
import com.hotel.service.SysUserService;
import com.hotel.security.AuthHeaderUtil;
import com.hotel.security.BaseContext;
import com.hotel.security.JwtUtil;
import com.hotel.security.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端用户：登出 / 个人信息（需登录，由 {@code JwtInterceptor} 校验）。
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final SysUserService sysUserService;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    /** 登出：将 Access Token 加入黑名单 */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = AuthHeaderUtil.extract(request);
        if (token != null) {
            try {
                Claims claims = jwtUtil.parseUserClaims(token);
                tokenBlacklistService.blacklist(token, jwtUtil.getRemainingMillis(claims));
            } catch (Exception ignored) {
                // Token 无效则无需加入黑名单
            }
        }
        return Result.ok();
    }

    /** 获取当前登录用户信息 */
    @GetMapping("/profile")
    public Result<UserProfileVO> profile() {
        Long userId = BaseContext.getUserId();
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setPhone(user.getPhone());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setGender(user.getGender());
        return Result.ok(vo);
    }

    /** 更新个人资料（仅非空字段会被更新） */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody UpdateProfileDTO dto) {
        Long userId = BaseContext.getUserId();
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        SysUser update = new SysUser();
        update.setId(userId);
        update.setNickname(dto.getNickname());
        update.setPhone(dto.getPhone());
        update.setAvatar(dto.getAvatar());
        update.setGender(dto.getGender());
        sysUserService.updateById(update);
        return Result.ok();
    }

    /** 修改密码 */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody @Valid ChangePasswordDTO dto) {
        Long userId = BaseContext.getUserId();
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (!user.getPassword().equals(dto.getOldPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }
        SysUser update = new SysUser();
        update.setId(userId);
        update.setPassword(dto.getNewPassword());
        sysUserService.updateById(update);
        return Result.ok();
    }
}
