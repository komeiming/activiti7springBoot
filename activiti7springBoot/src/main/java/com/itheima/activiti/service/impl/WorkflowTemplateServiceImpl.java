package com.itheima.activiti.service.impl;

import com.itheima.activiti.entity.WorkflowTemplate;
import com.itheima.activiti.mapper.WorkflowTemplateMapper;
import com.itheima.activiti.service.WorkflowTemplateService;
import com.itheima.activiti.common.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 工作流模板服务实现类
 */
@Service
public class WorkflowTemplateServiceImpl implements WorkflowTemplateService {

    @Autowired
    private WorkflowTemplateMapper workflowTemplateMapper;

    /**
     * 创建工作流模板
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowTemplate createTemplate(WorkflowTemplate template) {
        // 生成唯一ID
        template.setId(UUID.randomUUID().toString());
        // 设置默认状态为未发布
        if (template.getStatus() == null || template.getStatus().isEmpty()) {
            template.setStatus("draft");
        }
        // 设置创建和更新时间
        LocalDateTime now = LocalDateTime.now();
        template.setCreatedAt(now);
        template.setUpdatedAt(now);
        // 设置租户ID，只有当未设置时才从上下文获取
        if (template.getTenantId() == null || template.getTenantId().isEmpty()) {
            String tenantId = TenantContext.getTenantId();
            template.setTenantId(tenantId);
        }
        // 保存到数据库
        workflowTemplateMapper.insert(template);
        return template;
    }

    /**
     * 查询工作流模板列表
     */
    @Override
    public List<WorkflowTemplate> queryTemplates(Map<String, Object> params) {
        // 计算分页偏移量
        if (params.containsKey("page") && params.containsKey("size")) {
            int page = (int) params.get("page");
            int size = (int) params.get("size");
            int offset = (page - 1) * size;
            params.put("offset", offset);
        }
        // 添加租户ID条件
        String tenantId = TenantContext.getTenantId();
        params.put("tenantId", tenantId);
        // 创建一个包含params的map，因为mapper接口使用了@Param("params")注解
        Map<String, Object> mapperParams = new HashMap<>();
        mapperParams.put("params", params);
        return workflowTemplateMapper.selectTemplatesByCondition(mapperParams);
    }

    /**
     * 查询工作流模板总数
     */
    @Override
    public long countTemplates(Map<String, Object> params) {
        // 添加租户ID条件
        String tenantId = TenantContext.getTenantId();
        params.put("tenantId", tenantId);
        // 创建一个包含params的map，因为mapper接口使用了@Param("params")注解
        Map<String, Object> mapperParams = new HashMap<>();
        mapperParams.put("params", params);
        return workflowTemplateMapper.selectTemplateCountByCondition(mapperParams);
    }

    /**
     * 根据ID获取工作流模板
     */
    @Override
    public WorkflowTemplate getTemplateById(String id) {
        WorkflowTemplate template = workflowTemplateMapper.selectTemplateById(id);
        if (template != null) {
            // 验证租户ID，确保只能访问自己租户的模板
            String tenantId = TenantContext.getTenantId();
            if (!tenantId.equals(template.getTenantId())) {
                return null; // 或抛出异常
            }
        }
        return template;
    }

    /**
     * 更新工作流模板
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowTemplate updateTemplate(WorkflowTemplate template) {
        // 更新时间
        template.setUpdatedAt(LocalDateTime.now());
        // 保存到数据库
        workflowTemplateMapper.updateById(template);
        return template;
    }

    /**
     * 删除工作流模板
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTemplate(String id) {
        int result = workflowTemplateMapper.deleteById(id);
        return result > 0;
    }

    /**
     * 发布工作流模板
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishTemplate(String id) {
        WorkflowTemplate template = workflowTemplateMapper.selectTemplateById(id);
        if (template == null) {
            return false;
        }
        // 更新状态为已发布
        template.setStatus("published");
        template.setUpdatedAt(LocalDateTime.now());
        workflowTemplateMapper.updateById(template);
        return true;
    }

    /**
     * 下架工作流模板
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean offlineTemplate(String id) {
        WorkflowTemplate template = workflowTemplateMapper.selectTemplateById(id);
        if (template == null) {
            return false;
        }
        // 更新状态为已下架
        template.setStatus("offline");
        template.setUpdatedAt(LocalDateTime.now());
        workflowTemplateMapper.updateById(template);
        return true;
    }
}
