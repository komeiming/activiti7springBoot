package com.itheima.activiti.service.impl;

import com.itheima.activiti.entity.MenuOperationLog;
import com.itheima.activiti.entity.Permission;
import com.itheima.activiti.mapper.MenuOperationLogMapper;
import com.itheima.activiti.mapper.PermissionMapper;
import com.itheima.activiti.service.MenuService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单服务接口实现类
 */
@Service
public class MenuServiceImpl implements MenuService {

    private static final Logger logger = LoggerFactory.getLogger(MenuServiceImpl.class);

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private MenuOperationLogMapper menuOperationLogMapper;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 记录菜单操作日志
     */
    private void logMenuOperation(Long menuId, String operationType, String description, Object oldData, Object newData) {
        try {
            MenuOperationLog log = new MenuOperationLog();
            log.setMenuId(menuId);
            log.setOperationType(operationType);
            log.setOperator(SecurityContextHolder.getContext().getAuthentication().getName());
            log.setOperationTime(LocalDateTime.now());
            log.setOldData(oldData != null ? objectMapper.writeValueAsString(oldData) : null);
            log.setNewData(newData != null ? objectMapper.writeValueAsString(newData) : null);
            log.setDescription(description);

            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                log.setIpAddress(request.getRemoteAddr());
                log.setUserAgent(request.getHeader("User-Agent"));
            }

            menuOperationLogMapper.insert(log);
        } catch (Exception e) {
            logger.error("记录菜单操作日志失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 查询所有菜单，支持按语言代码查询
     */
    @Override
    @Cacheable(value = "menus", key = "#languageCode == null ? 'default' : #languageCode")
    public List<Permission> getAllMenus(String languageCode) {
        logger.info("查询所有菜单，语言代码：{}", languageCode);
        List<Permission> allMenus = permissionMapper.findAll();
        
        // 设置本地化属性
        allMenus.forEach(menu -> menu.setLocalizedProperties(languageCode));
        
        // 构建树形结构
        return buildMenuTree(allMenus);
    }

    /**
     * 根据ID查询菜单
     */
    @Override
    public Permission getMenuById(Long id) {
        logger.info("根据ID查询菜单：{}", id);
        List<Permission> allMenus = permissionMapper.findAll();
        for (Permission menu : allMenus) {
            if (menu.getId().equals(id)) {
                return menu;
            }
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                Permission childMenu = findMenuByIdRecursive(menu.getChildren(), id);
                if (childMenu != null) {
                    return childMenu;
                }
            }
        }
        return null;
    }

    /**
     * 递归查找菜单
     */
    private Permission findMenuByIdRecursive(List<Permission> menus, Long id) {
        for (Permission menu : menus) {
            if (menu.getId().equals(id)) {
                return menu;
            }
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                Permission childMenu = findMenuByIdRecursive(menu.getChildren(), id);
                if (childMenu != null) {
                    return childMenu;
                }
            }
        }
        return null;
    }

    /**
     * 创建菜单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "menus", allEntries = true)
    public Permission createMenu(Permission menu) {
        logger.info("创建菜单：{}", menu.getName());
        permissionMapper.insert(menu);
        logMenuOperation(menu.getId(), "create", "创建菜单", null, menu);
        return menu;
    }

    /**
     * 更新菜单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "menus", allEntries = true)
    public Permission updateMenu(Permission menu) {
        logger.info("更新菜单：{}", menu.getId());
        Permission oldMenu = permissionMapper.findById(menu.getId());
        permissionMapper.update(menu);
        logMenuOperation(menu.getId(), "update", "更新菜单", oldMenu, menu);
        return menu;
    }

    /**
     * 删除菜单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "menus", allEntries = true)
    public boolean deleteMenu(Long id) {
        logger.info("删除菜单：{}", id);
        Permission oldMenu = permissionMapper.findById(id);
        boolean result = permissionMapper.delete(id) > 0;
        if (result) {
            logMenuOperation(id, "delete", "删除菜单", oldMenu, null);
        }
        return result;
    }

    /**
     * 批量删除菜单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "menus", allEntries = true)
    public boolean batchDeleteMenus(List<Long> ids) {
        logger.info("批量删除菜单：{}", ids);
        List<Permission> oldMenus = new ArrayList<>();
        for (Long id : ids) {
            Permission oldMenu = permissionMapper.findById(id);
            if (oldMenu != null) {
                oldMenus.add(oldMenu);
            }
        }
        // 注意：这里的批量删除SQL需要修改，使用MyBatis的动态SQL
        boolean result = false;
        for (Long id : ids) {
            result = permissionMapper.delete(id) > 0 || result;
        }
        if (result) {
            logMenuOperation(null, "batch_delete", "批量删除菜单", oldMenus, null);
        }
        return result;
    }

    /**
     * 导出菜单数据
     */
    @Override
    public List<Permission> exportMenus() {
        logger.info("导出菜单数据");
        List<Permission> allMenus = permissionMapper.findAll();
        logMenuOperation(null, "export", "导出菜单", null, null);
        return buildMenuTree(allMenus);
    }

