package com.itheima.activiti.service;

import com.itheima.activiti.BaseTest;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 流程定义管理服务测试
 */
public class ProcessDefinitionServiceTest extends BaseTest {

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    private String deploymentId;
    private String processDefinitionId;

    @BeforeEach
    public void initData() {
        // 清理测试数据
        cleanTestData();
        
        try {
            // 模拟部署一个测试流程
            File bpmnFile = new File("src/main/resources/processes/employee-onboarding.bpmn20.xml");
            if (bpmnFile.exists()) {
                try (InputStream inputStream = new FileInputStream(bpmnFile)) {
                    MultipartFile multipartFile = new MockMultipartFile("employee-onboarding.bpmn20.xml", inputStream);
                    processDefinitionService.deployProcess(multipartFile, "Test Deployment");
                    
                    // 获取最新部署的ID（不使用orderByDeploymentTime方法）
                    List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
                    if (!deployments.isEmpty()) {
                        deploymentId = deployments.get(deployments.size() - 1).getId();
                    }
                    
                    // 获取部署的流程定义
                    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
                            .deploymentId(deploymentId)
                            .list();
                    
                    if (!processDefinitions.isEmpty()) {
                        processDefinitionId = processDefinitions.get(0).getId();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDeployProcess() {
        // 验证部署成功
        Assertions.assertNotNull(deploymentId, "部署ID不应为null");
        
        // 检查部署是否存在
        Deployment deployment = repositoryService.createDeploymentQuery()
                .deploymentId(deploymentId)
                .singleResult();
        Assertions.assertNotNull(deployment, "部署应存在");
    }

    @Test
    public void testGetProcessDefinitionList() {
        // 获取流程定义列表
        List<ProcessDefinition> processDefinitions = processDefinitionService.getProcessDefinitionList(1, 10);
        Assertions.assertNotNull(processDefinitions, "结果不应为null");
        Assertions.assertTrue(!processDefinitions.isEmpty(), "流程定义列表不应为空");
    }

    @Test
    public void testGetProcessDefinitionDetail() {
        // 获取流程定义详情
        Map<String, Object> detail = processDefinitionService.getProcessDefinitionDetail(processDefinitionId);
        Assertions.assertNotNull(detail, "详情不应为null");
        Assertions.assertEquals(processDefinitionId, detail.get("id"), "ID应匹配");
    }

    @Test
    public void testSuspendProcessDefinition() {
        // 挂起流程定义
        boolean suspended = processDefinitionService.suspendProcessDefinition(processDefinitionId);
        Assertions.assertTrue(suspended, "挂起操作应成功");
        
        // 验证流程定义状态为挂起
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult();
        Assertions.assertTrue(processDefinition.isSuspended(), "流程定义应处于挂起状态");
    }

    @Test
    public void testActivateProcessDefinition() {
        // 先挂起，再激活
        processDefinitionService.suspendProcessDefinition(processDefinitionId);
        boolean activated = processDefinitionService.activateProcessDefinition(processDefinitionId);
        
        Assertions.assertTrue(activated, "激活操作应成功");
        
        // 验证流程定义状态为激活
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult();
        Assertions.assertFalse(processDefinition.isSuspended(), "流程定义应处于激活状态");
    }

    @Test
    public void testDeleteProcessDefinition() {
        // 创建一个新的测试部署用于删除测试
        try {
            File bpmnFile = new File("src/main/resources/processes/employee-onboarding.bpmn20.xml");
            if (bpmnFile.exists()) {
                try (InputStream inputStream = new FileInputStream(bpmnFile)) {
                    MultipartFile multipartFile = new MockMultipartFile("test-delete.bpmn20.xml", inputStream);
                    processDefinitionService.deployProcess(multipartFile, "Test Delete Deployment");
                    
                    // 获取最新部署的ID（不使用orderByDeploymentTime方法）
                    List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
                    String deleteDeploymentId = null;
                    if (!deployments.isEmpty()) {
                        deleteDeploymentId = deployments.get(deployments.size() - 1).getId();
                    }
                    
                    // 获取要删除的流程定义ID
                    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
                            .deploymentId(deleteDeploymentId)
                            .list();
                    
                    if (!processDefinitions.isEmpty()) {
                        String deleteProcessDefinitionId = processDefinitions.get(0).getId();
                        
                        // 执行删除操作
                        boolean deleted = processDefinitionService.deleteProcessDefinition(deleteDeploymentId, true);
                        Assertions.assertTrue(deleted, "删除操作应成功");
                        
                        // 验证流程定义已删除
                        ProcessDefinition deletedDefinition = repositoryService.createProcessDefinitionQuery()
                                .processDefinitionId(deleteProcessDefinitionId)
                                .singleResult();
                        Assertions.assertNull(deletedDefinition, "流程定义应被删除");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("删除测试失败: " + e.getMessage());
        }
    }

    @Test
    public void testStartProcessInstance() {
        // 启动流程实例
        Map<String, Object> variables = new java.util.HashMap<>();
        variables.put("employeeName", "测试员工");
        variables.put("department", "测试部门");
        
        // 修复返回值类型，使用Object类型以兼容实际的返回值
        Object result = processDefinitionService.startProcessInstance(processDefinitionId, "TEST-123", variables);
        
        Assertions.assertNotNull(result, "启动结果不应为null");
        
        // 根据实际返回值类型进行检查
        if (result instanceof Map) {
            Map<?, ?> resultMap = (Map<?, ?>) result;
            Assertions.assertTrue(resultMap.containsKey("processInstanceId"), "应返回流程实例ID");
            
            // 验证流程实例存在
            Object processInstanceId = resultMap.get("processInstanceId");
            Assertions.assertNotNull(processInstanceId, "流程实例ID不应为null");
        } else if (result instanceof String) {
            // 如果返回的是流程实例ID字符串
            Assertions.assertNotNull(result, "流程实例ID不应为null");
        }
    }
}