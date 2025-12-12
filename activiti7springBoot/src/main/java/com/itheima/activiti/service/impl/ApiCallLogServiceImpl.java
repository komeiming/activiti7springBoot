package com.itheima.activiti.service.impl;

import com.itheima.activiti.entity.ApiCallLog;
import com.itheima.activiti.mapper.ApiCallLogMapper;
import com.itheima.activiti.service.ApiCallLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * API调用日志服务实现类
 */
@Service
public class ApiCallLogServiceImpl implements ApiCallLogService {
    
    @Autowired
    private ApiCallLogMapper apiCallLogMapper;
    
    /**
     * 保存API调用日志
     */
    @Override
    public void saveApiCallLog(ApiCallLog apiCallLog) {
        apiCallLogMapper.insert(apiCallLog);
    }
    
    /**
     * 根据条件查询API调用日志
     */
    @Override
    public List<ApiCallLog> getApiCallLogs(Map<String, Object> params) {
        return apiCallLogMapper.selectByCondition(params);
    }
    
    /**
     * 查询API调用日志总数
     */
    @Override
    public int countApiCallLogs(Map<String, Object> params) {
        return apiCallLogMapper.countByCondition(params);
    }
    
    /**
     * 根据APP ID查询API调用统计
     */
    @Override
    public List<Map<String, Object>> getApiCallStatsByAppId(Map<String, Object> params) {
        return apiCallLogMapper.selectStatsByAppId(params);
    }
    
    /**
     * 查询租户调用统计
     */
    @Override
    public Map<String, Object> getTenantCallStats(String tenantId, String startTime, String endTime) {
        // 这里可以实现租户调用统计逻辑
        // 包括调用次数、成功率、响应时间等统计信息
        return null;
    }
}