package com.itheima.activiti.config;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;

/**
 * 自定义权限验证器，用于支持细粒度的权限控制
 */
@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || permission == null) {
            return false;
        }

        // 获取用户的权限集合
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        
        // 转换权限为字符串
        String requiredPermission = permission.toString();
        
        // 检查用户是否拥有所需的权限
        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals(requiredPermission)) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || targetId == null || targetType == null || permission == null) {
            return false;
        }
        
        // 转换权限为字符串
        String requiredPermission = permission.toString();
        
        // 构建资源类型和权限的组合，如 "process:read"
        String combinedPermission = targetType + ":" + requiredPermission;
        
        // 获取用户的权限集合
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        
        // 检查用户是否拥有所需的权限
        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals(combinedPermission) || 
                authority.getAuthority().equals("*") || // 超级管理员权限
                authority.getAuthority().equals(targetType + ":*")) { // 某类资源的所有权限
                return true;
            }
        }
        
        return false;
    }
}