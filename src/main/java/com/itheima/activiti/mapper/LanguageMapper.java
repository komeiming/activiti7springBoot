package com.itheima.activiti.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LanguageMapper {
    
    /**
     * 查询所有语言
     */
    @Select("SELECT * FROM sys_language")
    List<com.itheima.activiti.entity.Language> findAll();
    
    /**
     * 根据语言代码查询语言
     */
    @Select("SELECT * FROM sys_language WHERE code = #{code}")
    com.itheima.activiti.entity.Language findByCode(String code);
    
    /**
     * 查询默认语言
     */
    @Select("SELECT * FROM sys_language WHERE is_default = TRUE")
    com.itheima.activiti.entity.Language findDefault();
}
