package com.itheima.activiti.controller;

import com.itheima.activiti.common.Result;
import com.itheima.activiti.common.TenantContext;
import com.itheima.activiti.dto.CommonResponse;
import com.itheima.activiti.entity.WorkflowTemplate;
import com.itheima.activiti.service.ProcessDefinitionService;
import com.itheima.activiti.service.WorkflowTemplateService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 工作流模块公有服务控制器
 */
@RestController
@RequestMapping("/api/v1/workflow")
public class PublicWorkflowController {
    
    private static final Logger logger = LoggerFactory.getLogger(PublicWorkflowController.class);
    
    @Autowired
    private ProcessDefinitionService processDefinitionService;
    
    @Autowired
    private WorkflowTemplateService workflowTemplateService;
    
    // ================ 流程模板管理接口 ================
    
    /**
     * 创建流程模板
     */
    @PostMapping("/template")
    public Result<WorkflowTemplate> createWorkflowTemplate(@RequestBody WorkflowTemplate template) {
        try {
            logger.info("创建流程模板请求: {}", template);
            
            // 获取当前租户ID
            String tenantId = TenantContext.getTenantId();
            if (tenantId == null || tenantId.trim().isEmpty()) {
                return Result.error("租户ID不能为空");
            }
            
            // 设置租户ID
            template.setTenantId(tenantId);
            
            // 验证必填字段
            if (template.getName() == null || template.getName().trim().isEmpty()) {
                return Result.error("模板名称不能为空");
            }
            if (template.getProcessJson() == null || template.getProcessJson().trim().isEmpty()) {
                return Result.error("流程JSON不能为空");
            }
            
            // 创建流程模板
            WorkflowTemplate createdTemplate = workflowTemplateService.createTemplate(template);
            
            return Result.success("流程模板创建成功", createdTemplate);
        } catch (Exception e) {
            logger.error("创建流程模板失败:", e);
            return Result.error("创建流程模板失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询自身流程模板
     */
    @GetMapping("/template")
    public Result<Map<String, Object>> getWorkflowTemplates(
            @RequestParam(required = false) String templateId,
            @RequestParam(required = false) String templateName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            logger.info("查询流程模板请求, templateId: {}, templateName: {}, status: {}, page: {}, size: {}", 
                    templateId, templateName, status, page, size);
            
            // 获取当前租户ID
            String tenantId = TenantContext.getTenantId();
            if (tenantId == null || tenantId.trim().isEmpty()) {
                return Result.error("租户ID不能为空");
            }
            
            // 构建查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("tenantId", tenantId);
            params.put("name", templateName);
            params.put("status", status);
            params.put("startTime", startTime);
            params.put("endTime", endTime);
            params.put("page", page);
            params.put("size", size);
            
            // 查询模板列表和总数
            java.util.List<WorkflowTemplate> templates = workflowTemplateService.queryTemplates(params);
            long total = workflowTemplateService.countTemplates(params);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("data", templates);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            
            return Result.success("查询流程模板成功", result);
        } catch (Exception e) {
            logger.error("查询流程模板失败:", e);
            return Result.error("查询流程模板失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取单个流程模板详情
     */
    @GetMapping("/template/{id}")
    public Result<WorkflowTemplate> getWorkflowTemplateById(@PathVariable String id) {
        try {
            logger.info("获取流程模板详情请求, id: {}", id);
            
            // 获取当前租户ID
            String tenantId = TenantContext.getTenantId();
            if (tenantId == null || tenantId.trim().isEmpty()) {
                return Result.error("租户ID不能为空");
            }
            
            // 获取模板详情
            WorkflowTemplate template = workflowTemplateService.getTemplateById(id);
            if (template == null) {
                return Result.error("流程模板不存在");
            }
            
            // 验证租户权限
            if (!template.getTenantId().equals(tenantId)) {
                return Result.error("无权限访问该流程模板");
            }
            
            return Result.success("获取流程模板详情成功", template);
        } catch (Exception e) {
            logger.error("获取流程模板详情失败:", e);
            return Result.error("获取流程模板详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新流程模板
     */
    @PutMapping("/template/{id}")
    public Result<WorkflowTemplate> updateWorkflowTemplate(@PathVariable String id, @RequestBody WorkflowTemplate template) {
        try {
            logger.info("更新流程模板请求, id: {}, template: {}", id, template);
            
            // 获取当前租户ID
            String tenantId = TenantContext.getTenantId();
            if (tenantId == null || tenantId.trim().isEmpty()) {
                return Result.error("租户ID不能为空");
            }
            
            // 验证模板是否存在
            WorkflowTemplate existingTemplate = workflowTemplateService.getTemplateById(id);
            if (existingTemplate == null) {
                return Result.error("流程模板不存在");
            }
            
            // 验证租户权限
            if (!existingTemplate.getTenantId().equals(tenantId)) {
                return Result.error("无权限修改该流程模板");
            }
            
            // 设置模板ID和租户ID
            template.setId(id);
            template.setTenantId(tenantId);
            
            // 更新模板
            WorkflowTemplate updatedTemplate = workflowTemplateService.updateTemplate(template);
            
            return Result.success("更新流程模板成功", updatedTemplate);
        } catch (Exception e) {
            logger.error("更新流程模板失败:", e);
            return Result.error("更新流程模板失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除流程模板
     */
    @DeleteMapping("/template/{id}")
    public Result<Boolean> deleteWorkflowTemplate(@PathVariable String id) {
        try {
            logger.info("删除流程模板请求, id: {}", id);
            
            // 获取当前租户ID
            String tenantId = TenantContext.getTenantId();
            if (tenantId == null || tenantId.trim().isEmpty()) {
                return Result.error("租户ID不能为空");
            }
            
            // 验证模板是否存在
            WorkflowTemplate template = workflowTemplateService.getTemplateById(id);
            if (template == null) {
                return Result.error("流程模板不存在");
            }
            
            // 验证租户权限
            if (!template.getTenantId().equals(tenantId)) {
                return Result.error("无权限删除该流程模板");
            }
            
            // 删除模板
            boolean deleted = workflowTemplateService.deleteTemplate(id);
            if (deleted) {
                return Result.success("删除流程模板成功", true);
            } else {
                return Result.error("删除流程模板失败");
            }
        } catch (Exception e) {
            logger.error("删除流程模板失败:", e);
            return Result.error("删除流程模板失败: " + e.getMessage());
        }
    }
    
    /**
     * 发布流程模板
     */
    @PostMapping("/template/{id}/publish")
    public Result<Boolean> publishWorkflowTemplate(@PathVariable String id) {
        try {
            logger.info("发布流程模板请求, id: {}", id);
            
            // 获取当前租户ID
            String tenantId = TenantContext.getTenantId();
            if (tenantId == null || tenantId.trim().isEmpty()) {
                return Result.error("租户ID不能为空");
            }
            
            // 验证模板是否存在
            WorkflowTemplate template = workflowTemplateService.getTemplateById(id);
            if (template == null) {
                return Result.error("流程模板不存在");
            }
            
            // 验证租户权限
            if (!template.getTenantId().equals(tenantId)) {
                return Result.error("无权限发布该流程模板");
            }
            
            // 发布模板
            boolean published = workflowTemplateService.publishTemplate(id);
            if (published) {
                return Result.success("发布流程模板成功", true);
            } else {
                return Result.error("发布流程模板失败");
            }
        } catch (Exception e) {
            logger.error("发布流程模板失败:", e);
            return Result.error("发布流程模板失败: " + e.getMessage());
        }
    }
    
    /**
     * 下架流程模板
     */
    @PostMapping("/template/{id}/offline")
    public Result<Boolean> offlineWorkflowTemplate(@PathVariable String id) {
        try {
            logger.info("下架流程模板请求, id: {}", id);
            
            // 获取当前租户ID
            String tenantId = TenantContext.getTenantId();
            if (tenantId == null || tenantId.trim().isEmpty()) {
                return Result.error("租户ID不能为空");
            }
            
            // 验证模板是否存在
            WorkflowTemplate template = workflowTemplateService.getTemplateById(id);
            if (template == null) {
                return Result.error("流程模板不存在");
            }
            
            // 验证租户权限
            if (!template.getTenantId().equals(tenantId)) {
                return Result.error("无权限下架该流程模板");
            }
            
            // 下架模板
            boolean offline = workflowTemplateService.offlineTemplate(id);
            if (offline) {
                return Result.success("下架流程模板成功", true);
            } else {
                return Result.error("下架流程模板失败");
            }
        } catch (Exception e) {
            logger.error("下架流程模板失败:", e);
            return Result.error("下架流程模板失败: " + e.getMessage());
        }
    }
    
    // ================ 流程实例管理接口 ================    

    /**
     * 启动流程实例
     */
    @PostMapping("/instance/start")
    public CommonResponse<Map<String, Object>> startProcessInstance(
            @RequestBody Map<String, Object> requestBody) {
        try {
            logger.info("启动流程实例请求: {}", requestBody);
            
            // 获取模板ID和流程参数
            String templateId = (String) requestBody.get("templateId");
            Map<String, Object> variables = (Map<String, Object>) requestBody.get("variables");
            String callbackUrl = (String) requestBody.get("callbackUrl");
            
            if (templateId == null || templateId.trim().isEmpty()) {
                return CommonResponse.fail("模板ID不能为空");
            }
            
            // 设置当前租户信息到流程变量
            if (variables == null) {
                variables = new HashMap<>();
            }
            variables.put("tenantId", TenantContext.getTenantId());
            variables.put("appId", TenantContext.getAppId());
            
            // 启动流程实例
            String processInstanceId = processDefinitionService.startProcessInstance(
                    templateId, null, variables);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("instanceId", processInstanceId);
            result.put("startTime", System.currentTimeMillis());
            result.put("currentNodeId", "start");
            
            logger.info("流程实例启动成功，processInstanceId: {}", processInstanceId);
            
            return CommonResponse.success(result);
        } catch (Exception e) {
            logger.error("启动流程实例失败:", e);
            return CommonResponse.fail("启动流程实例失败: " + e.getMessage());
        }
    }
    
    // ================ 流程定义管理接口 ================
    
    /**
     * 获取流程定义列表（租户隔离）
     */
    @GetMapping("/process/definition")
    public Result<Map<String, Object>> getProcessDefinitions(
            @RequestParam(required = false) String key,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            logger.info("查询流程定义请求, key: {}, name: {}, status: {}, page: {}, size: {}",
                    key, name, status, page, size);
            
            // 获取当前租户ID
            String tenantId = TenantContext.getTenantId();
            logger.info("当前租户ID: {}", tenantId);
            
            Map<String, Object> result;
            if (tenantId != null && !tenantId.trim().isEmpty()) {
                // 有租户ID时，查询该租户的流程定义
                result = ((com.itheima.activiti.service.impl.ProcessDefinitionServiceImpl) processDefinitionService)
                        .getProcessDefinitionsByTenantAndPage(tenantId, page, size);
            } else {
                // 无租户ID时，查询所有流程定义
                result = processDefinitionService.getProcessDefinitionsByPage(page, size);
            }
            
            return Result.success(result);
        } catch (Exception e) {
            logger.error("获取流程定义列表失败:", e);
            return Result.error("获取流程定义列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 部署流程定义（租户隔离）
     */
    @PostMapping("/process/definition/deploy")
    public Result<Map<String, Object>> deployProcess(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "name", required = true) String deploymentName) {
        try {
            logger.info("部署流程请求, fileName: {}, deploymentName: {}",
                    file.getOriginalFilename(), deploymentName);
            
            // 验证文件格式
            String filename = file.getOriginalFilename();
            if (!filename.endsWith(".bpmn") && !filename.endsWith(".bpmn20.xml")) {
                return Result.error("请上传.bpmn或.bpmn20.xml格式的文件");
            }
            
            // 获取当前租户ID
            String tenantId = TenantContext.getTenantId();
            logger.info("当前租户ID: {}", tenantId);
            
            Deployment deployment;
            if (tenantId != null && !tenantId.trim().isEmpty()) {
                // 有租户ID时，使用租户ID部署
                deployment = ((com.itheima.activiti.service.impl.ProcessDefinitionServiceImpl) processDefinitionService)
                        .deployProcessWithTenant(file, deploymentName, tenantId);
            } else {
                // 无租户ID时，正常部署
                deployment = processDefinitionService.deployProcess(file, deploymentName);
            }
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("deploymentId", deployment.getId());
            result.put("deploymentName", deployment.getName());
            result.put("deploymentTime", deployment.getDeploymentTime());
            result.put("tenantId", deployment.getTenantId());
            
            return Result.success("流程部署成功", result);
        } catch (Exception e) {
            logger.error("部署流程失败:", e);
            return Result.error("部署流程失败: " + e.getMessage());
        }
    }
    
    /**
     * 挂起流程定义
     */
    @PutMapping("/process/definition/{id}/suspend")
    public Result<Boolean> suspendProcessDefinition(@PathVariable String id) {
        try {
            logger.info("挂起流程定义请求, id: {}", id);
            
            boolean success = processDefinitionService.suspendProcessDefinition(id);
            
            return Result.success("流程定义已挂起", success);
        } catch (Exception e) {
            logger.error("挂起流程定义失败:", e);
            return Result.error("挂起流程定义失败: " + e.getMessage());
        }
    }
    
    /**
     * 激活流程定义
     */
    @PutMapping("/process/definition/{id}/activate")
    public Result<Boolean> activateProcessDefinition(@PathVariable String id) {
        try {
            logger.info("激活流程定义请求, id: {}", id);
            
            boolean success = processDefinitionService.activateProcessDefinition(id);
            
            return Result.success("流程定义已激活", success);
        } catch (Exception e) {
            logger.error("激活流程定义失败:", e);
            return Result.error("激活流程定义失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除流程定义
     */
    @DeleteMapping("/process/definition/{id}")
    public Result<Boolean> deleteProcessDefinition(
            @PathVariable String id,
            @RequestParam(defaultValue = "false") boolean cascade) {
        try {
            logger.info("删除流程定义请求, id: {}, cascade: {}", id, cascade);
            
            boolean success = false;
            
            // 先尝试根据流程定义ID获取流程定义对象
            ProcessDefinition processDefinition = processDefinitionService.getProcessDefinitionById(id);
            
            if (processDefinition == null) {
                return Result.error("流程定义不存在");
            }
            
            // 提取部署ID
            String deploymentId = processDefinition.getDeploymentId();
            
            // 调用服务层删除方法
            success = processDefinitionService.deleteProcessDefinition(deploymentId, cascade);
            
            return Result.success("流程定义已删除", success);
        } catch (Exception e) {
            logger.error("删除流程定义失败:", e);
            return Result.error("删除流程定义失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取流程图
     */
    @GetMapping("/process/definition/{id}/diagram")
    public ResponseEntity<?> getProcessDiagram(@PathVariable String id) {
        try {
            logger.info("获取流程图请求, id: {}", id);
            
            InputStream inputStream = processDefinitionService.getProcessDiagram(id);
            byte[] bytes = inputStream.readAllBytes();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("inline", "process-diagram.png");
            
            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("获取流程图失败:", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            errorResponse.put("code", "NO_DIAGRAM_AVAILABLE");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
    
    /**
     * 获取流程定义XML（租户隔离）
     */
    @GetMapping("/process/definition/{id}/xml")
    public Result<Map<String, Object>> getProcessDefinitionXML(@PathVariable String id) {
        try {
            logger.info("获取流程XML请求, id: {}", id);
            
            // 获取当前租户ID
            String tenantId = TenantContext.getTenantId();
            logger.info("当前租户ID: {}", tenantId);
            
            // 获取流程XML
            String xml = processDefinitionService.getProcessModelXML(id);
            
            // 验证流程定义的租户ID
            ProcessDefinition processDefinition = processDefinitionService.getProcessDefinitionById(id);
            if (processDefinition != null && tenantId != null && !tenantId.trim().isEmpty()) {
                if (!tenantId.equals(processDefinition.getTenantId())) {
                    return Result.error("无权限访问该流程定义");
                }
            }
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("xml", xml);
            result.put("processDefinitionId", id);
            
            return Result.success("获取流程XML成功", result);
        } catch (Exception e) {
            logger.error("获取流程XML失败:", e);
            return Result.error("获取流程XML失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据部署ID获取流程XML（租户隔离）
     */
    @GetMapping("/process/definition/xml/{deploymentId}")
    public Result<Map<String, Object>> getProcessDefinitionXMLByDeploymentId(@PathVariable String deploymentId) {
        try {
            logger.info("根据部署ID获取流程XML请求, deploymentId: {}", deploymentId);
            
            // 获取流程XML
            String xml = ((com.itheima.activiti.service.impl.ProcessDefinitionServiceImpl) processDefinitionService)
                    .getProcessModelXMLByDeploymentId(deploymentId);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("bpmnXml", xml);
            result.put("deploymentId", deploymentId);
            
            return Result.success("获取流程XML成功", result);
        } catch (Exception e) {
            logger.error("获取流程XML失败:", e);
            return Result.error("获取流程XML失败: " + e.getMessage());
        }
    }
    
    /**
     * 保存流程XML（租户隔离）
     */
    @PostMapping("/process/definition/save")
    public Result<Map<String, Object>> saveProcessDefinitionXML(@RequestBody Map<String, Object> request) {
        try {
            logger.info("保存流程XML请求: {}", request);
            
            // 获取当前租户ID
            String tenantId = TenantContext.getTenantId();
            logger.info("当前租户ID: {}", tenantId);
            
            if (tenantId == null || tenantId.trim().isEmpty()) {
                return Result.error("租户ID不能为空");
            }
            
            // 从请求中获取参数
            String name = (String) request.get("name");
            String key = (String) request.get("key");
            String xml = (String) request.get("xml");
            
            // 验证必填参数
            if (name == null || name.trim().isEmpty()) {
                return Result.error("流程名称不能为空");
            }
            if (key == null || key.trim().isEmpty()) {
                return Result.error("流程标识不能为空");
            }
            if (xml == null || xml.trim().isEmpty()) {
                return Result.error("流程XML不能为空");
            }
            
            // 调用服务层保存流程XML，传递租户ID参数
            ((com.itheima.activiti.service.impl.ProcessDefinitionServiceImpl) processDefinitionService)
                    .saveProcessXML(name, key, xml, tenantId);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("message", "流程保存成功");
            result.put("processName", name);
            result.put("processKey", key);
            
            return Result.success("保存流程XML成功", result);
        } catch (Exception e) {
            logger.error("保存流程XML失败:", e);
            return Result.error("保存流程XML失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询流程实例状态
     */
    @GetMapping("/instance/status")
    public CommonResponse<Map<String, Object>> getProcessInstanceStatus(
            @RequestParam String instanceId) {
        try {
            logger.info("查询流程实例状态, instanceId: {}", instanceId);
            
            if (instanceId == null || instanceId.trim().isEmpty()) {
                return CommonResponse.fail("实例ID不能为空");
            }
            
            // 获取流程实例信息
            org.activiti.engine.runtime.ProcessInstance processInstance = 
                processDefinitionService.getProcessInstanceById(instanceId);
            
            if (processInstance == null) {
                // 尝试从历史流程实例中查询
                org.activiti.engine.history.HistoricProcessInstance historicProcessInstance = 
                    processDefinitionService.getHistoricProcessInstanceById(instanceId);
                
                if (historicProcessInstance == null) {
                    return CommonResponse.notFound("流程实例不存在");
                }
                
                // 构建历史流程实例信息
                Map<String, Object> result = new HashMap<>();
                result.put("instanceId", historicProcessInstance.getId());
                result.put("status", "已完成");
                result.put("startTime", historicProcessInstance.getStartTime());
                result.put("endTime", historicProcessInstance.getEndTime());
                result.put("currentNode", null);
                result.put("completedNodes", new java.util.ArrayList<>());
                result.put("remainingNodes", new java.util.ArrayList<>());
                
                return CommonResponse.success(result);
            }
            
            // 构建活跃流程实例信息
            Map<String, Object> result = new HashMap<>();
            result.put("instanceId", processInstance.getId());
            result.put("status", processInstance.isSuspended() ? "已暂停" : "运行中");
            result.put("currentNode", "");
            result.put("completedNodes", new java.util.ArrayList<>());
            result.put("remainingNodes", new java.util.ArrayList<>());
            
            return CommonResponse.success(result);
        } catch (Exception e) {
            logger.error("查询流程实例状态失败:", e);
            return CommonResponse.fail("查询流程实例状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 暂停流程实例
     */
    @PutMapping("/instance/{instanceId}/suspend")
    public CommonResponse<String> suspendProcessInstance(
            @PathVariable String instanceId) {
        try {
            logger.info("暂停流程实例, instanceId: {}", instanceId);
            
            boolean success = processDefinitionService.suspendProcessInstance(instanceId);
            if (success) {
                return CommonResponse.success("流程实例已暂停");
            } else {
                return CommonResponse.fail("流程实例暂停失败");
            }
        } catch (Exception e) {
            logger.error("暂停流程实例失败:", e);
            return CommonResponse.fail("暂停流程实例失败: " + e.getMessage());
        }
    }
    
    /**
     * 恢复流程实例
     */
    @PutMapping("/instance/{instanceId}/activate")
    public CommonResponse<String> activateProcessInstance(
            @PathVariable String instanceId) {
        try {
            logger.info("恢复流程实例, instanceId: {}", instanceId);
            
            boolean success = processDefinitionService.activateProcessInstance(instanceId);
            if (success) {
                return CommonResponse.success("流程实例已恢复");
            } else {
                return CommonResponse.fail("流程实例恢复失败");
            }
        } catch (Exception e) {
            logger.error("恢复流程实例失败:", e);
            return CommonResponse.fail("恢复流程实例失败: " + e.getMessage());
        }
    }
    
    /**
     * 终止流程实例
     */
    @DeleteMapping("/instance/{instanceId}")
    public CommonResponse<String> terminateProcessInstance(
            @PathVariable String instanceId,
            @RequestParam(required = false) String deleteReason) {
        try {
            logger.info("终止流程实例, instanceId: {}, deleteReason: {}", instanceId, deleteReason);
            
            boolean success = processDefinitionService.deleteProcessInstance(instanceId, deleteReason);
            if (success) {
                return CommonResponse.success("流程实例已终止");
            } else {
                return CommonResponse.fail("流程实例终止失败");
            }
        } catch (Exception e) {
            logger.error("终止流程实例失败:", e);
            return CommonResponse.fail("终止流程实例失败: " + e.getMessage());
        }
    }
}