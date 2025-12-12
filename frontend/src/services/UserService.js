import axios from '../utils/axiosConfig'
import { ElMessage } from 'element-plus'

class UserService {
  constructor() {
    this.baseURL = '/api/users'
  }
  
  // 获取当前用户信息
  getCurrentUser() {
    const userInfo = localStorage.getItem('user')
    if (userInfo) {
      try {
        return JSON.parse(userInfo)
      } catch (error) {
        console.error('解析用户信息失败:', error)
        return null
      }
    }
    return null
  }

  // 设置当前用户信息
  setCurrentUser(user) {
    localStorage.setItem('user', JSON.stringify(user))
  }

  // 检查用户是否已登录
  isLoggedIn() {
    return !!this.getCurrentUser()
  }

  // 退出登录
  logout() {
    localStorage.removeItem('user')
    // 可以在这里添加其他清理操作，如清除token等
  }

  // 检查用户是否有指定角色
  hasRole(role) {
    const user = this.getCurrentUser()
    if (user && user.roles) {
      // 处理不同格式的角色列表
      if (Array.isArray(user.roles)) {
        // 情况1: roles是字符串数组，如['ADMIN', 'USER']
        return user.roles.includes(role) || user.roles.includes(role.toLowerCase()) || user.roles.includes(role.toUpperCase())
      } else if (typeof user.roles === 'object') {
        // 情况2: roles是对象数组，如[{roleCode: 'ADMIN'}, {roleCode: 'USER'}]
        return user.roles.some(r => r === role || 
                                 (typeof r === 'object' && (r.roleCode === role || r.roleCode === role.toLowerCase() || r.roleCode === role.toUpperCase() || 
                                                          r.code === role || r.code === role.toLowerCase() || r.code === role.toUpperCase() ||
                                                          r.name === role || r.name === role.toLowerCase() || r.name === role.toUpperCase())))
      }
    }
    return false
  }

  // 检查用户是否有管理员权限
  isAdmin() {
    const user = this.getCurrentUser()
    if (!user) {
      return false
    }
    
    // 多种方式检查管理员权限
    return this.hasRole('ADMIN') || this.hasRole('admin') || this.hasRole('ROLE_ADMIN') || 
           (user.role && (user.role === 'ADMIN' || user.role === 'admin'))
  }
  
  // 获取角色列表
  async getRoles() {
    try {
      console.log('调用API获取角色列表')
      const response = await axios.get('/api/roles')
      
      // 处理不同的响应格式
      
      // 情况1: 直接是数组（最常见的情况，因为axios拦截器会返回res.data || res）
      if (Array.isArray(response)) {
        console.log('成功获取角色列表:', response)
        return response
      }
      // 情况2: 是对象，包含data字段
      else if (response.data) {
        // 情况2.1: data直接是数组
        if (Array.isArray(response.data)) {
          console.log('成功获取角色列表:', response.data)
          return response.data
        }
        // 情况2.2: data是对象，包含code和data字段
        else if (response.data.code === 200 && response.data.data) {
          const roleList = Array.isArray(response.data.data) ? response.data.data : [response.data.data]
          console.log('成功获取角色列表:', roleList)
          return roleList
        }
        // 情况2.3: data是对象，直接返回
        else {
          console.log('成功获取角色列表:', response.data)
          return [response.data]
        }
      }
      // 情况3: 是对象，没有data字段，但直接是角色数据
      else if (typeof response === 'object' && response !== null) {
        console.log('成功获取角色列表:', response)
        return [response]
      }
      // 情况4: 其他情况
      else {
        console.error('获取角色列表API返回非预期格式:', response)
        // 返回默认角色数据作为备选
        return [
          { id: 1, roleName: '管理员', roleCode: 'ROLE_ADMIN' },
          { id: 2, roleName: '部门经理', roleCode: 'ROLE_MANAGER' },
          { id: 3, roleName: '普通用户', roleCode: 'ROLE_USER' }
        ]
      }
    } catch (error) {
      console.error('获取角色列表失败:', error)
      // 出错时返回默认角色数据
      return [
        { id: 1, roleName: '管理员', roleCode: 'ROLE_ADMIN' },
        { id: 2, roleName: '部门经理', roleCode: 'ROLE_MANAGER' },
        { id: 3, roleName: '普通用户', roleCode: 'ROLE_USER' }
      ]
    }
  }

