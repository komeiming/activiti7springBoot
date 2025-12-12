package com.itheima.activiti.service;

import com.itheima.activiti.dto.EmployeeOnboardRequest;
import com.itheima.activiti.dto.EmployeeOnboardResponse;
import com.itheima.activiti.dto.ProcessTaskInfo;
import com.itheima.activiti.dto.TaskActionRequest;

import java.util.List;
import java.util.Map;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

import com.itheima.activiti.entity.EmployeeOnboard;

/**
 * 员工入职流程服务接口
 */
public interface EmployeeOnboardService {
    
    /**
     * 部署流程定义
     * @return 部署ID
     */
    String deployProcess();
    
    /**
     * 部署员工入职流程
     * @return 部署ID
     */
    String deployOnboardProcess();
    
    /**
     * 部署流程文件
     * @param fileName 文件名
     * @param fileInputStream 文件输入流
     * @return 部署ID
     */
    String deployProcessFile(String fileName, java.io.InputStream fileInputStream);
    
    /**
     * 获取所有部署
     * @return 部署列表
     */
    List<org.activiti.engine.repository.Deployment> getAllDeployments();
    
    /**
     * 根据部署ID获取部署信息
     * @param deploymentId 部署ID
     * @return 部署信息
     */
    org.activiti.engine.repository.Deployment getDeploymentById(String deploymentId);
    
    /**
     * 删除部署
     * @param deploymentId 部署ID
     * @param cascade 是否级联删除相关的流程实例和任务
     */
    void deleteDeployment(String deploymentId, boolean cascade);
    
    /**
     * 获取任务变量
     * @param taskId 任务ID
     * @return 任务变量
     */
    Map<String, Object> getTaskVariables(String taskId);
    
    /**
     * 启动员工入职流程
     * @param variables 流程变量
     * @return 流程实例
     */
    ProcessInstance startProcess(Map<String, Object> variables);
    
    /**
     * 根据流程实例ID查询任务
     * @param processInstanceId 流程实例ID
     * @return 任务列表
     */
    List<Task> findTasksByProcessInstanceId(String processInstanceId);
    
    /**
     * 根据当前用户查询待办任务
     * @param assignee 处理人
     * @return 任务列表
     */
    List<Task> findTasksByAssignee(String assignee);
    
    /**
     * 根据用户组查询待办任务
     * @param candidateGroup 用户组
     * @return 任务列表
     */
    List<Task> findTasksByCandidateGroup(String candidateGroup);
    
    /**
     * 根据任务ID查询任务
     * @param taskId 任务ID
     * @return 任务对象
     */
    Task getTaskById(String taskId);
    
    /**
     * 完成任务
     * @param taskId 任务ID
     * @param variables 任务变量
     */
    void completeTask(String taskId, Map<String, Object> variables);
    
    /**
     * 签收任务
     * @param taskId 任务ID
     * @param assignee 签收人
     */
    void claimTask(String taskId, String assignee);
    
    /**
     * 取消签收任务
     * @param taskId 任务ID
     */
    void unclaimTask(String taskId);
    
    /**
     * 根据流程实例ID查询入职信息
     * @param processInstanceId 流程实例ID
     * @return 入职信息DTO
     */
    EmployeeOnboard findByProcessInstanceId(String processInstanceId);
    
    /**
     * 查询所有入职信息
     * @return 入职信息列表
     */
    List<EmployeeOnboard> findAll();
    
    /**
     * 获取所有任务（扩展方法，供TaskService实现调用）
     * @return 任务列表
     */
    List<Task> findAllTasks();
    
    /**
     * 委派任务
     * @param taskId 任务ID
     * @param assignee 受让人
     */
    void delegateTask(String taskId, String assignee);
    
    /**
     * 将EmployeeOnboard实体转换为响应DTO
     * @param entity 实体对象
     * @return 响应DTO对象
     */
    EmployeeOnboardResponse convertToDTO(EmployeeOnboard entity);
    
    /**
     * 将EmployeeOnboard实体列表转换为响应DTO列表
     * @param entities 实体列表
     * @return 响应DTO列表
     */
    List<EmployeeOnboardResponse> convertToDTOList(List<EmployeeOnboard> entities);
    
    /**
     * 挂起流程实例
     * @param processInstanceId 流程实例ID
     */
    void suspendProcessInstance(String processInstanceId);
    
    /**
     * 激活流程实例
     * @param processInstanceId 流程实例ID
     */
    void activateProcessInstance(String processInstanceId);
    
    // 移除重复的方法定义
    

    
    /**
     * 根据业务主键获取入职详情
     * @param businessKey 业务主键
     * @return 入职详情响应
     */
    EmployeeOnboardResponse getOnboardDetailByBusinessKey(String businessKey);
    
    /**
     * 启动入职流程
     * @param request 入职请求
     * @return 流程实例ID
     */
    String startOnboardProcess(EmployeeOnboardRequest request);
    
    /**
     * 获取部门入职申请
     * @param departmentId 部门ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 入职申请列表
     */
    Map<String, Object> getDepartmentOnboardApplications(String departmentId, int pageNum, int pageSize);
    
    /**
     * 获取入职流程历史
     * @param processInstanceId 流程实例ID
     * @return 历史记录列表
     */
    List<ProcessTaskInfo> getOnboardProcessHistory(String processInstanceId);
    
    /**
     * 取消入职流程
     * @param processInstanceId 流程实例ID
     * @return 是否取消成功
     */
    boolean cancelOnboardProcess(String processInstanceId);
    
    /**
     * 获取部门经理审批任务
     */
    Map<String, Object> getDepartmentManagerTasks(String assignee, int pageNum, int pageSize);
    
    /**
     * 获取HR任务
     */
    Map<String, Object> getHRTasks(String assignee, int pageNum, int pageSize);
    
    /**
     * 获取行政资源准备任务
     */
    Map<String, Object> getAdminResourceTasks(String assignee, int pageNum, int pageSize);
    
    /**
     * 获取培训师任务
     */
    Map<String, Object> getTrainerTasks(String assignee, int pageNum, int pageSize);
    
    /**
     * 部门经理审批入职
     */
    boolean approveOnboardByManager(String taskId, TaskActionRequest request);
    
    /**
     * HR确认入职
     */
    boolean confirmOnboardByHR(String taskId, TaskActionRequest request);
    
    /**
     * 完成资源准备
     */
    boolean completeResourcePreparation(String taskId, TaskActionRequest request);
    
    /**
     * 完成入职培训
     */
    boolean completeOnboardingTraining(String taskId, TaskActionRequest request);
    
    /**
     * 更新入职信息
     */
    boolean updateOnboardInfo(String businessKey, EmployeeOnboardRequest request);
}