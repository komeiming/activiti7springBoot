// 认证工具类
const Auth = {
    // 存储JWT token
    setToken: function(token) {
        localStorage.setItem('activiti_token', token);
    },
    
    // 获取JWT token
    getToken: function() {
        return localStorage.getItem('activiti_token');
    },
    
    // 检查是否已认证
    isAuthenticated: function() {
        return this.getToken() !== null;
    },
    
    // 清除token，登出
    logout: function() {
        localStorage.removeItem('activiti_token');
        localStorage.removeItem('user_info');
        window.location.href = '/login.html';
    },
    
    // 创建带认证头的请求配置
    createAuthHeader: function() {
        const token = this.getToken();
        return {
            headers: {
                'Authorization': token ? `Bearer ${token}` : '',
                'Content-Type': 'application/json'
            }
        };
    },
    
    // 执行登录请求，调用后端API
    login: function(username, password) {
        // 添加调试信息
        console.log('登录请求参数:', {username, password});
        
        // 调用后端认证API
        return fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({username, password})
        })
        .then(response => {
            console.log('登录响应状态:', response.status);
            if (!response.ok) {
                // 如果响应状态不是成功，解析错误信息
                return response.json().then(errorData => {
                    throw new Error(errorData.msg || '登录失败');
                }).catch(() => {
                    throw new Error('登录失败，请检查网络连接');
                });
            }
            return response.json();
        })
        .then(data => {
            console.log('登录成功，返回数据:', data);
            
            // 检查响应格式
            if (!data.data || !data.data.token) {
                throw new Error('无效的登录响应格式');
            }
            
            // 存储token和用户信息
            this.setToken(data.data.token);
            const userData = {
                username: data.data.username,
                role: data.data.role,
                // 尝试从token中获取多角色信息，这里简化处理，至少存储主角色
                roles: [data.data.role] // 后续可以扩展为从token中解析多角色
            };
            localStorage.setItem('user_info', JSON.stringify(userData));
            localStorage.setItem('user', JSON.stringify(userData)); // 兼容EnhancedTaskList.vue使用的存储键
            
            return {
                success: true,
                user: userData
            };
        })
        .catch(error => {
            console.error('登录错误:', error);
            return {
                success: false,
                error: error.message || '登录失败，请稍后重试'
            };
        });
    },
    
    // 获取当前用户信息
    getUserInfo: function() {
        const userStr = localStorage.getItem('user_info');
        return userStr ? JSON.parse(userStr) : null;
    },
    
    // 检查用户角色（支持检查多个角色中的任何一个）
    hasRole: function(role) {
        const user = this.getUserInfo();
        if (!user) return false;
        // 如果有roles数组，检查其中是否包含指定角色
        if (user.roles && Array.isArray(user.roles)) {
            return user.roles.includes(role);
        }
        // 向后兼容：只检查单个role属性
        return user.role === role;
    }
};

// 全局可用
window.Auth = Auth;
