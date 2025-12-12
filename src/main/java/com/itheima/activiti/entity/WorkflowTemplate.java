package com.itheima.activiti.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 工作流模板实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowTemplate {

    /**
     * 模板ID
     */
    private String id;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 流程JSON定义
     */
    private String processJson;

    /**
     * 模板状态：draft(未发布), published(已发布), offline(已下架)
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 更新人
     */
    private String updatedBy;
}
