package com.itheima.activiti.service;

import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 流程定义服务接口
 */
public interface ProcessDefinitionService {

    /**
     * 部署流程定义
     * @param file 上传的BPMN文件
     * @param deploymentName 部署名称
     * @return 部署信息
     */
    Deployment deployProcess(MultipartFile file, String deploymentName);
    
    /**
     * 部署流程定义（重载方法）
     * @param file 上传的BPMN文件
     * @return 是否部署成功
     */
    boolean deployProcess(MultipartFile file);

    /**
     * 部署流程定义（从类路径）
     * @param resourcePath 资源路径
     * @param deploymentName 部署名称
     * @return 部署信息
     */
    Deployment deployProcessFromClassPath(String resourcePath, String deploymentName);

    /**
     * 获取所有流程定义列表
     * @return 流程定义列表
     */
    List<ProcessDefinition> getAllProcessDefinitions();

    /**
     * 分页获取流程定义列表
     * @param page 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    Map<String, Object> getProcessDefinitionsByPage(int page, int pageSize);

    /**
     * 根据流程定义ID获取流程定义
     * @param processDefinitionId 流程定义ID
     * @return 流程定义
     */
    ProcessDefinition getProcessDefinitionById(String processDefinitionId);

    /**
     * 根据流程定义key获取最新版本的流程定义
     * @param processDefinitionKey 流程定义key
     * @return 流程定义
     */
    ProcessDefinition getLatestProcessDefinitionByKey(String processDefinitionKey);

    /**
     * 挂起流程定义
     * @param processDefinitionId 流程定义ID
     * @return 是否挂起成功
     */
    boolean suspendProcessDefinition(String processDefinitionId);

    /**
     * 激活流程定义
     * @param processDefinitionId 流程定义ID
     * @return 是否激活成功
     */
    boolean activateProcessDefinition(String processDefinitionId);
    
    /**
     * 获取流程定义详情
     * @param processDefinitionId 流程定义ID
     * @return 流程定义详情
     */
    Map<String, Object> getProcessDefinitionDetail(String processDefinitionId);

    /**
     * 删除流程定义
     * @param deploymentId 部署ID
     * @param cascade 是否级联删除（删除相关的历史数据）
     * @return 是否删除成功
     */
    boolean deleteProcessDefinition(String deploymentId, boolean cascade);
    
    /**
     * 获取流程定义列表
     * @param page 页码
     * @param pageSize 每页大小
     * @return 流程定义列表
     */
    List<ProcessDefinition> getProcessDefinitionList(int page, int pageSize);

    /**
     * 获取流程定义的流程图
     * @param processDefinitionId 流程定义ID
     * @return 流程图输入流
     */
    InputStream getProcessDiagram(String processDefinitionId);

    /**
     * 获取流程定义的XML内容
     * @param processDefinitionId 流程定义ID
     * @return XML内容
     */
    String getProcessModelXML(String processDefinitionId);

    /**
     * 启动流程实例
     * @param processDefinitionKey 流程定义key
     * @param businessKey 业务key
     * @param variables 流程变量
     * @return 流程实例ID
     */
    String startProcessInstance(String processDefinitionKey, String businessKey, Map<String, Object> variables);
    
    /**
     * 自动部署leave流程定义
     * 从外部文件系统加载leave-process.bpmn文件并部署
     * @return 部署ID
     */
    String deployLeaveProcess();
    
    /**
     * 获取流程实例
     * @param processInstanceId 流程实例ID
     * @return 流程实例
     */
    ProcessInstance getProcessInstanceById(String processInstanceId);
    
    /**
     * 获取历史流程实例
     * @param processInstanceId 流程实例ID
     * @return 历史流程实例
     */
    HistoricProcessInstance getHistoricProcessInstanceById(String processInstanceId);
    
    /**
     * 挂起流程实例
     * @param processInstanceId 流程实例ID
     * @return 是否挂起成功
     */
    boolean suspendProcessInstance(String processInstanceId);
    
    /**
     * 激活流程实例
     * @param processInstanceId 流程实例ID
     * @return 是否激活成功
     */
    boolean activateProcessInstance(String processInstanceId);
    
    /**
     * 终止流程实例
     * @param processInstanceId 流程实例ID
     * @param deleteReason 删除原因
     * @return 是否删除成功
     */
    boolean deleteProcessInstance(String processInstanceId, String deleteReason);
    
    /**
     * 根据用户ID获取活跃的流程实例列表
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    Map<String, Object> getActiveProcessInstancesByUserId(String userId, int page, int pageSize);
    
    /**
     * 根据用户ID获取已完成的流程实例列表
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    Map<String, Object> getCompletedProcessInstancesByUserId(String userId, int page, int pageSize);
    
    /**
     * 获取流程实例的历史活动
     * @param processInstanceId 流程实例ID
     * @return 历史活动列表
     */
    List<Map<String, Object>> getProcessInstanceHistory(String processInstanceId);
    
    /**
     * 设置流程变量
     * @param processInstanceId 流程实例ID
     * @param variables 流程变量
     */
    void setProcessVariables(String processInstanceId, Map<String, Object> variables);
    
    /**
     * 获取流程实例变量
     * @param processInstanceId 流程实例ID
     * @return 流程变量
     */
    Map<String, Object> getProcessInstanceVariables(String processInstanceId);
}