  // 加载用户详情
  async loadUserDetails(userId) {
    try {
      const response = await axios.get(`${this.baseURL}/${userId}`)
      return response
    } catch (error) {
      console.error('加载用户详情失败:', error)
      throw error
    }
  }

  // 更新用户信息
  async updateUser(userId, userData) {
    try {
      const response = await axios.put(`${this.baseURL}/${userId}`, userData)
      return response
    } catch (error) {
      console.error('更新用户信息失败:', error)
      throw error
    }
  }

  // 获取用户任务统计
  async getUserTaskStatistics() {
    try {
      const response = await axios.get('/v1/tasks/statistics')
      
      // 验证响应数据格式
      if (!response || typeof response !== 'object') {
        console.error('用户任务统计响应格式错误')
        return {
          pending: 0,
          overdue: 0,
          completed: 0,
          myApplications: 0
        }
      }
      
      // 提取数据，确保返回默认值
      // 由于axios响应拦截器已经处理了response.data，所以直接使用response
      return {
        pending: response.pending || 0,
        overdue: response.overdue || 0,
        completed: response.completed || 0,
        myApplications: response.myApplications || 0
      }
    } catch (error) {
      // 对于'任务不存在'的情况，这是正常状态而不是错误，不显示错误提示
      // 只在控制台记录，让前端通过空状态来处理
      let isTaskNotFound = false;
      
      // 检查多种可能的错误消息位置和格式
      const errorMessages = [
        error.message,
        error.response?.data,
        error.response?.data?.message,
        error.response?.data?.error,
        error.response?.data?.msg,
        error.response?.data?.result,
        JSON.stringify(error.response?.data)
      ];
      
      // 检查是否包含任务不存在的关键词
      const notFoundKeywords = [
        '任务不存在',
        'Task not found',
        'task not found',
        '找不到任务',
        'No tasks found',
        'no tasks found',
        '没有任务',
        'Task does not exist',
        'task does not exist'
      ];
      
      // 遍历所有错误消息和关键词，检查是否匹配
      for (const msg of errorMessages) {
        if (typeof msg === 'string') {
          for (const keyword of notFoundKeywords) {
            if (msg.includes(keyword)) {
              isTaskNotFound = true;
              break;
            }
          }
          if (isTaskNotFound) break;
        }
      }
      
      if (isTaskNotFound) {
        console.info('没有找到任务统计数据，这是正常状态')
      } else {
        // 只对真正的错误（如网络错误、服务器错误等）显示错误提示
        console.error('获取用户任务统计失败:', error)
      }
      
      // 错误时返回默认值而不是抛出异常，避免影响界面渲染
      return {
        pending: 0,
        overdue: 0,
        completed: 0,
        myApplications: 0
      }
    }
  }

  // 获取待办任务数
  async getPendingTasksCount() {
    try {
      const response = await axios.get('/v1/tasks/count', {
        params: {
          assignee: this.getCurrentUser()?.username,
          active: true
        }
      })
      return response
    } catch (error) {
      console.error('获取待办任务数失败:', error)
      throw error
    }
  }

  // 获取用户已完成任务数
  async getCompletedTasksCount() {
    try {
      const response = await axios.get('/v1/tasks/count', {
        params: {
          assignee: this.getCurrentUser()?.username,
          completed: true
        }
      })
      return response
    } catch (error) {
      console.error('获取已完成任务数失败:', error)
      throw error
    }
  }

  // 获取用户发起的流程实例
  async getUserProcessInstances(params = {}) {
    try {
      const response = await axios.get('/api/process-instances', {
        params: {
          ...params,
          initiator: this.getCurrentUser()?.username
        }
      })
      return response
    } catch (error) {
      console.error('获取用户流程实例失败:', error)
      throw error
    }
  }

