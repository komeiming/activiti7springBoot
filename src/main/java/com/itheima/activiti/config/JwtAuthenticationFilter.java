package com.itheima.activiti.config;

import com.itheima.activiti.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JWT认证过滤器
 * 用于验证JWT token并设置认证信息
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;

    // 构造函数注入JwtUtil
    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 增加详细的请求日志
        String requestUri = request.getRequestURI();
        String requestMethod = request.getMethod();
        String requestUrl = request.getRequestURL().toString();
        String contextPath = request.getContextPath();
        logger.info("[JWT过滤器] 请求开始 - URI: {}, 方法: {}, URL: {}, ContextPath: {}", 
                   requestUri, requestMethod, requestUrl, contextPath);
        
        // 记录所有请求头，帮助调试
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            logger.debug("[JWT过滤器] 请求头 - {}: {}", headerName, request.getHeader(headerName));
        }
        
        // 对于登录请求和测试请求，直接放行，不进行JWT验证处理
        // 适配多种可能的登录请求路径：
        // 1. /api/auth/login - 直接访问后端的路径
        // 2. /auth/login - 前端代理后（移除/api前缀）的路径
        boolean isLoginRequest = (("/api/auth/login".equals(requestUri) || "/auth/login".equals(requestUri)) && "POST".equals(requestMethod));
        // 适配测试请求路径
        boolean isTestRequest = requestUri.startsWith("/notification/test/");
        if (isLoginRequest || isTestRequest) {
            logger.info("[JWT过滤器] 匹配到登录请求或测试请求，直接放行。请求URI: {}", requestUri);
            filterChain.doFilter(request, response);
            logger.info("[JWT过滤器] 登录请求或测试请求处理完成");
            return;
        }
        
        logger.info("[JWT过滤器] 非登录请求，继续JWT验证流程");
        
        // 从请求头中获取token
        String authHeader = request.getHeader("Authorization");
        logger.debug("Authorization头: {}", authHeader);

        // 只处理Bearer token格式
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            logger.debug("提取到token: {}", token != null ? "[存在]" : "[不存在]");

            try {
                // 验证token是否有效
                if (jwtUtil.validateToken(token)) {
                    // 从token中获取Claims
                    Claims claims = jwtUtil.getClaimsFromToken(token);
                    if (claims != null) {
                        // 获取用户名
                        String username = claims.getSubject();
                        // 获取角色信息
                        String role = (String) claims.get("role");
                        String rolesStr = (String) claims.get("roles");
                        
                        logger.debug("从token中提取的用户信息: 用户名={}, 角色={}", username, role);
                        
                        // 构建权限列表
                        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                        
                        // 优先使用rolesStr中的多角色信息（完整角色列表）
                        if (rolesStr != null) {
                            String[] roles = rolesStr.split(",");
                            for (String r : roles) {
                                authorities.add(new SimpleGrantedAuthority("ROLE_" + r));
                            }
                        } else if (role != null) {
                            // 如果没有多角色信息，只添加主角色
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                        }
                        
                        // 设置认证信息到SecurityContext
                        if (SecurityContextHolder.getContext().getAuthentication() == null) {
                            logger.debug("设置认证信息: 用户名={}, 权限列表={}", username, authorities);
                            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                    username, null, authorities);
                            SecurityContextHolder.getContext().setAuthentication(authToken);
                        }
                    }
                } else {
                    logger.warn("无效的token");
                }
            } catch (Exception e) {
                logger.error("JWT认证失败: {}", e.getMessage());
                // 不阻止请求继续处理，只是不设置认证信息
                SecurityContextHolder.clearContext();
            }
        }

        // 继续过滤链
        filterChain.doFilter(request, response);
    }
}