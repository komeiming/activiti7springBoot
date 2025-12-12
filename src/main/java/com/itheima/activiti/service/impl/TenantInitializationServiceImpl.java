package com.itheima.activiti.service.impl;

import com.itheima.activiti.entity.NotificationTemplate;
import com.itheima.activiti.entity.WorkflowTemplate;
import com.itheima.activiti.service.NotificationTemplateService;
import com.itheima.activiti.service.TenantInitializationService;
import com.itheima.activiti.service.WorkflowTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 租户初始化服务实现类
 */
@Service
public class TenantInitializationServiceImpl implements TenantInitializationService {
    
    @Autowired
    private NotificationTemplateService notificationTemplateService;
    
    @Autowired
    private WorkflowTemplateService workflowTemplateService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initializeTenant(String tenantId) {
        // 初始化通知模板
        initializeNotificationTemplates(tenantId);
        // 初始化工作流模板
        initializeWorkflowTemplates(tenantId);
    }
    
    @Override
    public void initializeNotificationTemplates(String tenantId) {
        // 创建默认的注册成功通知模板
        NotificationTemplate registerTemplate = new NotificationTemplate();
        registerTemplate.setId(UUID.randomUUID().toString());
        registerTemplate.setTenantId(tenantId);
        registerTemplate.setName("注册成功通知");
        registerTemplate.setCode("template_register_success");
        registerTemplate.setType("email");
        registerTemplate.setStatus("enabled");
        registerTemplate.setSender("no-reply@example.com");
        registerTemplate.setSubject("注册成功通知");
        registerTemplate.setContent("尊敬的${contactName}，您的租户注册已成功，APP ID：${appId}，请妥善保管您的密钥。");
        registerTemplate.setDescription("租户注册成功后发送的通知模板");
        registerTemplate.setCreatedBy("system");
        registerTemplate.setCreatedTime(LocalDateTime.now());
        registerTemplate.setUpdatedBy("system");
        registerTemplate.setUpdatedTime(LocalDateTime.now());
        
        notificationTemplateService.createTemplate(registerTemplate);
        
        // 创建默认的审核通过通知模板
        NotificationTemplate approvalTemplate = new NotificationTemplate();
        approvalTemplate.setId(UUID.randomUUID().toString());
        approvalTemplate.setTenantId(tenantId);
        approvalTemplate.setName("审核通过通知");
        approvalTemplate.setCode("template_approval_success");
        approvalTemplate.setType("email");
        approvalTemplate.setStatus("enabled");
        approvalTemplate.setSender("no-reply@example.com");
        approvalTemplate.setSubject("审核通过通知");
        approvalTemplate.setContent("尊敬的${contactName}，您的租户申请已审核通过，APP ID：${appId}，请妥善保管您的密钥。");
        approvalTemplate.setDescription("租户审核通过后发送的通知模板");
        approvalTemplate.setCreatedBy("system");
        approvalTemplate.setCreatedTime(LocalDateTime.now());
        approvalTemplate.setUpdatedBy("system");
        approvalTemplate.setUpdatedTime(LocalDateTime.now());
        
        notificationTemplateService.createTemplate(approvalTemplate);
    }
    
    @Override
    public void initializeWorkflowTemplates(String tenantId) {
        // 创建默认的审批流程模板
        WorkflowTemplate approvalTemplate = new WorkflowTemplate();
        approvalTemplate.setId(UUID.randomUUID().toString());
        approvalTemplate.setTenantId(tenantId);
        approvalTemplate.setName("默认审批流程");
        approvalTemplate.setDescription("租户默认的审批流程模板");
        // 简单的审批流程JSON定义
        String processJson = "{\"name\":\"默认审批流程\",\"nodes\":[{\"id\":\"start\",\"type\":\"start\",\"name\":\"开始\"},{\"id\":\"approve\",\"type\":\"approve\",\"name\":\"审批\"},{\"id\":\"end\",\"type\":\"end\",\"name\":\"结束\"}],\"flows\":[{\"from\":\"start\",\"to\":\"approve\"},{\"from\":\"approve\",\"to\":\"end\"}]} ";
        approvalTemplate.setProcessJson(processJson);
        approvalTemplate.setStatus("draft");
        approvalTemplate.setCreatedAt(LocalDateTime.now());
        approvalTemplate.setUpdatedAt(LocalDateTime.now());
        approvalTemplate.setCreatedBy("system");
        approvalTemplate.setUpdatedBy("system");
        
        workflowTemplateService.createTemplate(approvalTemplate);
        
        // 创建默认的请假流程模板
        WorkflowTemplate leaveTemplate = new WorkflowTemplate();
        leaveTemplate.setId(UUID.randomUUID().toString());
        leaveTemplate.setTenantId(tenantId);
        leaveTemplate.setName("默认请假流程");
        leaveTemplate.setDescription("租户默认的请假流程模板");
        String leaveProcessJson = "{\"name\":\"默认请假流程\",\"nodes\":[{\"id\":\"start\",\"type\":\"start\",\"name\":\"开始\"},{\"id\":\"manager_approve\",\"type\":\"approve\",\"name\":\"经理审批\"},{\"id\":\"hr_approve\",\"type\":\"approve\",\"name\":\"HR审批\"},{\"id\":\"end\",\"type\":\"end\",\"name\":\"结束\"}],\"flows\":[{\"from\":\"start\",\"to\":\"manager_approve\"},{\"from\":\"manager_approve\",\"to\":\"hr_approve\"},{\"from\":\"hr_approve\",\"to\":\"end\"}]} ";
        leaveTemplate.setProcessJson(leaveProcessJson);
        leaveTemplate.setStatus("draft");
        leaveTemplate.setCreatedAt(LocalDateTime.now());
        leaveTemplate.setUpdatedAt(LocalDateTime.now());
        leaveTemplate.setCreatedBy("system");
        leaveTemplate.setUpdatedBy("system");
        
        workflowTemplateService.createTemplate(leaveTemplate);
    }
}
