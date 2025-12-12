import axios from '../utils/axiosConfig';

const API_URL = '/api/permissions';

const PermissionService = {
  /**
   * 获取当前用户的菜单
   */
  getUserMenus: async (languageCode = '') => {
    try {
      const params = languageCode ? { languageCode } : {};
      const response = await axios.get(`${API_URL}/user-menus`, { params });
      return response;
    } catch (error) {
      console.error('获取用户菜单失败:', error);
      throw error;
    }
  },

  /**
   * 获取所有权限和菜单
   */
  getAllPermissions: async (languageCode = '') => {
    try {
      const params = languageCode ? { languageCode } : {};
      const response = await axios.get(API_URL, { params });
      return response;
    } catch (error) {
      console.error('获取所有权限失败:', error);
      throw error;
    }
  }
};

export default PermissionService;
