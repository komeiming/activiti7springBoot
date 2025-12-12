package com.itheima.activiti.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.itheima.activiti.service.ProcessDefinitionService;

/**
 * 流程部署控制器
 */
@RestController
@RequestMapping("/api/deployment")
@Tag(name = "流程部署管理", description = "提供流程部署相关的API接口")
public class ProcessDeploymentController {

    @Autowired
    private ProcessDefinitionService processDefinitionService;
    
    /**
     * 部署单个流程文件
     */
    @PostMapping("/single")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    @Operation(summary = "部署单个流程文件", description = "部署单个.bpmn或.bpmn20.xml格式的流程文件")
    @Parameter(name = "file", description = "流程文件", required = true, content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "multipart/form-data"))
    public ResponseEntity<Map<String, Object>> deploySingleFile(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 调用流程定义服务部署流程文件
            boolean success = processDefinitionService.deployProcess(file);
            if (success) {
                response.put("success", true);
                response.put("message", "流程部署成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "流程部署失败");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "流程部署失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 部署员工onboard流程（预定义流程）
     */
    @PostMapping("/onboard-process")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    @Operation(summary = "部署员工onboard流程", description = "部署系统预定义的员工入职流程")
    public ResponseEntity<Map<String, Object>> deployOnboardProcess() {
        Map<String, Object> response = new HashMap<>();
        try {
            // 部署预定义的onboard流程
            String deploymentId = processDefinitionService.deployOnboardProcess();
            response.put("success", true);
            response.put("message", "员工onboard流程部署成功");
            response.put("deploymentId", deploymentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "员工onboard流程部署失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 查询所有部署
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('PROCESS_SERVICE') or hasRole('INTEGRATED_SERVICE')")
    @Operation(summary = "查询所有部署", description = "获取系统中所有的流程部署信息")
    public ResponseEntity<Map<String, Object>> getDeployments() {
        Map<String, Object> response = new HashMap<>();
        try {
            // 获取所有流程定义，从流程定义中获取部署信息
            List<ProcessDefinition> processDefinitions = processDefinitionService.getAllProcessDefinitions();
            
            // 转换为简单的Map对象，避免Activiti引擎内部状态的序列化问题
            List<Map<String, Object>> deploymentData = new ArrayList<>();
            for (ProcessDefinition pd : processDefinitions) {
                Map<String, Object> pdMap = new HashMap<>();
                pdMap.put("id", pd.getId());
                pdMap.put("key", pd.getKey());
                pdMap.put("name", pd.getName());
                pdMap.put("version", pd.getVersion());
                pdMap.put("resourceName", pd.getResourceName());
                pdMap.put("deploymentId", pd.getDeploymentId());
                pdMap.put("suspended", pd.isSuspended());
                pdMap.put("tenantId", pd.getTenantId());
                deploymentData.add(pdMap);
            }
            
            response.put("success", true);
            response.put("data", deploymentData);
            response.put("total", deploymentData.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取部署列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 根据部署ID查询部署详情
     */
    @GetMapping("/{deploymentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('PROCESS_SERVICE') or hasRole('INTEGRATED_SERVICE')")
    @Operation(summary = "查询部署详情", description = "根据部署ID获取部署的详细信息")
    @Parameter(name = "deploymentId", description = "部署ID", required = true)
    public ResponseEntity<Map<String, Object>> getDeploymentById(@PathVariable String deploymentId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 根据部署ID获取部署详情
            // 由于ProcessDefinitionService没有直接获取部署详情的方法，这里可以通过流程定义查询
            List<ProcessDefinition> processDefinitions = processDefinitionService.getAllProcessDefinitions();
            List<ProcessDefinition> deploymentProcesses = new ArrayList<>();
            for (ProcessDefinition pd : processDefinitions) {
                if (pd.getDeploymentId().equals(deploymentId)) {
                    deploymentProcesses.add(pd);
                }
            }
            
            if (deploymentProcesses.isEmpty()) {
                response.put("success", false);
                response.put("message", "部署不存在");
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> deploymentInfo = new HashMap<>();
            deploymentInfo.put("id", deploymentId);
            deploymentInfo.put("processDefinitions", deploymentProcesses);
            
            response.put("success", true);
            response.put("data", deploymentInfo);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取部署详情失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 删除部署
     */
    @DeleteMapping("/{deploymentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除部署", description = "根据部署ID删除部署，支持级联删除")
    @Parameters({
        @Parameter(name = "deploymentId", description = "部署ID", required = true),
        @Parameter(name = "cascade", description = "是否级联删除", required = false, schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "false"))
    })
    public ResponseEntity<Map<String, Object>> deleteDeployment(@PathVariable String deploymentId, 
                                                               @RequestParam(required = false, defaultValue = "false") boolean cascade) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 删除部署
            boolean success = processDefinitionService.deleteProcessDefinition(deploymentId, cascade);
            if (success) {
                response.put("success", true);
                response.put("message", "部署删除成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "部署删除失败");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "部署删除失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}