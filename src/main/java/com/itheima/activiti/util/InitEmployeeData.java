package com.itheima.activiti.util;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 普通员工数据初始化工具类
 * 注意：此类已完成数据初始化，保留仅作为参考
 */
@Component
public class InitEmployeeData implements ApplicationRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 数据已初始化完成，此方法暂时注释
        /*
        System.out.println("开始初始化普通员工数据...");
        
        // 创建员工数据的SQL语句
        StringBuilder insertUsersSql = new StringBuilder();
        insertUsersSql.append("INSERT INTO users (id, password, last_login_date, status) VALUES ");
        insertUsersSql.append("('zhangsan', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', NOW(), 1),");
        insertUsersSql.append("('lisi', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', NOW(), 1),");
        insertUsersSql.append("('wangwu', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', NOW(), 1),");
        insertUsersSql.append("('zhaoliu', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', NOW(), 1),");
        insertUsersSql.append("('qianqi', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', NOW(), 1),");
        insertUsersSql.append("('sunba', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', NOW(), 1),");
        insertUsersSql.append("('zhoujiu', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', NOW(), 1),");
        insertUsersSql.append("('wushi', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', NOW(), 1)");
        insertUsersSql.append(" ON DUPLICATE KEY UPDATE id = id");
        
        // 创建用户角色的SQL语句
        StringBuilder insertUserRolesSql = new StringBuilder();
        insertUserRolesSql.append("INSERT INTO user_roles (user_id, role_id) VALUES ");
        insertUserRolesSql.append("('zhangsan', 'ROLE_USER'),");
        insertUserRolesSql.append("('lisi', 'ROLE_USER'),");
        insertUserRolesSql.append("('wangwu', 'ROLE_USER'),");
        insertUserRolesSql.append("('zhaoliu', 'ROLE_USER'),");
        insertUserRolesSql.append("('qianqi', 'ROLE_USER'),");
        insertUserRolesSql.append("('sunba', 'ROLE_USER'),");
        insertUserRolesSql.append("('zhoujiu', 'ROLE_USER'),");
        insertUserRolesSql.append("('wushi', 'ROLE_USER')");
        insertUserRolesSql.append(" ON DUPLICATE KEY UPDATE user_id = user_id");
        
        try {
            // 执行SQL语句
            jdbcTemplate.execute(insertUsersSql.toString());
            jdbcTemplate.execute(insertUserRolesSql.toString());
            
            System.out.println("普通员工数据初始化成功！");
        } catch (Exception e) {
            System.err.println("初始化数据时出错：" + e.getMessage());
        }
        */
    }
}