package com.itheima.activiti.service.impl;

import com.itheima.activiti.mapper.TemplateOperationLogMapper;
import com.itheima.activiti.entity.TemplateOperationLog;
import com.itheima.activiti.service.TemplateOperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * 模板操作日志服务实现类
 */
@Service
public class TemplateOperationLogServiceImpl implements TemplateOperationLogService {
    
    @Autowired
    private TemplateOperationLogMapper templateOperationLogMapper;
    
    // 实现基本CRUD方法
    @Override
    public List<TemplateOperationLog> list() {
        try {
            return templateOperationLogMapper.selectAll();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    @Override
    public TemplateOperationLog getById(String id) {
        try {
            return templateOperationLogMapper.selectById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public boolean save(TemplateOperationLog log) {
        try {
            // 生成唯一ID
            if (log.getId() == null) {
                log.setId(java.util.UUID.randomUUID().toString());
            }
            return templateOperationLogMapper.insert(log) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 记录模板操作日志
     */
    @Override
    @Transactional
    public void recordOperationLog(TemplateOperationLog log) {
        // 保存操作日志
        save(log);
    }
    
    /**
     * 按条件查询日志
     */
    @Override
    public List<TemplateOperationLog> getLogList(Map<String, Object> params) {
        // 转换分页参数：pageNum和pageSize -> offset和limit
        if (params.containsKey("pageNum") && params.containsKey("pageSize")) {
            try {
                Integer pageNum = Integer.parseInt(params.get("pageNum").toString());
                Integer pageSize = Integer.parseInt(params.get("pageSize").toString());
                Integer offset = (pageNum - 1) * pageSize;
                params.put("offset", offset);
                params.put("limit", pageSize);
            } catch (Exception e) {
                System.err.println("转换分页参数失败: " + e.getMessage());
            }
        }
        
        // 转换操作类型为小写，与数据库存储一致
        if (params.containsKey("operationType")) {
            Object operationType = params.get("operationType");
            if (operationType != null && operationType instanceof String) {
                params.put("operationType", ((String) operationType).toLowerCase());
            }
        }
        
        // 获取日志列表
        List<TemplateOperationLog> logs = templateOperationLogMapper.selectByParams(params);
        
        // 将返回的操作类型转换为大写，与前端期望的格式一致
        logs.forEach(log -> {
            if (log.getOperationType() != null) {
                log.setOperationType(log.getOperationType().toUpperCase());
            }
        });
        
        return logs;
    }
    
    /**
     * 根据模板ID查询操作日志
     */
    @Override
    public List<TemplateOperationLog> findByTemplateId(String templateId) {
        List<TemplateOperationLog> logs = templateOperationLogMapper.findByTemplateId(templateId);
        // 将返回的操作类型转换为大写，与前端期望的格式一致
        logs.forEach(log -> {
            if (log.getOperationType() != null) {
                log.setOperationType(log.getOperationType().toUpperCase());
            }
        });
        return logs;
    }
    
    /**
     * 根据操作类型查询日志
     */
    @Override
    public List<TemplateOperationLog> findByOperationType(String operationType) {
        List<TemplateOperationLog> logs = templateOperationLogMapper.findByOperationType(operationType.toLowerCase());
        // 将返回的操作类型转换为大写，与前端期望的格式一致
        logs.forEach(log -> {
            if (log.getOperationType() != null) {
                log.setOperationType(log.getOperationType().toUpperCase());
            }
        });
        return logs;
    }
    
    /**
     * 统计日志数量
     */
    @Override
    public int countLogs(Map<String, Object> condition) {
        // 转换操作类型为小写，与数据库存储一致
        if (condition.containsKey("operationType")) {
            Object operationType = condition.get("operationType");
            if (operationType != null && operationType instanceof String) {
                condition.put("operationType", ((String) operationType).toLowerCase());
            }
        }
        return templateOperationLogMapper.countLogs(condition);
    }
    
    /**
     * 批量记录操作日志
     */
    @Override
    @Transactional
    public void batchRecordLogs(List<TemplateOperationLog> logs) {
        if (logs != null && !logs.isEmpty()) {
            // 逐个保存操作日志
            for (TemplateOperationLog log : logs) {
                templateOperationLogMapper.insert(log);
            }
        }
    }
}
