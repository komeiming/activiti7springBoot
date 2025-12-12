// API 工具类，用于处理与后端的通信
const API = {
    // 基础URL
    baseUrl: '',
    
    // 通用请求方法
    request: async function(endpoint, options = {}) {
        const defaultOptions = {
            headers: {
                'Content-Type': 'application/json',
                ...Auth.createAuthHeader().headers
            },
            ...options
        };
        
        try {
            const response = await fetch(`${this.baseUrl}${endpoint}`, defaultOptions);
            
            if (!response.ok) {
                // 如果是401未授权，清除token并重定向到登录页
                if (response.status === 401) {
                    Auth.logout();
                    window.location.href = 'login.html';
                    return Promise.reject(new Error('未授权，请重新登录'));
                }
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            // 检查响应是否为空
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                return await response.json();
            }
            return await response.text();
        } catch (error) {
            console.error('API请求错误:', error);
            throw error;
        }
    },
    
    // 获取任务列表
    getTasks: async function() {
        // 调用后端TaskController的任务列表API
        const response = await this.request('/api/v1/tasks');
        if (response.success && response.data) {
            // 转换后端响应格式以匹配前端期望的格式
            return {
                tasks: response.data.data || [],
                total: response.data.total || 0
            };
        } else if (response.data) {
            // 处理AdminController返回的格式（备用方案）
            return response;
        } else {
            throw new Error(response.msg || '获取任务列表失败');
        }
    },
    
    // 获取任务详情
    getTaskDetail: async function(taskId) {
        const response = await this.request(`/api/v1/tasks/${taskId}`);
        if (response.success && response.data) {
            return response.data;
        } else {
            throw new Error(response.msg || '获取任务详情失败');
        }
    },
    
    // 完成任务
    completeTask: async function(taskId, variables = {}) {
        const response = await this.request(`/api/v1/tasks/${taskId}/complete`, {
            method: 'POST',
            body: JSON.stringify(variables)
        });
        if (response.success) {
            return response;
        } else {
            throw new Error(response.msg || '完成任务失败');
        }
    },
    
    // 获取流程定义列表（管理员）
    getProcessDefinitions: async function() {
        return this.request('/admin/process-definitions');
    },
    
    // 获取流程引擎信息（管理员）
    getEngineInfo: async function() {
        return this.request('/admin/engine-info');
    },
    
    // 启动流程实例（管理员）
    startProcess: async function() {
        return this.request('/admin/start-process');
    },
    
    // 上传BPMN文件（管理员）
    uploadBpmn: async function(file) {
        const formData = new FormData();
        formData.append('file', file);
        
        // 上传文件时不设置Content-Type，让浏览器自动设置并包含boundary
        const token = Auth.getToken();
        const response = await fetch(`${this.baseUrl}/process/upload-bpmn`, {
            method: 'POST',
            headers: {
                'Authorization': token ? `Bearer ${token}` : ''
            },
            body: formData
        });
        
        if (!response.ok) {
            if (response.status === 401) {
                Auth.logout();
                window.location.href = 'login.html';
                throw new Error('未授权，请重新登录');
            }
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        return data;
    },
    
    // 获取用户信息
    getUserInfo: async function() {
        return this.request('/user/info');
    }
};

// 全局可用
window.API = API;
