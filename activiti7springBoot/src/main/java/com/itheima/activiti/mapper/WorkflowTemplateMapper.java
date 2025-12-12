package com.itheima.activiti.mapper;

import com.itheima.activiti.entity.WorkflowTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 工作流模板Mapper接口
 */
@Mapper
public interface WorkflowTemplateMapper {

    /**
     * 插入工作流模板
     * @param template 工作流模板
     * @return 插入行数
     */
    int insert(WorkflowTemplate template);

    /**
     * 根据ID更新工作流模板
     * @param template 工作流模板
     * @return 更新行数
     */
    int updateById(WorkflowTemplate template);

    /**
     * 根据ID删除工作流模板
     * @param id 模板ID
     * @return 删除行数
     */
    int deleteById(String id);

    /**
     * 根据条件查询工作流模板列表
     * @param params 查询条件
     * @return 模板列表
     */
    List<WorkflowTemplate> selectTemplatesByCondition(@Param("params") Map<String, Object> params);

    /**
     * 根据条件查询工作流模板总数
     * @param params 查询条件
     * @return 模板总数
     */
    int selectTemplateCountByCondition(@Param("params") Map<String, Object> params);

    /**
     * 根据模板ID查询模板详情
     * @param id 模板ID
     * @return 模板详情
     */
    WorkflowTemplate selectTemplateById(@Param("id") String id);

    /**
     * 根据租户ID查询模板列表
     * @param tenantId 租户ID
     * @return 模板列表
     */
    List<WorkflowTemplate> selectTemplatesByTenantId(@Param("tenantId") String tenantId);
}
