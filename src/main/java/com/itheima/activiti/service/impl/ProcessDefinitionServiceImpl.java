package com.itheima.activiti.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;

import com.itheima.activiti.service.ProcessDefinitionService;

/**
 * 流程定义服务实现类
 */
@Service("processDefinitionService")
public class ProcessDefinitionServiceImpl implements ProcessDefinitionService {

    private static final Logger log = LoggerFactory.getLogger(ProcessDefinitionServiceImpl.class);
    
    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;
    
    @Autowired
    private ProcessEngine processEngine;

    public Deployment deployProcess(MultipartFile file, String deploymentName) {
        try {
            return repositoryService.createDeployment()
                    .name(deploymentName)
                    .addInputStream(file.getOriginalFilename(), file.getInputStream())
                    .deploy();
        } catch (Exception e) {
            throw new RuntimeException("部署流程文件失败", e);
        }
    }

    public Deployment deployProcessFromClassPath(String resourcePath, String deploymentName) {
        return repositoryService.createDeployment()
                .name(deploymentName)
                .addClasspathResource(resourcePath)
                .deploy();
    }

    public List<ProcessDefinition> getAllProcessDefinitions() {
        return repositoryService.createProcessDefinitionQuery()
                .latestVersion()
                .orderByProcessDefinitionName().asc()
                .list();
    }

