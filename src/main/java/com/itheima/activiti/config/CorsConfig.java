package com.itheima.activiti.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * CORS配置类
 * 允许前端应用跨域访问后端API
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许所有来源的请求
        // 使用setAllowedOriginPatterns代替addAllowedOrigin，因为allowCredentials为true时不能使用*通配符
        config.setAllowedOriginPatterns(Arrays.asList("*"));
        // 允许所有HTTP方法
        config.addAllowedMethod("*");
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 允许携带认证信息
        config.setAllowCredentials(true);
        // 允许的暴露头
        config.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        // 设置预检请求的有效期（秒）
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有路径应用CORS配置
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}