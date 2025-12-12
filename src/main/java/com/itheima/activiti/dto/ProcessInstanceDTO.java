package com.itheima.activiti.dto;

import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * 流程实例DTO
 */
@Data
public class ProcessInstanceDTO {
    private String id;
    private String processDefinitionId;
    private String processDefinitionName;
    private String processDefinitionKey;
    private int processDefinitionVersion;
    private String businessKey;
    private boolean suspended;
    private String initiator;
    private Date startTime;
    private Date endTime;
    private Map<String, Object> variables;
}