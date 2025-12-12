package com.itheima.activiti.mapper;

import com.itheima.activiti.entity.Tenant;

import java.util.List;

/**
 * 租户Mapper接口
 */
public interface TenantMapper {
    
    /**
     * 插入租户
     */
    int insert(Tenant tenant);
    
    /**
     * 更新租户
     */
    int update(Tenant tenant);
    
    /**
     * 根据ID查询租户
     */
    Tenant selectById(String id);
    
    /**
     * 根据APP ID查询租户
     */
    Tenant selectByAppId(String appId);
    
    /**
     * 根据联系人手机号查询租户
     */
    Tenant selectByContactPhone(String contactPhone);
    
    /**
     * 根据联系人邮箱查询租户
     */
    Tenant selectByContactEmail(String contactEmail);
    
    /**
     * 查询所有租户
     */
    List<Tenant> selectAll();
    
    /**
     * 根据状态查询租户
     */
    List<Tenant> selectByStatus(String status);
    
    /**
     * 删除租户
     */
    int delete(String id);
}