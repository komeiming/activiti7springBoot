package com.itheima.activiti.dto;

import lombok.Data;

import java.util.Date;

/**
 * 流程定义DTO
 */
@Data
public class ProcessDefinitionDTO {
    private String id;
    private String name;
    private String key;
    private int version;
    private String resourceName;
    private String diagramResourceName;
    private String deploymentId;
    private boolean suspended;
    private Date deploymentTime;
}