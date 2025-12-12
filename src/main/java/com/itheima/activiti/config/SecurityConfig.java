package com.itheima.activiti.config;

import com.itheima.activiti.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security配置类
 * 配置JWT认证和授权机制
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 配置安全过滤链
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF保护，因为我们使用JWT
            .csrf(csrf -> csrf.disable())
            // 配置CORS，允许前端跨域访问
            .cors(cors -> cors.configurationSource(request -> {
                org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
                config.setAllowCredentials(true);
                config.addAllowedOrigin("http://localhost:5173"); // 前端服务地址
                config.addAllowedHeader("*");
                config.addAllowedMethod("*");
                return config;
            }))
            // 设置会话管理为无状态（不使用会话）
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 配置请求授权规则
            .authorizeHttpRequests(auth -> auth
                // 允许所有OPTIONS请求
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 允许访问登录接口（适配前端代理后的路径）
                .requestMatchers("/api/auth/login", "/auth/login").permitAll()
                // 允许访问流程部署和查询接口（测试用）
                .requestMatchers("/api/process/definition/deploy/leave", "/test/process/list", "/api/process/definition/list").permitAll()
                // 允许访问H2控制台（仅开发环境）
                .requestMatchers("/h2-console/**").permitAll()
                // 允许访问静态资源
                .requestMatchers("/static/**", "/public/**").permitAll()
                // 允许访问通知测试接口（适配前端代理前后的路径）
                .requestMatchers("/notification/test/**", "/api/notification/test/**").permitAll()
                // 其他所有请求都需要认证
                .anyRequest().authenticated()
            )
            // 配置安全头，为H2控制台禁用X-Frame-Options
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.disable())
            );

        // 添加JWT认证过滤器，放在UsernamePasswordAuthenticationFilter之前
        http.addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}