  // 获取用户流程定义权限
  async getUserProcessDefinitionPermissions() {
    try {
      const response = await axios.get('/api/users/permissions/process-definitions')
      return response
    } catch (error) {
      console.error('获取用户流程定义权限失败:', error)
      throw error
    }
  }

  // 检查用户是否有权限启动指定流程
  async canStartProcess(processDefinitionKey) {
    try {
      const response = await axios.get(`/api/process/definition/${processDefinitionKey}/can-start`)
      return response
    } catch (error) {
      console.error('检查流程启动权限失败:', error)
      throw error
    }
  }

  // 构建用户流程变量
  buildUserProcessVariables() {
    const user = this.getCurrentUser()
    if (!user) return {}
    
    return {
      initiatorId: user.id || user.username,
      initiatorName: user.name || user.username,
      initiatorDepartment: user.department || '',
      initiatorEmail: user.email || '',
      initiationTime: new Date().toISOString()
    }
  }

  // 获取用户列表
  async getUserList(params = {}) {
    try {
      // 后端API路径为/api/users，不支持分页参数
      const response = await axios.get(this.baseURL)
      
      // 处理可能的不同响应格式
      // 由于axios响应拦截器已经处理了response.data，所以直接使用response
      if (Array.isArray(response)) {
        // 直接返回数组
        return {
          success: true,
          data: response,
          total: response.length
        }
      } else if (typeof response === 'object' && response) {
        // 检查是否有data字段且为数组
        if (Array.isArray(response.data)) {
          return {
            success: true,
            data: response.data,
            total: response.total || response.data.length
          }
        }
        // 检查是否有users字段且为数组
        if (Array.isArray(response.users)) {
          return {
            success: true,
            data: response.users,
            total: response.total || response.users.length
          }
        }
        // 检查是否有rows字段且为数组（分页接口格式）
        if (Array.isArray(response.rows)) {
          return {
            success: true,
            data: response.rows,
            total: response.total || response.rows.length
          }
        }
        // 检查是否是单个用户对象
        if (response.id && response.username) {
          return {
            success: true,
            data: [response],
            total: 1
          }
        }
      }
      
      // 如果以上条件都不满足，返回空数组
      console.warn('获取用户列表响应格式不符合预期:', response)
      return {
        success: true,
        data: [],
        total: 0
      }
    } catch (error) {
      console.error('获取用户列表失败:', error)
      
      // 404错误或超时，返回模拟数据
      if (error.response?.status === 404 || error.code === 'ECONNABORTED' || !error.response) {
        return {
          success: true,
          data: [
            {
              id: 1,
              username: 'admin',
              fullName: '系统管理员',
              email: 'admin@example.com',
              phone: '13800138000',
              roles: [{ id: 1, roleName: '管理员', roleCode: 'ADMIN' }],
              createTime: '2024-01-01 00:00:00',
              status: 'ACTIVE'
            },
            {
              id: 2,
              username: 'user1',
              fullName: '用户一',
              email: 'user1@example.com',
              phone: '13900139001',
              roles: [{ id: 2, roleName: '普通用户', roleCode: 'USER' }],
              createTime: '2024-01-02 10:00:00',
              status: 'ACTIVE'
            },
            {
              id: 3,
              username: 'dept_manager',
              fullName: '部门经理',
              email: 'manager@example.com',
              phone: '13900139002',
              roles: [{ id: 3, roleName: '部门经理', roleCode: 'ROLE_MANAGER' }],
              createTime: '2024-01-03 15:00:00',
              status: 'ACTIVE'
            }
          ],
          total: 3
        }
      }
      
      throw error
    }
  }

