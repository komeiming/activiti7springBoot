package com.itheima.activiti.service;

import com.itheima.activiti.BaseTest;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务服务测试
 */
public class TaskServiceTest extends BaseTest {

    @Autowired
    private TaskService taskService;
    
    @Autowired
    private ProcessDefinitionService processDefinitionService;

    private String processInstanceId;
    private String taskId;
    private String assignee = "zhangsan"; // 测试用户

    @BeforeEach
    public void initData() {
        // 清理测试数据
        cleanTestData();
        
        try {
            // 部署测试流程
            String bpmnResource = "processes/employee-onboarding.bpmn20.xml";
            repositoryService.createDeployment()
                    .addClasspathResource(bpmnResource)
                    .name("测试部署")
                    .deploy();
            
            // 获取流程定义ID
            String processDefinitionId = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionKey("employee-onboarding")
                    .singleResult()
                    .getId();
            
            // 启动流程实例
            Map<String, Object> variables = new HashMap<>();
            variables.put("employeeName", "测试员工");
            variables.put("department", "测试部门");
            variables.put("manager", assignee); // 设置第一个任务的处理人
            
            // 修复返回值类型
            Object result = processDefinitionService.startProcessInstance(processDefinitionId, "TEST-123", variables);
            if (result instanceof Map) {
                processInstanceId = (String) ((Map<?, ?>) result).get("processInstanceId");
            }
            
            // 获取第一个任务
            List<Task> tasks = taskService.createTaskQuery()
                    .processInstanceId(processInstanceId)
                    .list();
            
            if (!tasks.isEmpty()) {
                taskId = tasks.get(0).getId();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetTaskListByAssignee() {
        // 根据办理人获取任务列表
        List<Map<String, Object>> tasks = taskService.getTaskListByAssignee(assignee, 1, 10);
        
        Assertions.assertNotNull(tasks, "任务列表不应为null");
        Assertions.assertFalse(tasks.isEmpty(), "任务列表不应为空");
        
        // 验证任务的办理人
        boolean found = false;
        for (Map<String, Object> task : tasks) {
            if (task.get("assignee").equals(assignee)) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found, "应找到指定办理人的任务");
    }

    @Test
    public void testGetTaskByProcessInstanceId() {
        // 根据流程实例ID获取任务
        List<Map<String, Object>> tasks = taskService.getTaskListByProcessInstanceId(processInstanceId);
        
        Assertions.assertNotNull(tasks, "任务列表不应为null");
        Assertions.assertFalse(tasks.isEmpty(), "任务列表不应为空");
        
        // 验证任务的流程实例ID
        boolean found = false;
        for (Map<String, Object> task : tasks) {
            if (task.get("processInstanceId").equals(processInstanceId)) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found, "应找到指定流程实例的任务");
    }

    @Test
    public void testGetTaskDetail() {
        // 获取任务详情
        Map<String, Object> taskDetail = taskService.getTaskDetail(taskId);
        
        Assertions.assertNotNull(taskDetail, "任务详情不应为null");
        Assertions.assertEquals(taskId, taskDetail.get("id"), "任务ID应匹配");
    }

    @Test
    public void testCompleteTask() {
        // 完成任务
        Map<String, Object> variables = new HashMap<>();
        variables.put("approved", true);
        variables.put("hr", "lisi"); // 设置下一个任务的处理人
        
        boolean completed = taskService.completeTask(taskId, variables);
        Assertions.assertTrue(completed, "完成任务操作应成功");
        
        // 验证原任务已不存在
        Task completedTask = taskService.createTaskQuery().taskId(taskId).singleResult();
        Assertions.assertNull(completedTask, "任务应已完成");
        
        // 验证流程继续执行，产生了新任务
        List<Task> newTasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
        Assertions.assertFalse(newTasks.isEmpty(), "应产生新任务");
    }

    @Test
    public void testClaimAndUnclaimTask() {
        // 先解除任务的办理人（如果有）
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task.getAssignee() != null) {
            taskService.unclaim(taskId);
        }
        
        // 认领任务
        String newAssignee = "wangwu";
        boolean claimed = taskService.claimTask(taskId, newAssignee);
        Assertions.assertTrue(claimed, "认领任务操作应成功");
        
        // 验证任务的办理人已更新
        Task claimedTask = taskService.createTaskQuery().taskId(taskId).singleResult();
        Assertions.assertEquals(newAssignee, claimedTask.getAssignee(), "任务办理人应更新");
        
        // 取消认领任务
        boolean unclaimed = taskService.unclaimTask(taskId);
        Assertions.assertTrue(unclaimed, "取消认领任务操作应成功");
        
        // 验证任务的办理人已为空
        Task unclaimedTask = taskService.createTaskQuery().taskId(taskId).singleResult();
        Assertions.assertNull(unclaimedTask.getAssignee(), "任务办理人应为空");
    }

    @Test
    public void testDelegateTask() {
        String delegatee = "zhaoliu";
        boolean delegated = taskService.delegateTask(taskId, delegatee);
        Assertions.assertTrue(delegated, "委派任务操作应成功");
        
        // 验证是否创建了委派任务
        // Task delegatedTask = taskService.createTaskQuery().taskDelegationState(org.activiti.engine.task.TaskDelegationState.PENDING).taskId(taskId).singleResult();
        // Assertions.assertNotNull(delegatedTask, "委派任务应存在");
    }

    @Test
    public void testAddComment() {
        // 添加评论
        String comment = "这是一条测试评论";
        String processInstanceId = taskService.createTaskQuery().taskId(taskId).singleResult().getProcessInstanceId();
        
        taskService.addComment(taskId, processInstanceId, comment);
        
        // 验证评论是否添加成功
        List<Comment> comments = taskService.getTaskComments(taskId);
        Assertions.assertFalse(comments.isEmpty(), "评论列表不应为空");
        
        boolean found = false;
        for (Comment c : comments) {
            if (c.getFullMessage().equals(comment)) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found, "应找到添加的评论");
    }

    @Test
    public void testUpdateTaskAssignee() {
        // 更新任务办理人
        String newAssignee = "sunqi";
        boolean updated = taskService.updateTaskAssignee(taskId, newAssignee);
        Assertions.assertTrue(updated, "更新任务办理人操作应成功");
        
        // 验证任务办理人已更新
        Task updatedTask = taskService.createTaskQuery().taskId(taskId).singleResult();
        Assertions.assertEquals(newAssignee, updatedTask.getAssignee(), "任务办理人应更新");
    }

    @Test
    public void testGetAllTasks() {
        // 获取所有任务
        Map<String, Object> tasks = taskService.getAllTasks(1, 10);
        
        Assertions.assertNotNull(tasks, "任务列表不应为null");
        Assertions.assertTrue(tasks.containsKey("list"), "应返回任务列表");
        
        List<?> taskList = (List<?>) tasks.get("list");
        Assertions.assertFalse(taskList.isEmpty(), "任务列表不应为空");
    }
}