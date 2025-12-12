import axios from '../utils/axiosConfig';

class NotificationTemplateService {
  // 获取模板列表
  async getTemplates(params = {}) {
    try {
      const response = await axios.get('/api/notification/templates', { params });
      // 由于axios响应拦截器已经处理了response.data，所以直接返回response
      return response;
    } catch (error) {
      console.error('获取模板列表失败:', error);
      throw error;
    }
  }

  // 获取模板详情
  async getTemplate(id) {
    try {
      const response = await axios.get(`/api/notification/templates/${id}`);
      // 由于axios响应拦截器已经处理了response.data，所以直接返回response
      return response;
    } catch (error) {
      console.error(`获取模板ID: ${id} 详情失败:`, error);
      throw error;
    }
  }

  // 创建模板
  async createTemplate(templateData) {
    try {
      const response = await axios.post('/api/notification/templates', templateData);
      // 由于axios响应拦截器已经处理了response.data，所以直接返回response
      return response;
    } catch (error) {
      console.error('创建模板失败:', error);
      throw error;
    }
  }

  // 更新模板
  async updateTemplate(id, templateData) {
    try {
      const response = await axios.put(`/api/notification/templates/${id}`, templateData);
      // 由于axios响应拦截器已经处理了response.data，所以直接返回response
      return response;
    } catch (error) {
      console.error(`更新模板ID: ${id} 失败:`, error);
      throw error;
    }
  }

  // 删除模板
  async deleteTemplate(id) {
    try {
      const response = await axios.delete(`/api/notification/templates/${id}`);
      // 由于axios响应拦截器已经处理了response.data，所以直接返回response
      return response;
    } catch (error) {
      console.error(`删除模板ID: ${id} 失败:`, error);
      throw error;
    }
  }

  // 启用模板
  async enableTemplate(id) {
    try {
      const response = await axios.put(`/api/notification/templates/${id}/enable`);
      // 由于axios响应拦截器已经处理了response.data，所以直接返回response
      return response;
    } catch (error) {
      console.error(`启用模板ID: ${id} 失败:`, error);
      throw error;
    }
  }

  // 禁用模板
  async disableTemplate(id) {
    try {
      const response = await axios.put(`/api/notification/templates/${id}/disable`);
      // 由于axios响应拦截器已经处理了response.data，所以直接返回response
      return response;
    } catch (error) {
      console.error(`禁用模板ID: ${id} 失败:`, error);
      throw error;
    }
  }

  // 测试发送邮件
  async testSendEmail(email, subject, content) {
    try {
      const response = await axios.post('/api/notification/test/email', {
        receiver: email,
        params: {
          subject: subject,
          content: content
        }
      });
      // 由于axios响应拦截器已经处理了response.data，所以直接返回response
      return response;
    } catch (error) {
      console.error('测试发送邮件失败:', error);
      throw error;
    }
  }

  // 测试发送短信
  async testSendSms(phone, content) {
    try {
      const response = await axios.post('/api/notification/test/sms', {
        receiver: phone,
        params: {
          content: content
        }
      });
      // 由于axios响应拦截器已经处理了response.data，所以直接返回response
      return response;
    } catch (error) {
      console.error('测试发送短信失败:', error);
      throw error;
    }
  }

  // 发送通知
  async sendNotification(params) {
    try {
      const response = await axios.post('/api/notification/send', params);
      // 由于axios响应拦截器已经处理了response.data，所以直接返回response
      return response;
    } catch (error) {
      console.error('发送通知失败:', error);
      throw error;
    }
  }

  // 获取通知日志
  async getNotificationLogs(params = {}) {
    try {
      const response = await axios.get('/api/notification/logs', { params });
      // 由于axios响应拦截器已经处理了response.data，所以直接返回response
      return response;
    } catch (error) {
      console.error('获取通知日志失败:', error);
      throw error;
    }
  }

  // 获取模板操作日志
  async getTemplateOperationLogs(params = {}) {
    try {
      const response = await axios.get('/api/notification/template-logs', { params });
      // 由于axios响应拦截器已经处理了response.data，所以直接返回response
      return response;
    } catch (error) {
      console.error('获取模板操作日志失败:', error);
      throw error;
    }
  }
}

export default new NotificationTemplateService();