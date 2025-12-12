package com.itheima.activiti.util;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 初始化HR用户数据
 * 用于创建或更新hr用户账户
 */
@Component
public class InitHRUser implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(InitHRUser.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("开始初始化HR用户数据...");
        
        try {
            // 首先检查角色表中是否有HR角色
            Integer hrRoleCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_role WHERE code = 'ROLE_HR'", Integer.class);
            
            // 如果没有HR角色，先创建角色
            if (hrRoleCount == 0) {
                logger.info("创建HR角色...");
                jdbcTemplate.update(
                    "INSERT INTO sys_role (id, name, code, description) VALUES (3, 'HR专员', 'ROLE_HR', '人力资源专员')");
            }
            
            // 检查hr用户是否存在
            Integer hrUserCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_user WHERE username = 'hr'", Integer.class);
            
            // 如果hr用户不存在，创建hr用户
            if (hrUserCount == 0) {
                logger.info("创建hr用户...");
                // 密码使用BCrypt加密，明文为123456
                jdbcTemplate.update(
                    "INSERT INTO sys_user (username, password, full_name, email, phone, department, position, status) " +
                    "VALUES ('hr', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', 'HR专员', 'hr@example.com', '13800138001', '人力资源部', 'HR专员', 1)");
            } else {
                logger.info("hr用户已存在，更新用户状态...");
                // 确保用户状态为启用
                jdbcTemplate.update(
                    "UPDATE sys_user SET status = 1 WHERE username = 'hr'");
            }
            
            // 获取hr用户的id
            Long hrUserId = jdbcTemplate.queryForObject(
                "SELECT id FROM sys_user WHERE username = 'hr'", Long.class);
            
            // 检查用户角色关联是否存在
            Integer userRoleCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_user_role WHERE user_id = ? AND role_id = ?", 
                new Object[]{hrUserId, 3}, Integer.class);
            
            // 如果用户角色关联不存在，创建关联
            if (userRoleCount == 0) {
                logger.info("为hr用户分配HR角色...");
                jdbcTemplate.update(
                    "INSERT INTO sys_user_role (user_id, role_id) VALUES (?, ?)", 
                    new Object[]{hrUserId, 3});
            }
            
            logger.info("HR用户数据初始化完成！");
        } catch (Exception e) {
            logger.error("初始化HR用户数据时出错：", e);
        }
    }
}