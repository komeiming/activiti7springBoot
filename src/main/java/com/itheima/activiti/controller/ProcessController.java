package com.itheima.activiti.controller;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/process")
public class ProcessController {
    private static final Logger logger = Logger.getLogger(ProcessController.class.getName());
    
    @Autowired
    private RepositoryService repositoryService;

    // 上传BPMN文件 - 实际部署到Activiti引擎
    @PostMapping("/upload-bpmn")
    public ResponseEntity<Map<String, Object>> uploadBpmn(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 验证文件
            if (file == null || file.isEmpty()) {
                response.put("success", false);
                response.put("message", "文件不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 支持.bpmn和.bpmn20.xml两种格式
            String filename = file.getOriginalFilename();
            if (!filename.endsWith(".bpmn") && !filename.endsWith(".bpmn20.xml")) {
                response.put("success", false);
                response.put("message", "请上传.bpmn或.bpmn20.xml格式的文件");
                return ResponseEntity.badRequest().body(response);
            }

            // 实际部署流程定义到Activiti引擎
            try (InputStream inputStream = file.getInputStream()) {
                DeploymentBuilder deploymentBuilder = repositoryService.createDeployment()
                    .name("流程部署-" + filename)
                    .addInputStream(filename, inputStream)
                    .enableDuplicateFiltering();
                
                String deploymentId = deploymentBuilder.deploy().getId();
                logger.info("流程部署成功，部署ID: " + deploymentId);
                
                // 查找部署的流程定义
                ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .deploymentId(deploymentId)
                    .singleResult();
                
                response.put("success", true);
                response.put("message", "文件 " + file.getOriginalFilename() + " 上传并部署成功");
                response.put("deploymentId", deploymentId);
                if (processDefinition != null) {
                    response.put("processDefinitionId", processDefinition.getId());
                    response.put("processDefinitionKey", processDefinition.getKey());
                    response.put("processDefinitionName", processDefinition.getName());
                }
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            logger.severe("流程部署失败: " + e.getMessage());
            response.put("success", false);
            response.put("message", "上传部署失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 删除流程定义 - 实际删除Activiti引擎中的流程定义
    @DeleteMapping("/definition/{definitionId}")
    public ResponseEntity<Map<String, Object>> deleteProcessDefinition(@PathVariable String definitionId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 查找流程定义
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(definitionId)
                .singleResult();
                
            if (processDefinition == null) {
                response.put("success", false);
                response.put("message", "流程定义不存在: " + definitionId);
                return ResponseEntity.notFound().build();
            }
            
            // 实际删除流程定义，true表示级联删除相关的流程实例和历史数据
            repositoryService.deleteDeployment(processDefinition.getDeploymentId(), true);
            logger.info("流程定义删除成功，定义ID: " + definitionId);
            
            response.put("success", true);
            response.put("message", "流程定义删除成功");
            response.put("definitionId", definitionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("流程定义删除失败: " + e.getMessage());
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 暂停流程定义 - 实际暂停Activiti引擎中的流程定义
    @PutMapping("/definition/{definitionId}/suspend")
    public ResponseEntity<Map<String, Object>> suspendProcessDefinition(@PathVariable String definitionId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 查找流程定义
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(definitionId)
                .singleResult();
                
            if (processDefinition == null) {
                response.put("success", false);
                response.put("message", "流程定义不存在: " + definitionId);
                return ResponseEntity.notFound().build();
            }
            
            // 检查是否已经是暂停状态
            if (processDefinition.isSuspended()) {
                response.put("success", true);
                response.put("message", "流程定义已经处于暂停状态");
                response.put("definitionId", definitionId);
                response.put("status", "suspended");
                return ResponseEntity.ok(response);
            }
            
            // 实际暂停流程定义，true表示同时暂停所有流程实例
            repositoryService.suspendProcessDefinitionById(definitionId, true, null);
            logger.info("流程定义暂停成功，定义ID: " + definitionId);
            
            response.put("success", true);
            response.put("message", "流程定义已暂停");
            response.put("definitionId", definitionId);
            response.put("status", "suspended");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("流程定义暂停失败: " + e.getMessage());
            response.put("success", false);
            response.put("message", "暂停失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 激活流程定义 - 实际激活Activiti引擎中的流程定义
    @PutMapping("/definition/{definitionId}/activate")
    public ResponseEntity<Map<String, Object>> activateProcessDefinition(@PathVariable String definitionId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 查找流程定义
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(definitionId)
                .singleResult();
                
            if (processDefinition == null) {
                response.put("success", false);
                response.put("message", "流程定义不存在: " + definitionId);
                return ResponseEntity.notFound().build();
            }
            
            // 检查是否已经是激活状态
            if (!processDefinition.isSuspended()) {
                response.put("success", true);
                response.put("message", "流程定义已经处于激活状态");
                response.put("definitionId", definitionId);
                response.put("status", "active");
                return ResponseEntity.ok(response);
            }
            
            // 实际激活流程定义，true表示同时激活所有流程实例
            repositoryService.activateProcessDefinitionById(definitionId, true, null);
            logger.info("流程定义激活成功，定义ID: " + definitionId);
            
            response.put("success", true);
            response.put("message", "流程定义已激活");
            response.put("definitionId", definitionId);
            response.put("status", "active");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("流程定义激活失败: " + e.getMessage());
            response.put("success", false);
            response.put("message", "激活失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
