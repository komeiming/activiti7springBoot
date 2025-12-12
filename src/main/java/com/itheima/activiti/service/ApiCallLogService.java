package com.itheima.activiti.service;

import com.itheima.activiti.entity.ApiCallLog;

import java.util.List;
import java.util.Map;

/**
 * API调用日志服务接口
 */
public interface ApiCallLogService {
    
    /**
     * 保存API调用日志
     */
    void saveApiCallLog(ApiCallLog apiCallLog);
    
    /**
     * 根据条件查询API调用日志
     */
    List<ApiCallLog> getApiCallLogs(Map<String, Object> params);
    
    /**
     * 查询API调用日志总数
     */
    int countApiCallLogs(Map<String, Object> params);
    
    /**
     * 根据APP ID查询API调用统计
     */
    List<Map<String, Object>> getApiCallStatsByAppId(Map<String, Object> params);
    
    /**
     * 查询租户调用统计
     */
    Map<String, Object> getTenantCallStats(String tenantId, String startTime, String endTime);
}