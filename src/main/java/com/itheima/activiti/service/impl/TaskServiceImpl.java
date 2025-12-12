package com.itheima.activiti.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itheima.activiti.service.TaskService;

@Service
public class TaskServiceImpl implements TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

    @Autowired
    private org.activiti.engine.TaskService activitiTaskService;
    // HistoryService已移除，使用ProcessEngine获取历史服务

    public Task getTaskById(String taskId) {
        return activitiTaskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();
    }

    public Map<String, Object> getTaskVariables(String taskId) {
        return activitiTaskService.getVariables(taskId);
    }

    public List<Task> findTasksByAssignee(String assignee) {
        return activitiTaskService.createTaskQuery()
                .taskAssignee(assignee)
                .list();
    }

    public List<Task> findTasksByProcessInstanceId(String processInstanceId) {
        return activitiTaskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .list();
    }

    public List<Task> findAllTasks() {
        return activitiTaskService.createTaskQuery()
                .list();
    }

    public boolean completeTask(String taskId, Map<String, Object> variables) {
        try {
            // 变量映射：将前端提交的approvalAction转换为BPMN文件中使用的approved布尔变量
            if (variables != null && variables.containsKey("approvalAction")) {
                String approvalAction = (String) variables.get("approvalAction");
                // 根据approvalAction值设置approved变量
                boolean approved = "approve".equals(approvalAction);
                variables.put("approved", approved);
                logger.info("变量映射完成：approvalAction={} -> approved={}", approvalAction, approved);
            }
            
            activitiTaskService.complete(taskId, variables);
            logger.info("任务已完成，任务ID: {}", taskId);
            return true;
        } catch (Exception e) {
            logger.error("完成任务失败: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean claimTask(String taskId, String assignee) {
        try {
            activitiTaskService.claim(taskId, assignee);
            logger.info("任务已认领，任务ID: {}, 认领人: {}", taskId, assignee);
            return true;
        } catch (Exception e) {
            logger.error("认领任务失败: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean unclaimTask(String taskId) {
        try {
            activitiTaskService.unclaim(taskId);
            logger.info("任务已取消认领，任务ID: {}", taskId);
            return true;
        } catch (Exception e) {
            logger.error("取消认领任务失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    public void unclaim(String taskId) {
        activitiTaskService.unclaim(taskId);
    }

    public boolean delegateTask(String taskId, String assignee) {
        try {
            activitiTaskService.delegateTask(taskId, assignee);
            logger.info("任务已委派，任务ID: {}, 受让人: {}", taskId, assignee);
            return true;
        } catch (Exception e) {
            logger.error("委派任务失败: {}", e.getMessage(), e);
            return false;
        }
    }

    public List<Map<String, Object>> getTaskListByAssignee(String assignee, int page, int pageSize) {
        List<Task> tasks = activitiTaskService.createTaskQuery()
                .taskAssignee(assignee)
                .listPage((page - 1) * pageSize, pageSize);
        return convertTasksToMaps(tasks);
    }

    public List<Map<String, Object>> getTaskListByProcessInstanceId(String processInstanceId) {
        List<Task> tasks = activitiTaskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .list();
        return convertTasksToMaps(tasks);
    }

    public Map<String, Object> getTaskDetail(String taskId) {
        Task task = getTaskById(taskId);
        if (task == null) return null;
        
        Map<String, Object> taskMap = new HashMap<>();
        taskMap.put("id", task.getId());
        taskMap.put("name", task.getName());
        taskMap.put("description", task.getDescription());
        taskMap.put("assignee", task.getAssignee());
        taskMap.put("owner", task.getOwner());
        taskMap.put("processInstanceId", task.getProcessInstanceId());
        taskMap.put("processDefinitionId", task.getProcessDefinitionId());
        taskMap.put("createTime", task.getCreateTime());
        taskMap.put("dueDate", task.getDueDate());
        taskMap.put("priority", task.getPriority());
        taskMap.put("variables", getTaskVariables(taskId));
        
        return taskMap;
    }

    public void addComment(String taskId, String processInstanceId, String message) {
        activitiTaskService.addComment(taskId, processInstanceId, message);
    }

    public List<Comment> getTaskComments(String taskId) {
        return activitiTaskService.getTaskComments(taskId);
    }

    public boolean updateTaskAssignee(String taskId, String assignee) {
        try {
            activitiTaskService.setAssignee(taskId, assignee);
            return true;
        } catch (Exception e) {
            logger.error("更新任务办理人失败: {}", e.getMessage());
            return false;
        }
    }

    public Map<String, Object> getAllTasks(int page, int pageSize) {
        long totalCount = activitiTaskService.createTaskQuery().count();
        List<Task> tasks = activitiTaskService.createTaskQuery()
                .listPage((page - 1) * pageSize, pageSize);
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", convertTasksToMaps(tasks));
        result.put("total", totalCount);
        result.put("page", page);
        result.put("size", pageSize);
        
        return result;
    }
    
    public org.activiti.engine.task.TaskQuery createTaskQuery() {
        return activitiTaskService.createTaskQuery();
    }
    
    @Override
    public Map<String, Object> getCompletedTasks(String assignee, String taskName, int page, int pageSize) {
        try {
            logger.info("查询已完成任务列表，处理人: {}, 任务名称: {}, 页码: {}, 每页数量: {}", 
                        assignee, taskName, page, pageSize);
            
            // 使用ProcessEngine获取HistoryService
            ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
            org.activiti.engine.HistoryService historyService = processEngine.getHistoryService();
            
            // 创建历史任务查询
            org.activiti.engine.history.HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(assignee)
                .finished() // 仅查询已完成的任务
                .orderByHistoricTaskInstanceEndTime().desc(); // 按完成时间倒序
            
            // 添加任务名称过滤条件
            if (taskName != null && !taskName.isEmpty()) {
                query.taskNameLike("%" + taskName + "%");
            }
            
            // 获取总数
            long totalCount = query.count();
            
            // 分页查询
            List<HistoricTaskInstance> completedTasks = query
                .listPage((page - 1) * pageSize, pageSize);
            
            // 转换为Map列表
            List<Map<String, Object>> taskMaps = new ArrayList<>();
            for (HistoricTaskInstance task : completedTasks) {
                Map<String, Object> taskMap = new HashMap<>();
                taskMap.put("id", task.getId());
                taskMap.put("name", task.getName());
                taskMap.put("description", task.getDescription());
                taskMap.put("assignee", task.getAssignee());
                taskMap.put("createTime", task.getCreateTime());
                taskMap.put("claimTime", task.getClaimTime());
                taskMap.put("endTime", task.getEndTime());
                taskMap.put("durationInMillis", task.getDurationInMillis());
                taskMap.put("processInstanceId", task.getProcessInstanceId());
                taskMap.put("processDefinitionId", task.getProcessDefinitionId());
                taskMap.put("taskDefinitionKey", task.getTaskDefinitionKey());
                taskMap.put("status", "completed");
                
                // 获取任务变量（从历史变量中查询）
                Map<String, Object> processVariables = new HashMap<>();
                try {
                    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                            .processInstanceId(task.getProcessInstanceId())
                            .singleResult();
                    if (historicProcessInstance != null) {
                        List<HistoricVariableInstance> variableInstances = historyService
                                .createHistoricVariableInstanceQuery()
                                .processInstanceId(task.getProcessInstanceId())
                                .list();
                        for (HistoricVariableInstance variableInstance : variableInstances) {
                            processVariables.put(variableInstance.getVariableName(), variableInstance.getValue());
                        }
                    }
                } catch (Exception e) {
                    logger.error("获取历史流程变量失败: " + task.getProcessInstanceId(), e);
                }
                taskMap.put("variables", processVariables);
                
                taskMaps.add(taskMap);
            }
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("data", taskMaps);
            result.put("total", totalCount);
            result.put("page", page);
            result.put("size", pageSize);
            
            logger.info("查询已完成任务成功，共找到 {} 条记录", totalCount);
            return result;
        } catch (Exception e) {
            logger.error("查询已完成任务失败: {}", e.getMessage(), e);
            throw new RuntimeException("查询已完成任务失败", e);
        }
    }
    
    // 辅助方法：将Task列表转换为Map列表
    private List<Map<String, Object>> convertTasksToMaps(List<Task> tasks) {
        List<Map<String, Object>> result = new ArrayList<>();
        // 使用ProcessEngine获取HistoryService
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        org.activiti.engine.HistoryService historyService = processEngine.getHistoryService();
        
        for (Task task : tasks) {
            Map<String, Object> taskMap = new HashMap<>();
            taskMap.put("id", task.getId());
            taskMap.put("name", task.getName());
            taskMap.put("description", task.getDescription());
            taskMap.put("assignee", task.getAssignee());
            taskMap.put("owner", task.getOwner());
            taskMap.put("createTime", task.getCreateTime());
            taskMap.put("dueDate", task.getDueDate());
            taskMap.put("priority", task.getPriority());
            taskMap.put("executionId", task.getExecutionId());
            taskMap.put("processInstanceId", task.getProcessInstanceId());
            taskMap.put("processDefinitionId", task.getProcessDefinitionId());
            taskMap.put("taskDefinitionKey", task.getTaskDefinitionKey());
            
            // 获取流程变量
            try {
                List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery()
                        .processInstanceId(task.getProcessInstanceId())
                        .list();
                
                if (variables != null && !variables.isEmpty()) {
                    Map<String, Object> processVars = new HashMap<>();
                    for (HistoricVariableInstance var : variables) {
                        processVars.put(var.getVariableName(), var.getValue());
                    }
                    taskMap.put("processVariables", processVars);
                }
            } catch (Exception e) {
                logger.error("获取流程变量时出错: {}", e.getMessage());
            }
            
            result.add(taskMap);
        }
        return result;
    }
}