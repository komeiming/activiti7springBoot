package com.itheima.activiti.config;

import com.itheima.activiti.common.TenantContext;
import com.itheima.activiti.entity.ApiCallLog;
import com.itheima.activiti.service.ApiCallLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * API调用日志过滤器，用于自动记录所有API调用
 */
@Component
public class ApiCallLogFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiCallLogFilter.class);
    
    @Autowired
    private ApiCallLogService apiCallLogService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 只记录API路径
        String requestUri = request.getRequestURI();
        // 排除注册请求和管理员API请求，因为注册时还没有租户信息，管理员API可能不需要记录
        if (requestUri.startsWith("/api/") && !requestUri.startsWith("/api/auth/login") && !requestUri.equals("/api/v1/tenant/register") && !requestUri.startsWith("/api/admin/")) {
            
            // 包装请求和响应，以便后续读取内容
            ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
            ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
            
            long startTime = System.currentTimeMillis();
            
            try {
                // 继续执行过滤器链
                filterChain.doFilter(wrappedRequest, wrappedResponse);
            } finally {
                long endTime = System.currentTimeMillis();
                long responseTime = endTime - startTime;
                
                // 记录API调用日志
                try {
                    ApiCallLog apiCallLog = new ApiCallLog();
                    apiCallLog.setId(UUID.randomUUID().toString());
                    apiCallLog.setAppId(TenantContext.getAppId());
                    apiCallLog.setTenantId(TenantContext.getTenantId());
                    apiCallLog.setApiPath(requestUri);
                    apiCallLog.setRequestMethod(request.getMethod());
                    apiCallLog.setRequestParams(getRequestPayload(wrappedRequest));
                    apiCallLog.setResponseData(getResponsePayload(wrappedResponse));
                    apiCallLog.setResponseTime(responseTime);
                    apiCallLog.setSuccess(response.getStatus() >= 200 && response.getStatus() < 300);
                    apiCallLog.setClientIp(getClientIp(request));
                    apiCallLog.setCallTime(LocalDateTime.now());
                    
                    // 保存日志
                    apiCallLogService.saveApiCallLog(apiCallLog);
                    
                    // 记录调试日志
                    logger.debug("API调用日志: {}, 响应时间: {}ms", requestUri, responseTime);
                    
                } catch (Exception e) {
                    logger.error("记录API调用日志失败:", e);
                }
                
                // 确保响应被刷新
                wrappedResponse.copyBodyToResponse();
            }
        } else {
            // 非API请求或登录请求或注册请求，直接放行
            filterChain.doFilter(request, response);
        }
    }
    
    /**
     * 获取请求体内容
     */
    private String getRequestPayload(ContentCachingRequestWrapper request) {
        ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
        byte[] buf = wrapper.getContentAsByteArray();
        if (buf.length > 0) {
            try {
                // 限制请求体大小，避免日志过大
                int maxLength = 2048;
                if (buf.length > maxLength) {
                    return new String(buf, 0, maxLength, wrapper.getCharacterEncoding()) + "...[truncated]";
                }
                return new String(buf, wrapper.getCharacterEncoding());
            } catch (UnsupportedEncodingException e) {
                logger.error("获取请求体失败:", e);
                return "[无法解析请求体]";
            }
        }
        return "";
    }
    
    /**
     * 获取响应体内容
     */
    private String getResponsePayload(ContentCachingResponseWrapper response) {
        ContentCachingResponseWrapper wrapper = (ContentCachingResponseWrapper) response;
        byte[] buf = wrapper.getContentAsByteArray();
        if (buf.length > 0) {
            try {
                // 限制响应体大小，避免日志过大
                int maxLength = 2048;
                if (buf.length > maxLength) {
                    return new String(buf, 0, maxLength, wrapper.getCharacterEncoding()) + "...[truncated]";
                }
                return new String(buf, wrapper.getCharacterEncoding());
            } catch (UnsupportedEncodingException e) {
                logger.error("获取响应体失败:", e);
                return "[无法解析响应体]";
            }
        }
        return "";
    }
    
    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}