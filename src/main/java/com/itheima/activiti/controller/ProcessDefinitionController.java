package com.itheima.activiti.controller;

import com.itheima.activiti.service.ProcessDefinitionService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
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
 * 流程定义管理控制器
 */
@RestController
@RequestMapping("/api/process/definition")
public class ProcessDefinitionController {

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    /**
     * 部署流程定义
     */
    @PostMapping("/deploy")
    public ResponseEntity<Map<String, Object>> deployProcess(
            @RequestParam("file") MultipartFile file,
            @RequestParam("deploymentName") String deploymentName) {
        
        // 验证文件格式，支持.bpmn和.bpmn20.xml
        Map<String, Object> result = new HashMap<>();
        
        if (file == null || file.isEmpty()) {
            result.put("success", false);
            result.put("message", "请选择要部署的流程文件");
            return ResponseEntity.badRequest().body(result);
        }
        
        String filename = file.getOriginalFilename();
        if (!filename.endsWith(".bpmn") && !filename.endsWith(".bpmn20.xml")) {
            result.put("success", false);
            result.put("message", "请上传.bpmn或.bpmn20.xml格式的文件");
            return ResponseEntity.badRequest().body(result);
        }
        
        try {
            Deployment deployment = processDefinitionService.deployProcess(file, deploymentName);
            
            result.put("success", true);
            result.put("message", "流程部署成功");
            result.put("deploymentId", deployment.getId());
            result.put("deploymentName", deployment.getName());
            result.put("deploymentTime", deployment.getDeploymentTime());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "流程部署失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 获取流程定义列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getProcessDefinitionList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        Map<String, Object> result = processDefinitionService.getProcessDefinitionsByPage(page, pageSize);
        result.put("success", true);
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取流程定义详情
     */
    @GetMapping("/{processDefinitionId}")
    public ResponseEntity<Map<String, Object>> getProcessDefinition(
            @PathVariable String processDefinitionId) {
        
        ProcessDefinition processDefinition = processDefinitionService.getProcessDefinitionById(processDefinitionId);
        
        if (processDefinition == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "流程定义不存在"
            ));
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("processDefinition", Map.of(
                "id", processDefinition.getId(),
                "key", processDefinition.getKey(),
                "name", processDefinition.getName(),
                "version", processDefinition.getVersion(),
                "deploymentId", processDefinition.getDeploymentId(),
                "resourceName", processDefinition.getResourceName(),
                "diagramResourceName", processDefinition.getDiagramResourceName(),
                "suspended", processDefinition.isSuspended()
        ));
        
        return ResponseEntity.ok(result);
    }

    /**
     * 挂起流程定义
     */
    @PutMapping("/{processDefinitionId}/suspend")
    public ResponseEntity<Map<String, Object>> suspendProcessDefinition(
            @PathVariable String processDefinitionId) {
        
        boolean success = processDefinitionService.suspendProcessDefinition(processDefinitionId);
        
        if (success) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "流程定义已挂起"
            ));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "流程定义挂起失败"
            ));
        }
    }

    /**
     * 激活流程定义
     */
    @PutMapping("/{processDefinitionId}/activate")
    public ResponseEntity<Map<String, Object>> activateProcessDefinition(
            @PathVariable String processDefinitionId) {
        
        boolean success = processDefinitionService.activateProcessDefinition(processDefinitionId);
        
        if (success) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "流程定义已激活"
            ));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "流程定义激活失败"
            ));
        }
    }

    /**
     * 删除流程定义
     */
    @DeleteMapping("/deployment/{deploymentId}")
    public ResponseEntity<Map<String, Object>> deleteProcessDefinition(
            @PathVariable String deploymentId,
            @RequestParam(defaultValue = "false") boolean cascade) {
        
        processDefinitionService.deleteProcessDefinition(deploymentId, cascade);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "流程定义已删除"
        ));
    }

    /**
     * 获取流程图
     */
    @GetMapping("/{processDefinitionId}/diagram")
    public ResponseEntity<byte[]> getProcessDiagram(
            @PathVariable String processDefinitionId) {
        
        try {
            InputStream inputStream = processDefinitionService.getProcessDiagram(processDefinitionId);
            byte[] bytes = inputStream.readAllBytes();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("attachment", "process-diagram.png");
            
            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 获取流程模型XML
     */
    @GetMapping("/{processDefinitionId}/xml")
    public ResponseEntity<Map<String, Object>> getProcessModelXML(
            @PathVariable String processDefinitionId) {
        
        String xml = processDefinitionService.getProcessModelXML(processDefinitionId);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "xml", xml
        ));
    }

    /**
     * 启动流程实例
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startProcessInstance(
            @RequestParam String processDefinitionKey,
            @RequestParam(required = false) String businessKey,
            @RequestBody(required = false) Map<String, Object> variables) {
        
        try {
            System.out.println("开始启动流程实例，processDefinitionKey: " + processDefinitionKey + 
                              ", businessKey: " + businessKey + 
                              ", variables: " + (variables != null ? variables.toString() : "null"));
            
            // 参数验证
            if (processDefinitionKey == null || processDefinitionKey.trim().isEmpty()) {
                throw new IllegalArgumentException("流程定义key不能为空");
            }
            
            String processInstanceId = processDefinitionService.startProcessInstance(
                    processDefinitionKey, 
                    businessKey,
                    variables != null ? variables : new HashMap<>());
            
            System.out.println("流程实例启动成功，processInstanceId: " + processInstanceId);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "流程实例启动成功",
                    "processInstanceId", processInstanceId
            ));
        } catch (Exception e) {
            System.err.println("启动流程实例失败: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "启动流程实例失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 自动部署leave流程定义
     * 从类路径加载leave-process.bpmn文件并部署
     */
    @PostMapping("/deploy/leave")
    public ResponseEntity<Map<String, Object>> deployLeaveProcess() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("[Controller] 开始部署Leave流程");
            
            // 调用服务层方法部署leave流程
            String deploymentId = processDefinitionService.deployLeaveProcess();
            
            System.out.println("[Controller] Leave流程部署成功，部署ID: " + deploymentId);
            
            result.put("success", true);
            result.put("message", "Leave流程部署成功");
            result.put("deploymentId", deploymentId);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("[Controller] Leave流程部署失败: " + e.getMessage());
            e.printStackTrace();
            
            result.put("success", false);
            result.put("message", "Leave流程部署失败: " + e.getMessage());
            result.put("errorDetails", e.toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 测试端点 - 直接返回流程定义列表（用于测试）
     */
    @GetMapping("/test/process/list")
    public ResponseEntity<Map<String, Object>> testGetProcessList() {
        System.out.println("[Test Controller] 测试获取流程定义列表");
        return getProcessDefinitionList(1, 10);
    }
}