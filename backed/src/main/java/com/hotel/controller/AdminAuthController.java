package com.hotel.controller;

import com.hotel.common.BusinessException;
import com.hotel.common.Result;
import com.hotel.common.ResultCode;
import com.hotel.common.UnauthorizedException;
import com.hotel.dto.AdminLoginDTO;
import com.hotel.dto.LoginVO;
import com.hotel.dto.RefreshTokenDTO;
import com.hotel.entity.SysAdmin;
import com.hotel.security.JwtUtil;
import com.hotel.service.SysAdminService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * B 端认证：登录 / 刷新令牌（公开接口）。
 * <p>登出需 Access Token，见 {@link AdminAccountController}。
 */
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final SysAdminService sysAdminService;
    private final JwtUtil jwtUtil;

    /** 管理员登录 */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid AdminLoginDTO dto) {
        SysAdmin admin = sysAdminService.lambdaQuery()
                .eq(SysAdmin::getUsername, dto.getUsername())
                .one();
        if (admin == null) {
            throw new BusinessException(ResultCode.ADMIN_NOT_FOUND);
        }
        if (admin.getStatus() != null && admin.getStatus() == 0) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }
        if (!admin.getPassword().equals(dto.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        return Result.ok(buildLoginVO(admin));
    }

    /** 刷新 Access Token */
    @PostMapping("/refresh")
    public Result<LoginVO> refresh(@RequestBody @Valid RefreshTokenDTO dto) {
        Claims claims;
        try {
            claims = jwtUtil.parseAdminClaims(dto.getRefreshToken());
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException(ResultCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException(ResultCode.REFRESH_TOKEN_INVALID);
        }
        if (!"refresh".equals(JwtUtil.getType(claims)) || !"admin".equals(JwtUtil.getRole(claims))) {
            throw new UnauthorizedException(ResultCode.REFRESH_TOKEN_INVALID);
        }
        Long adminId = Long.valueOf(claims.getSubject());
        SysAdmin admin = sysAdminService.getById(adminId);
        if (admin == null || (admin.getStatus() != null && admin.getStatus() == 0)) {
            throw new UnauthorizedException(ResultCode.REFRESH_TOKEN_INVALID);
        }
        return Result.ok(buildLoginVO(admin));
    }

    private LoginVO buildLoginVO(SysAdmin admin) {
        return LoginVO.builder()
                .accessToken(jwtUtil.generateAdminAccessToken(admin.getId()))
                .refreshToken(jwtUtil.generateAdminRefreshToken(admin.getId()))
                .userId(admin.getId())
                .nickname(admin.getNickname())
                .role(admin.getRole())
                .build();
    }
}
