package com.itheima.activiti.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 菜单操作日志实体类
 */
@Data
public class MenuOperationLog {
    private Long id;
    private Long menuId; // 关联sys_permission表的id，为空表示批量操作
    private String operationType; // 操作类型：create, update, delete, batch_delete, batch_update
    private String operator; // 操作人
    private LocalDateTime operationTime; // 操作时间
    private String oldData; // 操作前数据，JSON格式
    private String newData; // 操作后数据，JSON格式
    private String description; // 操作描述
    private String ipAddress; // 操作人IP地址
    private String userAgent; // 操作人浏览器信息
}
