import axios from '../utils/axiosConfig';

class PendingNotificationService {
  // 获取待发送通知列表
  async getPendingNotifications(params = {}) {
    try {
      const response = await axios.get('/notification/pending', { params });
      // 由于axios响应拦截器已经处理了response.data，所以直接返回response
      return response;
    } catch (error) {
      console.error('获取待发送通知列表失败:', error);
      throw error;
    }
  }

  // 获取待发送通知详情
  async getPendingNotification(id) {
    try {
      const response = await axios.get(`/notification/pending/${id}`);
      // 由于axios响应拦截器已经处理了response.data，所以直接返回response
      return response;
    } catch (error) {
      console.error(`获取待发送通知ID: ${id} 详情失败:`, error);
      throw error;
    }
  }

  // 根据状态获取待发送通知
  async getPendingNotificationsByStatus(status) {
    try {
      const response = await axios.get(`/notification/pending/status/${status}`);
      // 由于axios响应拦截器已经处理了response.data，所以直接返回response
      return response;
    } catch (error) {
      console.error(`获取状态为${status}的待发送通知失败:`, error);
      throw error;
    }
  }

  // 根据类型获取待发送通知
  async getPendingNotificationsByType(type) {
    try {
      const response = await axios.get(`/notification/pending/type/${type}`);
      // 由于axios响应拦截器已经处理了response.data，所以直接返回response
      return response;
    } catch (error) {
      console.error(`获取类型为${type}的待发送通知失败:`, error);
      throw error;
    }
  }

  // 处理待发送通知
  async processPendingNotification(id) {
    try {
      const response = await axios.post(`/notification/pending/process/${id}`);
      // 由于axios响应拦截器已经处理了response.data，所以直接返回response
      return response;
    } catch (error) {
      console.error(`处理待发送通知ID: ${id} 失败:`, error);
      throw error;
    }
  }

  // 批量处理待发送通知
  async processPendingNotifications() {
    try {
      const response = await axios.post('/notification/pending/process/batch');
      // 由于axios响应拦截器已经处理了response.data，所以直接返回response
      return response;
    } catch (error) {
      console.error('批量处理待发送通知失败:', error);
      throw error;
    }
  }

  // 删除待发送通知
  async deletePendingNotification(id) {
    try {
      const response = await axios.delete(`/notification/pending/${id}`);
      // 由于axios响应拦截器已经处理了response.data，所以直接返回response
      return response;
    } catch (error) {
      console.error(`删除待发送通知ID: ${id} 失败:`, error);
      throw error;
    }
  }

  // 批量删除待发送通知
  async deletePendingNotifications(ids) {
    try {
      const response = await axios.delete('/notification/pending/batch', { data: ids });
      // 由于axios响应拦截器已经处理了response.data，所以直接返回response
      return response;
    } catch (error) {
      console.error('批量删除待发送通知失败:', error);
      throw error;
    }
  }
}

export default new PendingNotificationService();