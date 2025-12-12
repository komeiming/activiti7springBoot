package com.itheima.activiti.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Configuration
public class DatabaseSchemaUpdater {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSchemaUpdater.class);

    @Bean
    public ApplicationRunner schemaUpdater(DataSource dataSource) {
        return args -> {
            logger.info("Updating database schema...");
            
            // 检查并添加新列
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                
                // 检查manager_id列是否存在
                ResultSet rs = conn.getMetaData().getColumns(null, null, "EMPLOYEE_ONBOARD", "MANAGER_ID");
                if (!rs.next()) {
                    stmt.execute("ALTER TABLE employee_onboard ADD COLUMN manager_id VARCHAR(255)");
                    logger.info("Added manager_id column");
                }
                
                // 检查hr_id列是否存在
                rs = conn.getMetaData().getColumns(null, null, "EMPLOYEE_ONBOARD", "HR_ID");
                if (!rs.next()) {
                    stmt.execute("ALTER TABLE employee_onboard ADD COLUMN hr_id VARCHAR(255)");
                    logger.info("Added hr_id column");
                }
                
                // 检查hr_name列是否存在
                rs = conn.getMetaData().getColumns(null, null, "EMPLOYEE_ONBOARD", "HR_NAME");
                if (!rs.next()) {
                    stmt.execute("ALTER TABLE employee_onboard ADD COLUMN hr_name VARCHAR(255)");
                    logger.info("Added hr_name column");
                }
                
                // 检查API调用日志表是否存在
                rs = conn.getMetaData().getTables(null, null, "API_CALL_LOG", null);
                if (!rs.next()) {
                    // 创建API调用日志表
                    stmt.execute("CREATE TABLE api_call_log (\n" +
                            "    id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '日志ID',\n" +
                            "    app_id VARCHAR(36) COMMENT '应用ID',\n" +
                            "    tenant_id VARCHAR(36) COMMENT '租户ID',\n" +
                            "    api_path VARCHAR(255) NOT NULL COMMENT 'API路径',\n" +
                            "    request_method VARCHAR(10) NOT NULL COMMENT '请求方法',\n" +
                            "    request_params TEXT COMMENT '请求参数',\n" +
                            "    response_data TEXT COMMENT '响应数据',\n" +
                            "    response_time BIGINT NOT NULL COMMENT '响应时间（毫秒）',\n" +
                            "    success BOOLEAN NOT NULL COMMENT '是否成功',\n" +
                            "    error_code VARCHAR(20) COMMENT '错误代码',\n" +
                            "    error_message TEXT COMMENT '错误信息',\n" +
                            "    client_ip VARCHAR(50) COMMENT '客户端IP',\n" +
                            "    call_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '调用时间'\n" +
                            ")");
                    logger.info("Created api_call_log table");
                }
                
                // 检查工作流模板表是否存在
                rs = conn.getMetaData().getTables(null, null, "WORKFLOW_TEMPLATE", null);
                if (!rs.next()) {
                    // 创建工作流模板表
                    stmt.execute("CREATE TABLE workflow_template (\n" +
                            "    id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '模板ID',\n" +
                            "    tenant_id VARCHAR(36) NOT NULL COMMENT '租户ID',\n" +
                            "    name VARCHAR(255) NOT NULL COMMENT '模板名称',\n" +
                            "    description TEXT COMMENT '模板描述',\n" +
                            "    process_json TEXT NOT NULL COMMENT '流程JSON定义',\n" +
                            "    status VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '模板状态：draft(未发布), published(已发布), offline(已下架)',\n" +
                            "    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',\n" +
                            "    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',\n" +
                            "    created_by VARCHAR(36) COMMENT '创建人',\n" +
                            "    updated_by VARCHAR(36) COMMENT '更新人'\n" +
                            ")");
                    logger.info("Created workflow_template table");
                }
                
                // 检查工作流实例表是否存在
                rs = conn.getMetaData().getTables(null, null, "WORKFLOW_INSTANCE", null);
                if (!rs.next()) {
                    // 创建工作流实例表
                    stmt.execute("CREATE TABLE workflow_instance (\n" +
                            "    id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '实例ID',\n" +
                            "    template_id VARCHAR(36) NOT NULL COMMENT '模板ID',\n" +
                            "    tenant_id VARCHAR(36) NOT NULL COMMENT '租户ID',\n" +
                            "    app_id VARCHAR(36) COMMENT '应用ID',\n" +
                            "    status VARCHAR(20) NOT NULL DEFAULT 'running' COMMENT '实例状态：running(运行中), completed(已完成), terminated(已终止), suspended(已暂停)',\n" +
                            "    current_node VARCHAR(36) COMMENT '当前节点ID',\n" +
                            "    variables TEXT COMMENT '流程变量',\n" +
                            "    callback_url VARCHAR(255) COMMENT '回调地址',\n" +
                            "    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '启动时间',\n" +
                            "    completed_at TIMESTAMP NULL COMMENT '完成时间',\n" +
                            "    created_by VARCHAR(36) COMMENT '创建人'\n" +
                            ")");
                    logger.info("Created workflow_instance table");
                }
                
                // 检查工作流执行日志表是否存在
                rs = conn.getMetaData().getTables(null, null, "WORKFLOW_EXECUTION_LOG", null);
                if (!rs.next()) {
                    // 创建工作流执行日志表
                    stmt.execute("CREATE TABLE workflow_execution_log (\n" +
                            "    id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '日志ID',\n" +
                            "    instance_id VARCHAR(36) NOT NULL COMMENT '实例ID',\n" +
                            "    tenant_id VARCHAR(36) NOT NULL COMMENT '租户ID',\n" +
                            "    node_id VARCHAR(36) NOT NULL COMMENT '节点ID',\n" +
                            "    node_name VARCHAR(255) NOT NULL COMMENT '节点名称',\n" +
                            "    operation_type VARCHAR(20) NOT NULL COMMENT '操作类型：start(开始), complete(完成), skip(跳过), error(错误)',\n" +
                            "    operation_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',\n" +
                            "    operation_result TEXT COMMENT '操作结果',\n" +
                            "    error_message TEXT COMMENT '错误信息',\n" +
                            "    operator VARCHAR(36) COMMENT '操作人'\n" +
                            ")");
                    logger.info("Created workflow_execution_log table");
                }
                
                // 检查tenant表的role列是否存在
                rs = conn.getMetaData().getColumns(null, null, "TENANT", "ROLE");
                if (!rs.next()) {
                    stmt.execute("ALTER TABLE tenant ADD COLUMN role VARCHAR(64) NOT NULL DEFAULT 'ROLE_USER' COMMENT '租户角色'");
                    logger.info("Added role column to tenant table");
                }
                
                logger.info("Database schema updated successfully");
            } catch (Exception e) {
                logger.error("Failed to update database schema", e);
            }
        };
    }
}