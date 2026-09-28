package com.hotel.controller;

import com.hotel.common.BusinessException;
import com.hotel.common.Result;
import com.hotel.common.ResultCode;
import com.hotel.dto.AdminProfileVO;
import com.hotel.entity.SysAdmin;
import com.hotel.service.SysAdminService;
import com.hotel.security.AuthHeaderUtil;
import com.hotel.security.BaseContext;
import com.hotel.security.JwtUtil;
import com.hotel.security.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * B 端管理员：登出 / 个人信息（需登录，由 {@code AdminJwtInterceptor} 校验）。
 * <p>其它 B 端业务接口（用户/房型/房间/订单/入住/评论/公告管理）待后续实现。
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminAccountController {

    private final SysAdminService sysAdminService;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    /** 登出：将 Access Token 加入黑名单 */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = AuthHeaderUtil.extract(request);
        if (token != null) {
            try {
                Claims claims = jwtUtil.parseAdminClaims(token);
                tokenBlacklistService.blacklist(token, jwtUtil.getRemainingMillis(claims));
            } catch (Exception ignored) {
                // Token 无效则无需加入黑名单
            }
        }
        return Result.ok();
    }

    /** 获取当前登录管理员信息 */
    @GetMapping("/profile")
    public Result<AdminProfileVO> profile() {
        Long adminId = BaseContext.getAdminId();
        SysAdmin admin = sysAdminService.getById(adminId);
        if (admin == null) {
            throw new BusinessException(ResultCode.ADMIN_NOT_FOUND);
        }
        AdminProfileVO vo = new AdminProfileVO();
        vo.setId(admin.getId());
        vo.setUsername(admin.getUsername());
        vo.setNickname(admin.getNickname());
        vo.setRole(admin.getRole());
        return Result.ok(vo);
    }
}
