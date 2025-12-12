package com.itheima.activiti.config;

import com.itheima.activiti.service.TenantService;
import com.itheima.activiti.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
@EnableMethodSecurity(prePostEnabled = true) // 启用方法级安全，支持@PreAuthorize注解
public class SecurityConfig {

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private TenantService tenantService;
    
    @Autowired
    private AppIdSecretKeyAuthenticationFilter appIdSecretKeyAuthenticationFilter;
    
    @Autowired
    private ApiCallLogFilter apiCallLogFilter;

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
                config.setAllowedOriginPatterns(java.util.Arrays.asList("*")); // 允许所有来源，生产环境应限制
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
                .requestMatchers("/api/auth/login", "/auth/login", "/api/auth/tenant/login", "/auth/tenant/login").permitAll()
                // 允许访问H2控制台（仅开发环境）
                .requestMatchers("/h2-console/**").permitAll()
                // 允许访问静态资源
                .requestMatchers("/static/**", "/public/**").permitAll()
                // 允许访问Swagger UI相关路径
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                // 允许访问租户注册接口
                .requestMatchers("/api/v1/tenant/register", "/v1/tenant/register").permitAll()
                // 允许访问测试接口
                .requestMatchers("/test").permitAll()
                // 允许访问公共服务API
                .requestMatchers("/api/public/**", "/public/**").permitAll()
                // 允许访问v1路径下的API
                .requestMatchers("/v1/**").permitAll()
                // 允许访问/api/v1路径下的API
                .requestMatchers("/api/v1/**", "/api/admin/**").permitAll()
                // 允许访问流程图接口，不需要认证
                .requestMatchers("/api/process/definition/**/diagram").permitAll()
                // 其他所有请求都需要认证
                .anyRequest().authenticated()
            )
            // 配置安全头，为H2控制台禁用X-Frame-Options
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.disable())
            );

        // 添加JWT认证过滤器，放在UsernamePasswordAuthenticationFilter之前
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtUtil, tenantService);
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        
        // 添加APP ID和Secret Key鉴权过滤器，放在JWT过滤器之前
        http.addFilterBefore(appIdSecretKeyAuthenticationFilter, jwtFilter.getClass());
        
        // 添加API调用日志过滤器，放在最前面
        http.addFilterBefore(apiCallLogFilter, appIdSecretKeyAuthenticationFilter.getClass());

        return http.build();
    }
    
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}