package com.itheima.activiti.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.itheima.activiti.config.JwtAuthenticationFilter;
import com.itheima.activiti.util.JwtUtil;
import com.itheima.activiti.service.TenantService;

/**
 * Spring Security配置类
 * 配置JWT认证和授权机制
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final TenantService tenantService;

    // 构造函数注入JwtUtil和TenantService
    public SecurityConfig(JwtUtil jwtUtil, TenantService tenantService) {
        this.jwtUtil = jwtUtil;
        this.tenantService = tenantService;
    }

    /**
     * 配置安全过滤链
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF保护
            .csrf(csrf -> csrf.disable())
            // 允许所有CORS请求
            .cors(cors -> cors.disable())
            // 设置会话管理为无状态（不使用会话）
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 添加JWT认证过滤器
            .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, tenantService), UsernamePasswordAuthenticationFilter.class)
            // 配置请求授权规则 - 允许所有请求
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            // 配置安全头，为H2控制台禁用X-Frame-Options
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.disable())
            );

        return http.build();
    }
    
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}