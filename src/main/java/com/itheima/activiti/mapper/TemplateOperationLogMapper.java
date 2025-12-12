package com.itheima.activiti.mapper;

import com.itheima.activiti.entity.TemplateOperationLog;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

/**
 * 模板操作日志Mapper接口
 */
@Mapper
public interface TemplateOperationLogMapper {
    
    List<TemplateOperationLog> selectAll();
    
    TemplateOperationLog selectById(String id);
    
    int insert(TemplateOperationLog log);
    
    // 按条件查询日志
    List<TemplateOperationLog> selectByParams(Map<String, Object> params);
    
    // 根据模板ID查询操作日志
    List<TemplateOperationLog> findByTemplateId(String templateId);
    
    // 根据操作类型查询日志
    List<TemplateOperationLog> findByOperationType(String operationType);
    
    // 统计日志数量
    int countLogs(Map<String, Object> condition);
}
