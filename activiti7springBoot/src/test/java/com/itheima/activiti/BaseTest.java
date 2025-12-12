package com.itheima.activiti;

import com.itheima.activiti.Activiti7DemoApplication;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.test.ActivitiRule;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * 测试基类
 */
@SpringBootTest(classes = Activiti7DemoApplication.class)
public abstract class BaseTest {

    @Autowired
    protected WebApplicationContext wac;

    @Autowired
    protected DataSource dataSource;

    @Autowired
    protected ProcessEngine processEngine;

    @Autowired
    protected RepositoryService repositoryService;

    @Autowired
    protected RuntimeService runtimeService;

    @Autowired
    protected TaskService taskService;

    @Autowired
    protected HistoryService historyService;

    protected MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        // 初始化MockMvc
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    /**
     * 清理测试数据
     */
    protected void cleanTestData() {
        // 删除所有流程实例
        runtimeService.createProcessInstanceQuery().list().forEach(processInstance -> {
            runtimeService.deleteProcessInstance(processInstance.getId(), "测试清理");
        });
        
        // 删除所有历史数据
        for (String processInstanceId : historyService.createHistoricProcessInstanceQuery().list()
                .stream().map(hi -> hi.getId()).toArray(String[]::new)) {
            historyService.deleteHistoricProcessInstance(processInstanceId);
        }
        
        // 删除所有任务
        taskService.createTaskQuery().list().forEach(task -> {
            taskService.deleteTask(task.getId(), true);
        });
    }

    /**
     * 模拟用户认证
     */
    protected void mockUserAuthentication(String username, String... authorities) {
        // 可以在这里实现Spring Security的用户认证模拟
    }

    /**
     * 等待异步任务完成
     */
    protected void waitForAsyncTasks(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}