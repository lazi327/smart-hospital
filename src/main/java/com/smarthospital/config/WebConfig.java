package com.smarthospital.config;

import com.smarthospital.common.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    /** 前后端分离部署时放行的前端地址（逗号分隔）；留空表示同源部署，不启用 CORS */
    @Value("${app.cors.allowed-origins:}")
    private String allowedOrigins;

    /**
     * 用过滤器而非 addCorsMappings：预检 OPTIONS 会在进入 AuthInterceptor 之前被处理掉，
     * 否则不带 X-Auth-Token 的预检请求会被拦截成 401。
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        if (StringUtils.hasText(allowedOrigins)) {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOriginPatterns(Arrays.stream(allowedOrigins.split(","))
                    .map(String::trim).filter(StringUtils::hasText).toList());
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            config.setAllowedHeaders(List.of("*"));
            config.setMaxAge(3600L);
            source.registerCorsConfiguration("/api/**", config);
        }
        FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>(new CorsFilter(source));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/departments",
                        "/api/doctors", "/api/doctors/**",
                        "/api/schedules"
                );
    }
}