    /**
     * 导入菜单数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "menus", allEntries = true)
    public boolean importMenus(List<Permission> menus, boolean override) {
        logger.info("导入菜单数据，是否覆盖：{}", override);
        
        // 如果需要覆盖现有菜单，先删除所有菜单
        if (override) {
            List<Permission> allMenus = permissionMapper.findAll();
            for (Permission menu : allMenus) {
                permissionMapper.delete(menu.getId());
            }
        }
        
        // 扁平化菜单列表，便于处理父子关系
        List<Permission> flatMenus = new ArrayList<>();
        flattenMenuTree(menus, flatMenus);
        
        // 导入菜单，确保父菜单先于子菜单被创建
        boolean result = true;
        for (Permission menu : flatMenus) {
            if (menu.getParentId() == null || menu.getParentId() == 0) {
                // 根菜单，直接创建或更新
                if (override || permissionMapper.findById(menu.getId()) == null) {
                    if (permissionMapper.findById(menu.getId()) != null) {
                        permissionMapper.update(menu);
                    } else {
                        permissionMapper.insert(menu);
                    }
                }
            } else {
                // 子菜单，确保父菜单已存在
                if (permissionMapper.findById(menu.getParentId()) != null) {
                    if (override || permissionMapper.findById(menu.getId()) == null) {
                        if (permissionMapper.findById(menu.getId()) != null) {
                            permissionMapper.update(menu);
                        } else {
                            permissionMapper.insert(menu);
                        }
                    }
                } else {
                    logger.error("导入菜单失败：父菜单 {} 不存在", menu.getParentId());
                    result = false;
                }
            }
        }
        
        if (result) {
            logMenuOperation(null, "import", "导入菜单", null, menus);
        }
        
        return result;
    }
    
    /**
     * 扁平化菜单树
     */
    private void flattenMenuTree(List<Permission> menus, List<Permission> flatMenus) {
        for (Permission menu : menus) {
            flatMenus.add(menu);
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                flattenMenuTree(menu.getChildren(), flatMenus);
            }
        }
    }

    /**
     * 刷新菜单缓存
     */
    @Override
    public void refreshMenuCache() {
        logger.info("刷新菜单缓存");
        // 清除所有菜单缓存
        cacheManager.getCache("menus").clear();
    }

    /**
     * 构建菜单树形结构
     */
    private List<Permission> buildMenuTree(List<Permission> menus) {
        // 将菜单转换为Map，便于查找父菜单
        Map<Long, Permission> menuMap = menus.stream()
                .collect(Collectors.toMap(Permission::getId, menu -> {
                    menu.setChildren(new ArrayList<>());
                    return menu;
                }));

        // 构建树形结构
        List<Permission> rootMenus = new ArrayList<>();
        for (Permission menu : menus) {
            if (menu.getParentId() == null || menu.getParentId() == 0) {
                rootMenus.add(menu);
            } else {
                Permission parentMenu = menuMap.get(menu.getParentId());
                if (parentMenu != null) {
                    parentMenu.getChildren().add(menu);
                }
            }
        }

        // 按menuOrder排序根菜单
        rootMenus.sort((m1, m2) -> {
            Integer order1 = m1.getMenuOrder() != null ? m1.getMenuOrder() : 0;
            Integer order2 = m2.getMenuOrder() != null ? m2.getMenuOrder() : 0;
            return order1.compareTo(order2);
        });

        // 递归排序子菜单
        sortChildren(rootMenus);

        return rootMenus;
    }
    
    /**
     * 递归排序子菜单
     */
    private void sortChildren(List<Permission> menus) {
        for (Permission menu : menus) {
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                menu.getChildren().sort((m1, m2) -> {
                    Integer order1 = m1.getMenuOrder() != null ? m1.getMenuOrder() : 0;
                    Integer order2 = m2.getMenuOrder() != null ? m2.getMenuOrder() : 0;
                    return order1.compareTo(order2);
                });
                sortChildren(menu.getChildren());
            }
        }
    }
}