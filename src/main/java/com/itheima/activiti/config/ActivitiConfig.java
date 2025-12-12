package com.itheima.activiti.config;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
public class ActivitiConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public SpringProcessEngineConfiguration processEngineConfiguration() throws IOException {
        SpringProcessEngineConfiguration config = new SpringProcessEngineConfiguration();
        config.setDataSource(dataSource);
        config.setTransactionManager(transactionManager());
        config.setDatabaseSchemaUpdate("true");
        config.setDbHistoryUsed(true);
        // 设置历史级别为full，确保记录完整的流程历史数据
        config.setHistoryLevel(org.activiti.engine.impl.history.HistoryLevel.FULL);
        return config;
    }

    @Bean
    public ProcessEngine processEngine() throws IOException {
        return processEngineConfiguration().buildProcessEngine();
    }

    @Bean
    public RepositoryService repositoryService() throws IOException {
        return processEngine().getRepositoryService();
    }

    @Bean
    public RuntimeService runtimeService() throws IOException {
        return processEngine().getRuntimeService();
    }

    @Bean
    public TaskService taskService() throws IOException {
        return processEngine().getTaskService();
    }
}