package com.itheima.activiti.service;

import com.itheima.activiti.entity.WorkflowTemplate;

import java.util.List;
import java.util.Map;

/**
 * 工作流模板服务接口
 */
public interface WorkflowTemplateService {

    /**
     * 创建工作流模板
     * @param template 工作流模板
     * @return 创建成功的模板
     */
    WorkflowTemplate createTemplate(WorkflowTemplate template);

    /**
     * 查询工作流模板列表
     * @param params 查询参数
     * @return 模板列表
     */
    List<WorkflowTemplate> queryTemplates(Map<String, Object> params);

    /**
     * 查询工作流模板总数
     * @param params 查询参数
     * @return 模板总数
     */
    long countTemplates(Map<String, Object> params);

    /**
     * 根据ID获取工作流模板
     * @param id 模板ID
     * @return 工作流模板
     */
    WorkflowTemplate getTemplateById(String id);

    /**
     * 更新工作流模板
     * @param template 工作流模板
     * @return 更新后的模板
     */
    WorkflowTemplate updateTemplate(WorkflowTemplate template);

    /**
     * 删除工作流模板
     * @param id 模板ID
     * @return 删除结果
     */
    boolean deleteTemplate(String id);

    /**
     * 发布工作流模板
     * @param id 模板ID
     * @return 发布结果
     */
    boolean publishTemplate(String id);

    /**
     * 下架工作流模板
     * @param id 模板ID
     * @return 下架结果
     */
    boolean offlineTemplate(String id);
}
