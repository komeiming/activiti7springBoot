package com.itheima.activiti.service;

import com.itheima.activiti.entity.TemplateOperationLog;
import java.util.List;
import java.util.Map;

/**
 * 模板操作日志服务接口
 */
public interface TemplateOperationLogService {
    
    // 基本CRUD方法
    List<TemplateOperationLog> list();
    TemplateOperationLog getById(String id);
    boolean save(TemplateOperationLog log);
    
    // 记录模板操作日志
    void recordOperationLog(TemplateOperationLog log);
    
    // 按条件查询日志
    List<TemplateOperationLog> getLogList(Map<String, Object> params);
    
    // 根据模板ID查询操作日志
    List<TemplateOperationLog> findByTemplateId(String templateId);
    
    // 根据操作类型查询日志
    List<TemplateOperationLog> findByOperationType(String operationType);
    
    // 统计日志数量
    int countLogs(Map<String, Object> condition);
    
    // 批量记录操作日志
    void batchRecordLogs(List<TemplateOperationLog> logs);
}
