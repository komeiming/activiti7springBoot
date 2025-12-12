package com.itheima.activiti.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin用户控制器，提供管理员特有的功能
 */
@RestController
public class AdminController {

    /**
     * 管理员查看所有任务（模拟数据）
     * 这是一个模拟的API端点，返回虚拟任务数据
     */
    @RequestMapping("/admin/all-tasks")
    public Map<String, Object> getAllTasks() {
        // 创建模拟任务数据
        List<Map<String, Object>> tasks = new ArrayList<>();
        
        // 添加几个模拟任务
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> task = new HashMap<>();
            task.put("id", "task" + i);
            task.put("name", "审批任务 #" + i);
            task.put("description", "这是一个模拟的审批任务，用于测试前端展示");
            task.put("assignee", "admin");
            task.put("created", new Date());
            task.put("priority", 50);
            task.put("dueDate", new Date(System.currentTimeMillis() + 86400000 * i)); // 1天后、2天后等
            
            tasks.add(task);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", tasks.size());
        result.put("tasks", tasks);
        result.put("message", "获取任务列表成功");
        
        return result;
    }
    
    /**
     * 获取流程定义列表（模拟数据）
     */
    @RequestMapping("/admin/process-definitions")
    public Map<String, Object> getProcessDefinitions() {
        List<Map<String, Object>> definitions = new ArrayList<>();
        
        Map<String, Object> def1 = new HashMap<>();
        def1.put("id", "team01:1:123456");
        def1.put("name", "团队审批流程");
        def1.put("key", "team01");
        def1.put("version", 1);
        def1.put("deploymentId", "deployment123");
        
        definitions.add(def1);
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", definitions.size());
        result.put("definitions", definitions);
        result.put("message", "获取流程定义列表成功");
        
        return result;
    }
    
    /**
     * 启动流程（模拟数据）
     */
    @RequestMapping("/admin/start-process")
    public Map<String, Object> startProcess() {
        Map<String, Object> result = new HashMap<>();
        result.put("processInstanceId", "proc" + System.currentTimeMillis());
        result.put("processDefinitionId", "team01:1:123456");
        result.put("message", "流程启动成功");
        
        return result;
    }
}
