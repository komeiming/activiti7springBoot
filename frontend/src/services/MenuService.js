import axios from '../utils/axiosConfig';

class MenuService {
  /**
   * 获取所有菜单
   */
  getAllMenus(languageCode = null) {
    let url = '/api/menus';
    if (languageCode) {
      url += `?languageCode=${languageCode}`;
    }
    return axios.get(url);
  }

  /**
   * 根据ID获取菜单
   */
  getMenuById(id) {
    return axios.get(`/api/menus/${id}`);
  }

  /**
   * 创建菜单
   */
  createMenu(menu) {
    return axios.post('/api/menus', menu);
  }

  /**
   * 更新菜单
   */
  updateMenu(id, menu) {
    return axios.put(`/api/menus/${id}`, menu);
  }

  /**
   * 删除菜单
   */
  deleteMenu(id) {
    return axios.delete(`/api/menus/${id}`);
  }

  /**
   * 批量删除菜单
   */
  batchDeleteMenus(ids) {
    return axios.delete('/api/menus/batch', { data: ids });
  }

  /**
   * 导出菜单
   */
  exportMenus() {
    return axios.get('/api/menus/export');
  }

  /**
   * 导入菜单
   */
  importMenus(menus, override = false) {
    return axios.post(`/api/menus/import?override=${override}`, menus);
  }

  /**
   * 刷新菜单缓存
   */
  refreshMenuCache() {
    return axios.post('/api/menus/refresh-cache');
  }

  /**
   * 获取菜单操作日志
   */
  getMenuOperationLogs() {
    return axios.get('/api/menus/operation-logs');
  }

  /**
   * 根据菜单ID获取操作日志
   */
  getMenuOperationLogsByMenuId(menuId) {
    return axios.get(`/api/menus/${menuId}/operation-logs`);
  }
  
  /**
   * 获取角色菜单权限
   */
  getRoleMenuPermissions(roleId) {
    return axios.get(`/api/roles/${roleId}/menus`);
  }
  
  /**
   * 保存角色菜单权限
   */
  saveRoleMenuPermissions(params) {
    return axios.post(`/api/roles/${params.roleId}/menus`, params.menuIds);
  }
}

export default new MenuService();