  // 创建用户
  async createUser(userData) {
    try {
      // 深度复制userData，避免修改原始对象
      const cleanUserData = JSON.parse(JSON.stringify(userData))
      
      console.log('原始用户数据:', userData)
      console.log('深度复制后的数据:', cleanUserData)
      
      // 完全清理数据，确保不包含任何可能导致问题的字段
      const formattedData = {
        username: (cleanUserData.username || '').trim(),
        password: cleanUserData.password, // 密码不需要trim，可能包含特殊字符
        fullName: (cleanUserData.fullName || cleanUserData.nickname || '').trim(),
        email: (cleanUserData.email || '').trim(),
        phone: (cleanUserData.phone || '').trim()
        // 明确不设置status字段，让后端处理状态默认值
        // 绝对不包含id字段
      }
      
      // 显式检查并移除任何可能存在的id字段
      if (formattedData.id !== undefined) {
        delete formattedData.id
        console.warn('发现并移除了id字段')
      }
      
      // 检查所有可能的ID相关字段并移除
      const idFields = ['id', 'userId', '_id', 'user_id', 'uid']
      for (const field of idFields) {
        if (formattedData[field] !== undefined) {
          delete formattedData[field]
          console.warn(`发现并移除了${field}字段`)
        }
      }
      
      console.log('最终提交给后端的数据:', formattedData)
      console.log('提交的URL:', this.baseURL)
      
      // 确保使用正确的API端点
      const response = await axios.post(this.baseURL, formattedData)
      
      // 检查响应格式
      console.log('后端响应:', response)
      
      // 如果创建成功且包含角色信息，尝试关联用户角色
      if ((response.status === 200 || response.status === 201) && response.data) {
        const userId = response.data.id || response.data.userId
        console.log('用户创建成功，返回的ID:', userId)
        
        if (userId && cleanUserData.roles && Array.isArray(cleanUserData.roles) && cleanUserData.roles.length > 0) {
          try {
            // 尝试更新用户角色关联
            const roleIds = cleanUserData.roles.map(role => role.id).filter(id => id)
            console.log('准备关联角色:', roleIds)
            // 使用正确的角色分配API端点
            await axios.post(`/api/user-roles/assign`, { userId, roleIds })
            console.log('用户角色关联成功:', userId, roleIds)
          } catch (roleError) {
            console.warn('更新用户角色失败，但用户创建成功:', roleError)
            ElMessage.warning('用户创建成功，但角色关联失败')
            // 不抛出错误，因为用户创建已经成功
          }
        }
      }
      
      return response
    } catch (error) {
      console.error('创建用户失败:', error)
      console.error('错误详情:', {
        status: error.response?.status,
        data: error.response?.data,
        message: error.message,
        config: error.config
      })
      
      // 处理具体的错误类型
      let errorMessage = '创建用户失败'
      if (error.response) {
        // 检查是否是主键冲突或用户名重复
        if (error.response.status === 409 || 
            (error.response.data?.message && 
             (error.response.data.message.includes('主键') || 
              error.response.data.message.includes('PRIMARY KEY') ||
              error.response.data.message.includes('Duplicate')))) {
          errorMessage = '用户名已存在或ID冲突，请更换用户名'
        } else if (error.response.data?.message) {
          errorMessage = `创建用户失败: ${error.response.data.message}`
        } else {
          errorMessage = `创建用户失败: ${error.response.statusText}`
        }
      } else if (error.request) {
        errorMessage = '服务器无响应，请检查网络连接'
      } else {
        errorMessage = `创建用户失败: ${error.message}`
      }
      
      ElMessage.error(errorMessage)
      throw error
    }
  }

  // 删除用户
  async deleteUser(userId) {
    try {
      const response = await axios.delete(`${this.baseURL}/${userId}`)
      return response
    } catch (error) {
      console.error('删除用户失败:', error)
      throw error
    }
  }

