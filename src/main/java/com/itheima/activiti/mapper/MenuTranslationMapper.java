package com.itheima.activiti.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MenuTranslationMapper {
    
    /**
     * 查询所有菜单翻译
     */
    @Select("SELECT * FROM sys_menu_translation")
    List<com.itheima.activiti.entity.MenuTranslation> findAll();
    
    /**
     * 根据菜单ID查询翻译
     */
    @Select("SELECT * FROM sys_menu_translation WHERE menu_id = #{menuId}")
    List<com.itheima.activiti.entity.MenuTranslation> findByMenuId(Long menuId);
    
    /**
     * 根据语言代码查询所有菜单翻译
     */
    @Select("SELECT * FROM sys_menu_translation WHERE language_code = #{languageCode}")
    List<com.itheima.activiti.entity.MenuTranslation> findByLanguageCode(String languageCode);
    
    /**
     * 根据菜单ID和语言代码查询翻译
     */
    @Select("SELECT * FROM sys_menu_translation WHERE menu_id = #{menuId} AND language_code = #{languageCode}")
    com.itheima.activiti.entity.MenuTranslation findByMenuIdAndLanguageCode(Long menuId, String languageCode);
}
