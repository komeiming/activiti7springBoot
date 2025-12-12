package com.itheima.activiti.dto;

import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * 任务DTO
 */
@Data
public class TaskDTO {
    private String id;
    private String name;
    private String description;
    private String assignee;
    private String owner;
    private String processInstanceId;
    private String processDefinitionId;
    private String taskDefinitionKey;
    private String parentTaskId;
    private Date createTime;
    private Date dueDate;
    private int priority;
    private String category;
    private Map<String, Object> variables;
}