    public Map<String, Object> getProcessDefinitionsByPage(int page, int pageSize) {
        long total = repositoryService.createProcessDefinitionQuery()
                .latestVersion()
                .count();

        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
                .latestVersion()
                .orderByProcessDefinitionName().asc()
                .listPage((page - 1) * pageSize, pageSize);

        // 将ProcessDefinition对象转换为简单的Map对象，避免JSON序列化错误
        List<Map<String, Object>> processDefinitionMaps = new ArrayList<>();
        for (ProcessDefinition pd : processDefinitions) {
            Map<String, Object> pdMap = new HashMap<>();
            pdMap.put("id", pd.getId());
            pdMap.put("key", pd.getKey());
            pdMap.put("name", pd.getName());
            pdMap.put("version", pd.getVersion());
            pdMap.put("resourceName", pd.getResourceName());
            pdMap.put("diagramResourceName", pd.getDiagramResourceName());
            pdMap.put("deploymentId", pd.getDeploymentId());
            pdMap.put("suspended", pd.isSuspended());
            pdMap.put("tenantId", pd.getTenantId());
            processDefinitionMaps.add(pdMap);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("rows", processDefinitionMaps);
        result.put("page", page);
        result.put("pageSize", pageSize);

        return result;
    }

    public ProcessDefinition getProcessDefinitionById(String processDefinitionId) {
        return repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult();
    }

    public ProcessDefinition getLatestProcessDefinitionByKey(String processDefinitionKey) {
        return repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .latestVersion()
                .singleResult();
    }

    public boolean suspendProcessDefinition(String processDefinitionId) {
        try {
            repositoryService.suspendProcessDefinitionById(processDefinitionId, true, null);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean activateProcessDefinition(String processDefinitionId) {
        try {
            repositoryService.activateProcessDefinitionById(processDefinitionId, true, null);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean deployProcess(MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            String deploymentName = fileName != null ? fileName.substring(0, fileName.lastIndexOf('.')) : "Process Deployment";
            repositoryService.createDeployment()
                    .name(deploymentName)
                    .addInputStream(fileName, file.getInputStream())
                    .deploy();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public Map<String, Object> getProcessDefinitionDetail(String processDefinitionId) {
        ProcessDefinition processDefinition = getProcessDefinitionById(processDefinitionId);
        if (processDefinition == null) {
            return null;
        }
        
        Map<String, Object> detail = new HashMap<>();
        detail.put("id", processDefinition.getId());
        detail.put("key", processDefinition.getKey());
        detail.put("name", processDefinition.getName());
        detail.put("version", processDefinition.getVersion());
        detail.put("resourceName", processDefinition.getResourceName());
        detail.put("diagramResourceName", processDefinition.getDiagramResourceName());
        detail.put("deploymentId", processDefinition.getDeploymentId());
        detail.put("isSuspended", processDefinition.isSuspended());
        
        return detail;
    }

    public boolean deleteProcessDefinition(String deploymentId, boolean cascade) {
        try {
            repositoryService.deleteDeployment(deploymentId, cascade);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public List<ProcessDefinition> getProcessDefinitionList(int page, int pageSize) {
        return repositoryService.createProcessDefinitionQuery()
                .latestVersion()
                .orderByProcessDefinitionName().asc()
                .listPage((page - 1) * pageSize, pageSize);
    }

    public InputStream getProcessDiagram(String processDefinitionId) {
        ProcessDefinition processDefinition = getProcessDefinitionById(processDefinitionId);
        if (processDefinition == null) {
            throw new RuntimeException("流程定义不存在");
        }
        
        // 在Activiti 7中，获取流程图的正确方法
        String diagramResourceName = processDefinition.getDiagramResourceName();
        return repositoryService.getResourceAsStream(
                processDefinition.getDeploymentId(),
                diagramResourceName
        );
    }

    public String getProcessModelXML(String processDefinitionId) {
        ProcessDefinition processDefinition = getProcessDefinitionById(processDefinitionId);
        if (processDefinition == null) {
            throw new RuntimeException("流程定义不存在");
        }
        
        try {
            InputStream inputStream = repositoryService.getResourceAsStream(
                processDefinition.getDeploymentId(), 
                processDefinition.getResourceName());
            
            if (inputStream == null) {
                throw new RuntimeException("未找到流程模型文件");
            }
            
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            inputStream.close();
            return new String(bytes);
        } catch (Exception e) {
            throw new RuntimeException("读取流程模型文件失败", e);
        }
    }

    // 确保这个方法是正确的重写方法
    public String startProcessInstance(String processDefinitionKey, String businessKey, Map<String, Object> variables) {
        try {
            System.out.println("服务层开始启动流程实例，processDefinitionKey: " + processDefinitionKey);
            
            // 验证流程定义是否存在且处于激活状态
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionKey(processDefinitionKey)
                    .active()
                    .latestVersion()
                    .singleResult();
            
            if (processDefinition == null) {
                throw new RuntimeException("流程定义不存在或未激活: " + processDefinitionKey);
            }
            
            System.out.println("找到激活的流程定义: " + processDefinition.getName() + " (id: " + processDefinition.getId() + ")");
            
            // 确保变量Map不为null
            if (variables == null) {
                variables = new HashMap<>();
            }
            
            // 如果提供了approver变量但没有manager变量，将approver的值赋给manager
            // 这是为了兼容BPMN文件中使用${manager}表达式的情况
            if (variables.containsKey("approver") && !variables.containsKey("manager")) {
                Object approverValue = variables.get("approver");
                variables.put("manager", approverValue);
                System.out.println("自动设置manager变量为approver的值: " + approverValue);
            }
            
            // 确保applicant变量存在，如果不存在则使用initiatorId
            if (!variables.containsKey("applicant") && variables.containsKey("initiatorId")) {
                variables.put("applicant", variables.get("initiatorId"));
            }
            
            // 启动流程实例
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                    processDefinitionKey, 
                    businessKey, 
                    variables);
            
            System.out.println("流程实例启动成功: " + processInstance.getId() + ", 业务键: " + processInstance.getBusinessKey());
            return processInstance.getId();
        } catch (Exception e) {
            System.err.println("服务层启动流程实例失败: " + e.getMessage());
            e.printStackTrace();
            throw e; // 重新抛出异常以便控制器捕获
        }
    }

    /**
     * 部署leave流程定义
     * @return 部署ID
     */
    public String deployLeaveProcess() {
        try {
            // 直接使用正确的路径 - processes目录
            String resourcePath = "processes/leave-process.bpmn";
            
            System.out.println("尝试部署流程文件: " + resourcePath);
            
            // 部署流程定义
            Deployment deployment = repositoryService.createDeployment()
                    .addClasspathResource(resourcePath)
                    .name("Leave Process")
                    .category("HR")
                    .deploy();
            
            String deploymentId = deployment.getId();
            System.out.println("Leave流程部署成功，部署ID: " + deploymentId);
            
            return deploymentId;
        } catch (Exception e) {
            System.err.println("部署Leave流程失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("部署Leave流程失败: " + e.getMessage(), e);
        }
    }
    
    // 新增方法：获取流程实例
    public ProcessInstance getProcessInstanceById(String processInstanceId) {
        return runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
    }
    
    // 新增方法：获取历史流程实例
    public HistoricProcessInstance getHistoricProcessInstanceById(String processInstanceId) {
        return processEngine.getHistoryService().createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
    }
    
    // 新增方法：挂起流程实例
    public boolean suspendProcessInstance(String processInstanceId) {
        try {
            runtimeService.suspendProcessInstanceById(processInstanceId);
            return true;
        } catch (Exception e) {
            log.error("挂起流程实例失败", e);
            return false;
        }
    }
    
    // 新增方法：激活流程实例
    public boolean activateProcessInstance(String processInstanceId) {
        try {
            runtimeService.activateProcessInstanceById(processInstanceId);
            return true;
        } catch (Exception e) {
            log.error("激活流程实例失败", e);
            return false;
        }
    }
    
    // 新增方法：终止流程实例
    public boolean deleteProcessInstance(String processInstanceId, String deleteReason) {
        try {
            runtimeService.deleteProcessInstance(processInstanceId, deleteReason);
            return true;
        } catch (Exception e) {
            log.error("删除流程实例失败", e);
            return false;
        }
    }
    
    // 新增方法：根据用户ID获取活跃的流程实例列表
    public Map<String, Object> getActiveProcessInstancesByUserId(String userId, int page, int pageSize) {
        // 查询用户参与的活跃流程实例
        // 这里通过用户已完成的任务来关联其参与的流程实例
        long total = processEngine.getHistoryService().createHistoricTaskInstanceQuery()
                .taskAssignee(userId)
                .processUnfinished()
                .count();
        
        List<HistoricProcessInstance> processInstances = processEngine.getHistoryService().createHistoricProcessInstanceQuery()
                .involvedUser(userId)
                .unfinished()
                .orderByProcessInstanceStartTime().desc()
                .listPage((page - 1) * pageSize, pageSize);
        
        List<Map<String, Object>> processInstanceMaps = new ArrayList<>();
        for (HistoricProcessInstance instance : processInstances) {
            Map<String, Object> instanceMap = new HashMap<>();
            instanceMap.put("id", instance.getId());
            instanceMap.put("processDefinitionId", instance.getProcessDefinitionId());
            instanceMap.put("processDefinitionKey", instance.getProcessDefinitionKey());
            instanceMap.put("processDefinitionName", instance.getProcessDefinitionName());
            instanceMap.put("businessKey", instance.getBusinessKey());
            instanceMap.put("startTime", instance.getStartTime());
            instanceMap.put("startUserId", instance.getStartUserId());
            instanceMap.put("isActive", true);
            processInstanceMaps.add(instanceMap);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", processInstanceMaps);
        result.put("total", total);
        return result;
    }
    
    // 新增方法：根据用户ID获取已完成的流程实例列表
    public Map<String, Object> getCompletedProcessInstancesByUserId(String userId, int page, int pageSize) {
        long total = processEngine.getHistoryService().createHistoricProcessInstanceQuery()
                .involvedUser(userId)
                .finished()
                .count();
        
        List<HistoricProcessInstance> processInstances = processEngine.getHistoryService().createHistoricProcessInstanceQuery()
                .involvedUser(userId)
                .finished()
                .orderByProcessInstanceEndTime().desc()
                .listPage((page - 1) * pageSize, pageSize);
        
        List<Map<String, Object>> processInstanceMaps = new ArrayList<>();
        for (HistoricProcessInstance instance : processInstances) {
            Map<String, Object> instanceMap = new HashMap<>();
            instanceMap.put("id", instance.getId());
            instanceMap.put("processDefinitionId", instance.getProcessDefinitionId());
            instanceMap.put("processDefinitionKey", instance.getProcessDefinitionKey());
            instanceMap.put("processDefinitionName", instance.getProcessDefinitionName());
            instanceMap.put("businessKey", instance.getBusinessKey());
            instanceMap.put("startTime", instance.getStartTime());
            instanceMap.put("endTime", instance.getEndTime());
            instanceMap.put("durationInMillis", instance.getDurationInMillis());
            instanceMap.put("startUserId", instance.getStartUserId());
            instanceMap.put("deleteReason", instance.getDeleteReason());
            instanceMap.put("isActive", false);
            processInstanceMaps.add(instanceMap);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", processInstanceMaps);
        result.put("total", total);
        return result;
    }
    
    // 新增方法：获取流程实例的历史活动
    public List<Map<String, Object>> getProcessInstanceHistory(String processInstanceId) {
        List<HistoricActivityInstance> activities = processEngine.getHistoryService().createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime().asc()
                .list();
        
        List<Map<String, Object>> activityMaps = new ArrayList<>();
        for (HistoricActivityInstance activity : activities) {
            Map<String, Object> activityMap = new HashMap<>();
            activityMap.put("id", activity.getId());
            activityMap.put("activityId", activity.getActivityId());
            activityMap.put("activityName", activity.getActivityName());
            activityMap.put("activityType", activity.getActivityType());
            activityMap.put("assignee", activity.getAssignee());
            activityMap.put("startTime", activity.getStartTime());
            activityMap.put("endTime", activity.getEndTime());
            activityMap.put("durationInMillis", activity.getDurationInMillis());
            activityMap.put("tenantId", activity.getTenantId());
            activityMaps.add(activityMap);
        }
        
        return activityMaps;
    }
    
    // 新增方法：设置流程变量
    public void setProcessVariables(String processInstanceId, Map<String, Object> variables) {
        runtimeService.setVariables(processInstanceId, variables);
    }
    
    // 新增方法：获取流程实例变量
    public Map<String, Object> getProcessInstanceVariables(String processInstanceId) {
        // 先尝试从运行时获取
        ProcessInstance processInstance = getProcessInstanceById(processInstanceId);
        if (processInstance != null) {
            return runtimeService.getVariables(processInstanceId);
        }
        
        // 如果运行时不存在，从历史记录获取
        // 使用正确的API获取历史流程变量
        return processEngine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(processInstanceId)
                .list()
                .stream()
                .collect(Collectors.toMap(
                        variableInstance -> variableInstance.getVariableName(),
                        variableInstance -> variableInstance.getValue()
                ));
    }
    
}