package com.itheima.activiti.utils;

import org.activiti.engine.impl.identity.Authentication;

/**
 * Activiti 安全工具类，用于在测试中设置当前用户
 * 简化版本，直接使用Activiti的Authentication类
 */
public class SecurityUtil {

    /**
     * 设置当前用户身份
     * @param username 用户名
     */
    public static void logInAs(String username) {
        // 使用Activiti内置的Authentication类设置当前用户
        Authentication.setAuthenticatedUserId(username);
    }

    /**
     * 获取当前登录用户名
     * @return 当前用户名
     */
    public static String getCurrentUsername() {
        return Authentication.getAuthenticatedUserId();
    }

    /**
     * 清除当前用户身份
     */
    public static void clearAuthentication() {
        Authentication.setAuthenticatedUserId(null);
    }
}
