package com.itheima.activiti.mapper;

import com.itheima.activiti.entity.ApiCallLog;

import java.util.List;
import java.util.Map;

/**
 * API调用日志Mapper接口
 */
public interface ApiCallLogMapper {
    
    /**
     * 插入API调用日志
     */
    int insert(ApiCallLog apiCallLog);
    
    /**
     * 根据条件查询API调用日志
     */
    List<ApiCallLog> selectByCondition(Map<String, Object> params);
    
    /**
     * 查询API调用日志总数
     */
    int countByCondition(Map<String, Object> params);
    
    /**
     * 根据APP ID查询API调用统计
     */
    List<Map<String, Object>> selectStatsByAppId(Map<String, Object> params);
}