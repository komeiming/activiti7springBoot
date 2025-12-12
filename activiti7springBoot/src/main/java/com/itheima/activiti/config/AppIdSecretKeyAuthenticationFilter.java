package com.itheima.activiti.config;

import com.itheima.activiti.common.TenantContext;
import com.itheima.activiti.entity.Tenant;
import com.itheima.activiti.service.TenantService;
import com.itheima.activiti.utils.SignatureUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义HttpServletRequestWrapper，用于保存请求body内容，以便后续可以重复读取
 */
class BodyCachingHttpServletRequestWrapper extends HttpServletRequestWrapper {
    private final String body;

    public BodyCachingHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        // 读取并保存请求body
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InputStream inputStream = request.getInputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
        }
        this.body = baos.toString(request.getCharacterEncoding());
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body.getBytes(getCharacterEncoding()));
        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }

            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }
            
            @Override
            public void setReadListener(jakarta.servlet.ReadListener listener) {
                // 不需要实现
            }
        };
    }

    @Override
    public java.io.BufferedReader getReader() throws IOException {
        return new java.io.BufferedReader(new java.io.StringReader(body));
    }

    public String getBody() {
        return body;
    }
}

/**
 * APP ID和Secret Key鉴权过滤器，用于处理第三方系统的API调用鉴权
 */
@Component
public class AppIdSecretKeyAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private TenantService tenantService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        // 包装请求，以便后续可以重复读取body
        BodyCachingHttpServletRequestWrapper wrappedRequest = new BodyCachingHttpServletRequestWrapper(request);
        
        // 获取请求路径
        String requestUri = wrappedRequest.getRequestURI();
        
        // 判断是否是公有服务API路径，排除管理员API
        if (((requestUri.startsWith("/api/notification") || requestUri.startsWith("/api/workflow")) || 
             (requestUri.startsWith("/api/v1/notification") || requestUri.startsWith("/api/v1/workflow"))) && 
             !requestUri.startsWith("/api/admin/")) {
            
            // 设置响应字符编码为UTF-8
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=UTF-8");
            
            // 首先检查是否已经通过JWT认证（租户用户登录）
            String authorization = wrappedRequest.getHeader("Authorization");
            if (authorization != null && authorization.startsWith("Bearer ")) {
                // JWT认证通过，从SecurityContextHolder获取用户信息
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated()) {
                    // 获取用户名（实际是appId）
                    String appId = authentication.getName();
                    
                    // 验证租户是否存在且状态正常
                    Tenant tenant = tenantService.getByAppId(appId);
                    if (tenant != null && "approved".equals(tenant.getStatus())) {
                        // 将租户信息存储到上下文
                        TenantContext.setTenantId(tenant.getId());
                        TenantContext.setAppId(appId);
                        // 继续执行过滤器链
                        filterChain.doFilter(wrappedRequest, response);
                        return;
                    }
                }
            }
            
            // 如果没有JWT认证，才检查APP ID/Secret Key认证
            // 获取请求头中的APP ID、签名和时间戳
            String appId = wrappedRequest.getHeader("X-APP-ID");
            String signature = wrappedRequest.getHeader("X-SIGN");
            String timestamp = wrappedRequest.getHeader("X-TIMESTAMP");
            
            // 如果没有APP ID、签名和时间戳，直接继续执行过滤器链，让后续过滤器处理
            if (appId == null || signature == null || timestamp == null) {
                // 继续执行过滤器链，让后续过滤器（如Spring Security）处理认证
                filterChain.doFilter(wrappedRequest, response);
                return;
            }
            
            // 验证租户是否存在且状态正常
            Tenant tenant = tenantService.getByAppId(appId);
            if (tenant == null || !"approved".equals(tenant.getStatus())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"code\":10001,\"message\":\"APP ID不存在或已禁用\"}");
                return;
            }
            
            // 获取请求方法
            String method = wrappedRequest.getMethod().toUpperCase();
            
            // 解析请求参数
            Map<String, String> params = new HashMap<>();
            
            // 解析URL查询参数
            String queryString = wrappedRequest.getQueryString();
            if (StringUtils.hasText(queryString)) {
                MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUriString("?" + queryString).build().getQueryParams();
                for (Map.Entry<String, java.util.List<String>> entry : queryParams.entrySet()) {
                    // 只取第一个值
                    if (!entry.getValue().isEmpty()) {
                        params.put(entry.getKey(), entry.getValue().get(0));
                    }
                }
            }
            
            // 对于POST/PUT请求，直接使用请求body的原始字符串作为签名的一部分
            if (("POST".equals(method) || "PUT".equals(method))) {
                try {
                    // 读取请求body
                    String body = wrappedRequest.getBody();
                    
                    // 如果body不为空，直接将其作为一个整体参数加入到params中
                    if (StringUtils.hasText(body)) {
                        params.put("body", body);
                    }
                } catch (Exception e) {
                    logger.error("解析请求body失败: " + e.getMessage(), e);
                }
            }
            
            // 验证签名
            boolean isValid = SignatureUtil.verifySignature(tenant.getSecretKey(), params, timestamp, signature);
            if (!isValid) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"code\":10002,\"message\":\"签名验证失败\"}");
                return;
            }
            
            // 将租户信息存储到上下文
            TenantContext.setTenantId(tenant.getId());
            TenantContext.setAppId(appId);
        }
        
        try {
            // 继续执行过滤器链
            filterChain.doFilter(wrappedRequest, response);
        } finally {
            // 不要清除上下文信息，让AOP在请求结束时处理
            // TenantContext.clear();
        }
    }
}