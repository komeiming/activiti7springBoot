package com.itheima.activiti.service;

import com.itheima.activiti.entity.Permission;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

/**
 * 菜单服务接口
 */
public interface MenuService {
    
    /**
     * 查询所有菜单，支持按语言代码查询
     */
    List<Permission> getAllMenus(String languageCode);
    
    /**
     * 根据ID查询菜单
     */
    Permission getMenuById(Long id);
    
    /**
     * 创建菜单
     */
    Permission createMenu(Permission menu);
    
    /**
     * 更新菜单
     */
    Permission updateMenu(Permission menu);
    
    /**
     * 删除菜单
     */
    boolean deleteMenu(Long id);
    
    /**
     * 批量删除菜单
     */
    boolean batchDeleteMenus(List<Long> ids);
    
    /**
     * 导出菜单数据
     */
    List<Permission> exportMenus();
    
    /**
     * 导入菜单数据
     */
    boolean importMenus(List<Permission> menus, boolean override);
    
    /**
     * 刷新菜单缓存
     */
    void refreshMenuCache();
}
