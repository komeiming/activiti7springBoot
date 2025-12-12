package com.itheima.activiti.controller;

import com.itheima.activiti.dto.CommonResponse;
import com.itheima.activiti.entity.MenuOperationLog;
import com.itheima.activiti.entity.Permission;
import com.itheima.activiti.mapper.MenuOperationLogMapper;
import com.itheima.activiti.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器
 */
@RestController
@RequestMapping("/api/menus")
public class MenuController {

    @Autowired
    private MenuService menuService;

    @Autowired
    private MenuOperationLogMapper menuOperationLogMapper;

    /**
     * 获取所有菜单
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public CommonResponse<List<Permission>> getAllMenus(@RequestParam(required = false) String languageCode) {
        try {
            List<Permission> menus = menuService.getAllMenus(languageCode);
            return CommonResponse.success(menus);
        } catch (Exception e) {
            return CommonResponse.fail("获取菜单列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取菜单
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public CommonResponse<Permission> getMenuById(@PathVariable Long id) {
        try {
            Permission menu = menuService.getMenuById(id);
            return menu != null ? CommonResponse.success(menu) : CommonResponse.fail("菜单不存在");
        } catch (Exception e) {
            return CommonResponse.fail("获取菜单失败: " + e.getMessage());
        }
    }

    /**
     * 创建菜单
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public CommonResponse<Permission> createMenu(@RequestBody Permission menu) {
        try {
            Permission createdMenu = menuService.createMenu(menu);
            return CommonResponse.success(createdMenu);
        } catch (Exception e) {
            return CommonResponse.fail("创建菜单失败: " + e.getMessage());
        }
    }

    /**
     * 更新菜单
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public CommonResponse<Permission> updateMenu(@PathVariable Long id, @RequestBody Permission menu) {
        try {
            menu.setId(id);
            Permission updatedMenu = menuService.updateMenu(menu);
            return CommonResponse.success(updatedMenu);
        } catch (Exception e) {
            return CommonResponse.fail("更新菜单失败: " + e.getMessage());
        }
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public CommonResponse<Boolean> deleteMenu(@PathVariable Long id) {
        try {
            boolean result = menuService.deleteMenu(id);
            return result ? CommonResponse.success(true) : CommonResponse.fail("删除菜单失败");
        } catch (Exception e) {
            return CommonResponse.fail("删除菜单失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除菜单
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public CommonResponse<Boolean> batchDeleteMenus(@RequestBody List<Long> ids) {
        try {
            boolean result = menuService.batchDeleteMenus(ids);
            return result ? CommonResponse.success(true) : CommonResponse.fail("批量删除菜单失败");
        } catch (Exception e) {
            return CommonResponse.fail("批量删除菜单失败: " + e.getMessage());
        }
    }

    /**
     * 导出菜单
     */
    @GetMapping("/export")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public CommonResponse<List<Permission>> exportMenus() {
        try {
            List<Permission> menus = menuService.exportMenus();
            return CommonResponse.success(menus);
        } catch (Exception e) {
            return CommonResponse.fail("导出菜单失败: " + e.getMessage());
        }
    }

    /**
     * 导入菜单
     */
    @PostMapping("/import")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public CommonResponse<Boolean> importMenus(@RequestParam(required = false, defaultValue = "false") boolean override, 
                                               @RequestBody List<Permission> menus) {
        try {
            boolean result = menuService.importMenus(menus, override);
            return result ? CommonResponse.success(true) : CommonResponse.fail("导入菜单失败");
        } catch (Exception e) {
            return CommonResponse.fail("导入菜单失败: " + e.getMessage());
        }
    }

    /**
     * 刷新菜单缓存
     */
    @PostMapping("/refresh-cache")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public CommonResponse<Void> refreshMenuCache() {
        try {
            menuService.refreshMenuCache();
            return CommonResponse.success(null);
        } catch (Exception e) {
            return CommonResponse.fail("刷新菜单缓存失败: " + e.getMessage());
        }
    }

    /**
     * 获取菜单操作日志
     */
    @GetMapping("/operation-logs")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public CommonResponse<List<MenuOperationLog>> getMenuOperationLogs() {
        try {
            List<MenuOperationLog> logs = menuOperationLogMapper.findAll();
            return CommonResponse.success(logs);
        } catch (Exception e) {
            return CommonResponse.fail("获取菜单操作日志失败: " + e.getMessage());
        }
    }

    /**
     * 根据菜单ID获取操作日志
     */
    @GetMapping("/{menuId}/operation-logs")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public CommonResponse<List<MenuOperationLog>> getMenuOperationLogsByMenuId(@PathVariable Long menuId) {
        try {
            List<MenuOperationLog> logs = menuOperationLogMapper.findByMenuId(menuId);
            return CommonResponse.success(logs);
        } catch (Exception e) {
            return CommonResponse.fail("获取菜单操作日志失败: " + e.getMessage());
        }
    }
}