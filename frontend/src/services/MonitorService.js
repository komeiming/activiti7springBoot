import axios from '../utils/axiosConfig';

class MonitorService {
  /**
   * 获取租户使用统计数据
   */
  async getTenantStats(params = {}) {
    try {
      const response = await axios.get('/api/monitor/tenant-stats', { params });
      return response;
    } catch (error) {
      console.error('获取租户使用统计数据失败:', error);
      throw error;
    }
  }

  /**
   * 获取API调用统计数据
   */
  async getApiStats(params = {}) {
    try {
      const response = await axios.get('/api/monitor/api-stats', { params });
      return response;
    } catch (error) {
      console.error('获取API调用统计数据失败:', error);
      throw error;
    }
  }

  /**
   * 获取系统操作日志
   */
  async getSystemLogs(params = {}) {
    try {
      const response = await axios.get('/api/monitor/system-logs', { params });
      return response;
    } catch (error) {
      console.error('获取系统操作日志失败:', error);
      throw error;
    }
  }

  /**
   * 导出系统操作日志
   */
  async exportSystemLogs(params = {}) {
    try {
      const response = await axios.get('/api/monitor/system-logs/export', { 
        params,
        responseType: 'blob' // 重要：设置响应类型为blob，用于下载文件
      });
      return response;
    } catch (error) {
      console.error('导出系统操作日志失败:', error);
      throw error;
    }
  }

  /**
   * 获取告警管理数据
   */
  async getAlerts(params = {}) {
    try {
      const response = await axios.get('/api/monitor/alerts', { params });
      return response;
    } catch (error) {
      console.error('获取告警管理数据失败:', error);
      throw error;
    }
  }
}

export default new MonitorService;