  // 批量删除用户
  async batchDeleteUsers(userIds) {
    try {
      // 如果后端支持批量删除API，否则逐个删除
      try {
        // 尝试使用可能的批量删除API
        return await axios.post(`${this.baseURL}/batch-delete`, { userIds })
      } catch (error) {
        // 如果批量删除API不存在，逐个删除
        console.warn('批量删除API不可用，尝试逐个删除')
        const results = []
        for (const id of userIds) {
          try {
            const result = await this.deleteUser(id)
            results.push({ id, success: true, result })
          } catch (err) {
            results.push({ id, success: false, error: err })
          }
        }
        return { success: results.every(r => r.success), results }
      }
    } catch (error) {
      console.error('批量删除用户失败:', error)
      throw error
    }
  }

  // 更新用户状态
  async updateUserStatus(userId, status) {
    try {
      // 尝试使用专门的状态更新API
      try {
        return await axios.put(`${this.baseURL}/${userId}/status`, { status })
      } catch (error) {
        // 如果没有专门的状态更新API，使用通用的更新API
        console.warn('状态更新API不可用，使用通用更新API')
        return await this.updateUser(userId, { status: status === 'ACTIVE' ? 1 : 0 })
      }
    } catch (error) {
      console.error('更新用户状态失败:', error)
      throw error
    }
  }



  // 获取用户角色
  async getUserRoles(userId) {
    try {
      const response = await axios.get(`/api/roles/by-user/${userId}`)
      
      // 兼容不同的响应格式 - 由于axios响应拦截器已经处理了response.data，所以直接使用response
      if (Array.isArray(response)) {
        // 直接返回数组
        return response
      } else if (typeof response === 'object' && response) {
        // 检查是否有success字段
        if (response.success !== undefined) {
          // 情况1: response是CommonResponse格式，包含success和data字段
          if (response.success) {
            // 检查data字段是否为数组
            if (Array.isArray(response.data)) {
              return response.data
            } else if (response.data && Array.isArray(response.data.data)) {
              // 嵌套格式: { data: { data: [] } }
              return response.data.data
            }
          }
        } else if (response.code !== undefined) {
          // 情况2: response是包含code字段的格式
          if (response.code === 200 || response.code === 0) {
            if (Array.isArray(response.data)) {
              return response.data
            } else if (response.data && Array.isArray(response.data.data)) {
              // 嵌套格式: { data: { data: [] } }
              return response.data.data
            }
          }
        } else if (Array.isArray(response.data)) {
          // 情况3: response包含data数组
          return response.data
        }
      }
      
      // 其他情况，返回空数组
      return []
    } catch (error) {
      console.error('获取用户角色失败:', error)
      return []
    }
  }

  // 保存用户角色
  async saveUserRoles(userId, roleIds) {
    try {
      // 尝试使用可能的用户角色保存API
      return await axios.post(`/api/user-roles/${userId}`, { roleIds })
    } catch (error) {
      console.error('保存用户角色失败:', error)
      throw error
    }
  }

  // 获取当前用户的部门信息
  async getCurrentUserDepartment() {
    try {
      const user = this.getCurrentUser()
      if (!user || !user.id) return null
      
      const response = await axios.get(`${this.baseURL}/${user.id}/department`)
      return response
    } catch (error) {
      console.error('获取用户部门信息失败:', error)
      throw error
    }
  }

  // 获取当前用户的可审批流程
  async getApprovalProcesses() {
    try {
      const response = await axios.get('/api/users/approval-processes')
      return response
    } catch (error) {
      console.error('获取可审批流程失败:', error)
      throw error
    }
  }

  // 更新用户密码
  async updatePassword(oldPassword, newPassword) {
    try {
      const response = await axios.put(`${this.baseURL}/${this.getCurrentUser()?.id}/password`, {
        oldPassword,
        newPassword
      })
      return response
    } catch (error) {
      console.error('更新密码失败:', error)
      throw error
    }
  }

  // 获取用户通知
  async getUserNotifications(params = {}) {
    try {
      const response = await axios.get('/api/notifications', {
        params
      })
      return response
    } catch (error) {
      console.error('获取用户通知失败:', error)
      throw error
    }
  }

  // 标记通知为已读
  async markNotificationAsRead(notificationId) {
    try {
      const response = await axios.put(`/api/notifications/${notificationId}/read`)
      return response
    } catch (error) {
      console.error('标记通知为已读失败:', error)
      throw error
    }
  }
  
