package com.itheima.activiti.service;

import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;

import java.util.List;
import java.util.Map;

/**
 * 任务服务接口
 */
public interface TaskService {
    
    /**
     * 根据任务ID获取任务
     */
    Task getTaskById(String taskId);
    
    /**
     * 根据任务ID获取任务变量
     */
    Map<String, Object> getTaskVariables(String taskId);
    
    /**
     * 根据办理人查询任务
     */
    List<Task> findTasksByAssignee(String assignee);
    
    /**
     * 根据流程实例ID查询任务
     */
    List<Task> findTasksByProcessInstanceId(String processInstanceId);
    
    /**
     * 查询所有任务
     */
    List<Task> findAllTasks();
    
    /**
     * 完成任务
     */
    boolean completeTask(String taskId, Map<String, Object> variables);
    
    /**
     * 认领任务
     */
    boolean claimTask(String taskId, String assignee);
    
    /**
     * 取消认领任务
     */
    boolean unclaimTask(String taskId);
    
    /**
     * 取消认领任务（兼容方法名）
     */
    void unclaim(String taskId);
    
    /**
     * 委派任务
     */
    boolean delegateTask(String taskId, String assignee);
    
    /**
     * 根据办理人获取任务列表（分页）
     */
    List<Map<String, Object>> getTaskListByAssignee(String assignee, int page, int pageSize);
    
    /**
     * 根据流程实例ID获取任务列表
     */
    List<Map<String, Object>> getTaskListByProcessInstanceId(String processInstanceId);
    
    /**
     * 获取任务详情
     */
    Map<String, Object> getTaskDetail(String taskId);
    
    /**
     * 添加评论
     */
    void addComment(String taskId, String processInstanceId, String message);
    
    /**
     * 获取任务评论
     */
    List<Comment> getTaskComments(String taskId);
    
    /**
     * 更新任务办理人
     */
    boolean updateTaskAssignee(String taskId, String assignee);
    
    /**
     * 获取所有任务（分页，返回Map）
     */
    Map<String, Object> getAllTasks(int page, int pageSize);
    
    /**
     * 创建任务查询对象
     */
    TaskQuery createTaskQuery();
    
    /**
     * 查询已完成任务列表（分页）
     */
    Map<String, Object> getCompletedTasks(String assignee, String taskName, int page, int pageSize);
    
    /**
     * 获取用户任务统计信息
     */
    Map<String, Object> getUserTaskStatistics(String assignee);
}