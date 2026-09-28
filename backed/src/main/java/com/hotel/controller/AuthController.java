package com.hotel.controller;

import com.hotel.common.BusinessException;
import com.hotel.common.Result;
import com.hotel.common.ResultCode;
import com.hotel.common.UnauthorizedException;
import com.hotel.dto.LoginVO;
import com.hotel.dto.RefreshTokenDTO;
import com.hotel.dto.UserLoginDTO;
import com.hotel.dto.UserRegisterDTO;
import com.hotel.entity.SysUser;
import com.hotel.security.JwtUtil;
import com.hotel.service.SysUserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端认证：注册 / 登录 / 刷新令牌（公开接口）。
 * <p>登出需 Access Token，见 {@link UserController}。
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;
    private final JwtUtil jwtUtil;

    /** 用户注册（注册成功后自动登录，返回令牌） */
    @PostMapping("/register")
    public Result<LoginVO> register(@RequestBody @Valid UserRegisterDTO dto) {
        long exists = sysUserService.lambdaQuery()
                .eq(SysUser::getPhone, dto.getPhone())
                .count();
        if (exists > 0) {
            throw new BusinessException(ResultCode.USER_EXISTS);
        }

        SysUser user = new SysUser();
        user.setPhone(dto.getPhone());
        user.setPassword(dto.getPassword());
        user.setNickname(StringUtils.hasText(dto.getNickname())
                ? dto.getNickname()
                : "用户" + dto.getPhone().substring(7));
        user.setGender(0);
        user.setStatus(1);
        sysUserService.save(user);

        return Result.ok(buildLoginVO(user, "user"));
    }

    /** 用户登录 */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid UserLoginDTO dto) {
        SysUser user = sysUserService.lambdaQuery()
                .eq(SysUser::getPhone, dto.getPhone())
                .one();
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }
        if (!user.getPassword().equals(dto.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        return Result.ok(buildLoginVO(user, "user"));
    }

    /** 刷新 Access Token */
    @PostMapping("/refresh")
    public Result<LoginVO> refresh(@RequestBody @Valid RefreshTokenDTO dto) {
        Claims claims;
        try {
            claims = jwtUtil.parseUserClaims(dto.getRefreshToken());
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException(ResultCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException(ResultCode.REFRESH_TOKEN_INVALID);
        }
        if (!"refresh".equals(JwtUtil.getType(claims)) || !"user".equals(JwtUtil.getRole(claims))) {
            throw new UnauthorizedException(ResultCode.REFRESH_TOKEN_INVALID);
        }
        Long userId = Long.valueOf(claims.getSubject());
        SysUser user = sysUserService.getById(userId);
        if (user == null || (user.getStatus() != null && user.getStatus() == 0)) {
            throw new UnauthorizedException(ResultCode.REFRESH_TOKEN_INVALID);
        }
        return Result.ok(buildLoginVO(user, "user"));
    }

    private LoginVO buildLoginVO(SysUser user, String role) {
        return LoginVO.builder()
                .accessToken(jwtUtil.generateUserAccessToken(user.getId()))
                .refreshToken(jwtUtil.generateUserRefreshToken(user.getId()))
                .userId(user.getId())
                .nickname(user.getNickname())
                .role(role)
                .build();
    }
}