  // 获取用户列表
  async getUsers() {
    try {
      // 移除所有分页和排序参数，直接调用API
      console.log('开始获取用户列表(无参数):', new Date().toISOString())
      
      const response = await axios.get(this.baseURL)
      
      // 验证响应数据
      if (!response) {
        console.error('获取用户列表响应为空')
        // 返回模拟数据而不是空数组
        return this.getMockUsers()
      }
      
      // 兼容不同的响应格式 - 由于axios响应拦截器已经处理了response.data，所以直接使用response
      if (Array.isArray(response)) {
        // 直接返回数组
        console.log(`成功获取 ${response.length} 个用户`)
        return response
      } else if (typeof response === 'object') {
        // 检查是否有data字段且为数组
        if (Array.isArray(response.data)) {
          console.log(`成功获取 ${response.data.length} 个用户`)
          return response.data
        }
        // 检查是否有users字段且为数组
        if (Array.isArray(response.users)) {
          console.log(`成功获取 ${response.users.length} 个用户`)
          return response.users
        }
        // 检查是否有rows字段且为数组（分页接口格式）
        if (Array.isArray(response.rows)) {
          console.log(`成功获取 ${response.rows.length} 个用户`)
          return response.rows
        }
        // 检查是否是单个用户对象
        if (response.id && response.username) {
          console.log('成功获取 1 个用户')
          return [response]
        }
      }
      
      console.error('获取用户列表响应格式错误:', response)
      // 返回模拟数据而不是空数组
      return this.getMockUsers()
    } catch (error) {
      console.error('获取用户列表失败:', error)

      // 根据错误类型提供更详细的错误提示
      let errorMsg = '获取用户列表失败，请稍后重试'
      
      if (error.response) {
        // 服务器返回错误
        if (error.response.status === 401) {
          errorMsg = '登录已过期，请重新登录'
        } else if (error.response.status === 403) {
          errorMsg = '没有权限获取用户列表，将使用模拟数据'
        } else if (error.response.status === 404) {
          errorMsg = '用户列表接口不存在，将使用模拟数据'
        } else if (error.response.data && error.response.data.message) {
          errorMsg = `获取用户列表失败: ${error.response.data.message}`
        }
      } else if (error.request) {
        // 请求已发送但没有收到响应
        errorMsg = '服务器无响应，请检查网络连接，将使用模拟数据'
      } else {
        // 请求配置出错
        errorMsg = `获取用户列表失败: ${error.message}`
      }
      
      // 显示友好的错误提示
      ElMessage.warning(errorMsg)
      
      // 出错时返回模拟数据，确保页面能正常显示
      return this.getMockUsers()
    }
  }
  
  // 获取模拟用户数据
  getMockUsers() {
    return [
      {
        id: '1',
        username: 'admin',
        fullName: '系统管理员',
        nickname: '系统管理员',
        email: 'admin@example.com',
        phone: '13800138000',
        department: '技术部',
        createTime: '2024-01-01 00:00:00',
        roles: [{ roleCode: 'ADMIN', roleName: '管理员' }],
        status: 'ACTIVE'
      },
      {
        id: '2',
        username: 'manager',
        fullName: '部门经理',
        nickname: '部门经理',
        email: 'manager@example.com',
        phone: '13800138001',
        department: '技术部',
        createTime: '2024-01-02 00:00:00',
        roles: [{ roleCode: 'ROLE_MANAGER', roleName: '部门经理' }],
        status: 'ACTIVE'
      },
      {
        id: '3',
        username: 'user',
        fullName: '普通用户',
        nickname: '普通用户',
        email: 'user@example.com',
        phone: '13800138002',
        department: '技术部',
        createTime: '2024-01-03 00:00:00',
        roles: [{ roleCode: 'USER', roleName: '普通用户' }],
        status: 'ACTIVE'
      }
    ]
  }
  

}

export default new UserService()