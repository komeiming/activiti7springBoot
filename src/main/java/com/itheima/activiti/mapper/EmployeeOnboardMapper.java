package com.itheima.activiti.mapper;

import com.itheima.activiti.entity.EmployeeOnboard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 员工onboard流程数据访问层
 */
@Mapper
public interface EmployeeOnboardMapper {
    
    /**
     * 根据流程实例ID查询onboard记录
     */
    EmployeeOnboard findByProcessInstanceId(@Param("processInstanceId") String processInstanceId);
    
    /**
     * 根据ID查询onboard记录
     */
    EmployeeOnboard findById(@Param("id") Long id);
    
    /**
     * 查询所有onboard记录
     */
    List<EmployeeOnboard> findAll();
    
    /**
     * 根据部门查询onboard记录
     */
    List<EmployeeOnboard> findByDepartment(@Param("department") String department);
    
    /**
     * 新增onboard记录
     */
    int insert(EmployeeOnboard employeeOnboard);
    
    /**
     * 更新onboard记录
     */
    int update(EmployeeOnboard employeeOnboard);
    
    /**
     * 更新IT设备状态
     */
    int updateItEquipmentStatus(@Param("processInstanceId") String processInstanceId, @Param("status") String status);
    
    /**
     * 更新HR文档状态
     */
    int updateHrDocumentStatus(@Param("processInstanceId") String processInstanceId, @Param("status") String status);
    
    /**
     * 更新培训状态
     */
    int updateTrainingStatus(@Param("processInstanceId") String processInstanceId, @Param("status") String status);
    
    /**
     * 删除onboard记录
     */
    int delete(@Param("id") Long id);
}