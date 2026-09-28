package com.hotel.config;

import com.hotel.security.AdminJwtInterceptor;
import com.hotel.security.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：注册 JWT 拦截器、配置跨域。
 *
 * <p>路径约定：
 * <ul>
 *   <li>公开：/public/**、/auth/**（C 端登录注册）、/admin/auth/**（B 端登录）</li>
 *   <li>C 端需登录：/user/**、/reservation/**、/guest/**、/favorite/**、/review/**、/checkin/**</li>
 *   <li>B 端需登录：/admin/**（除 /admin/auth/**）</li>
 * </ul>
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;
    private final AdminJwtInterceptor adminJwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // C 端用户拦截器
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns(
                        "/user/**",
                        "/reservation/**",
                        "/guest/**",
                        "/favorite/**",
                        "/review/**",
                        "/checkin/**")
                .order(1);

        // B 端管理员拦截器
        registry.addInterceptor(adminJwtInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/auth/**", "/admin/public/**")
                .order(2);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
