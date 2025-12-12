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
                
                logger.info("Database schema updated successfully");
            } catch (Exception e) {
                logger.error("Failed to update database schema", e);
            }
        };
    }
}