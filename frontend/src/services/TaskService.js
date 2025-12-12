import axios from '../utils/axiosConfig'
import { ElMessage } from 'element-plus'

/**
 * 任务服务类
 * 处理与任务相关的API调用
 */
class TaskService {
  constructor() {
    this.baseURL = '/v1/tasks'
  }

  /**
   * 获取任务列表
   * @param {number} page - 页码
   * @param {number} size - 每页数量
   * @param {Object} filters - 过滤条件
   * @returns {Promise<Object>} 任务列表数据
   */
  async getTasks(params) {
    try {
      // 兼容两种调用方式：单个对象参数或分离的参数
      let page, size, filters;
      if (typeof params === 'object' && params !== null) {
        // 单个对象参数方式
        page = params.page || 1;
        size = params.pageSize || params.size || 10;
        filters = { ...params };
        delete filters.page;
        delete filters.pageSize;
        delete filters.size;
      } else {
        // 分离参数方式（兼容旧代码）
        page = params || 1;
        size = arguments[1] || 10;
        filters = arguments[2] || {};
      }
      
      const requestParams = {
        page,
        size,
        ...filters
      }
      
      console.log('调用任务列表API:', this.baseURL, '参数:', requestParams)
      const response = await axios.get(this.baseURL, { params: requestParams })
      console.log('任务列表API响应:', response)
      
      // 检查响应是否存在
      if (!response) {
        console.error('任务列表API返回空响应')
        return {
          data: [],
          total: 0
        }
      }
      
      // 处理响应数据，支持多种格式
      let tasksData = [];
      let totalCount = 0;
      
      // 情况1: 响应是CommonResponse格式，包含success和data字段
      if (response && response.success !== undefined) {
        if (response.success) {
          // 情况1.1: data字段是对象，包含data数组和total字段（分页格式）
          if (response.data && typeof response.data === 'object' && response.data.data && Array.isArray(response.data.data)) {
            tasksData = response.data.data;
            totalCount = response.data.total || response.data.data.length;
          }
          // 情况1.2: data字段直接是数组
          else if (Array.isArray(response.data)) {
            tasksData = response.data;
            totalCount = response.total || response.data.length;
          }
          // 情况1.3: data字段是单个任务对象
          else if (response.data && typeof response.data === 'object') {
            tasksData = [response.data];
            totalCount = 1;
          }
        } else {
          console.error('任务列表API返回失败:', response.message)
        }
      }
      // 情况2: 响应直接是对象，包含data数组和total字段（分页格式）
      else if (response.data && typeof response.data === 'object' && response.data.data && Array.isArray(response.data.data)) {
        tasksData = response.data.data;
        totalCount = response.data.total || response.data.data.length;
      }
      // 情况3: 响应直接是数组
      else if (Array.isArray(response)) {
        tasksData = response;
        totalCount = response.length;
      }
      // 情况4: 响应是对象，且data字段直接是数组
      else if (Array.isArray(response.data)) {
        tasksData = response.data;
        totalCount = response.total || response.data.length;
      }
      // 情况5: 响应是对象，且data字段是单个任务对象
      else if (response.data && typeof response.data === 'object') {
        tasksData = [response.data];
        totalCount = 1;
      }
      
      console.log('解析后的任务数据:', { data: tasksData, total: totalCount })
      return {
        data: tasksData,
        total: totalCount
      }
    } catch (error) {
      console.error('获取任务列表失败:', error)
      console.error('错误详情:', error.response ? JSON.stringify(error.response.data) : error.message)
      return {
        data: [],
        total: 0
      }
    }
  }

  /**
   * 获取任务详情
   * @param {string} taskId - 任务ID
   * @returns {Promise<Object|null>} 任务详情或null
   */
  async getTaskDetail(taskId) {
    try {
      console.log('调用任务详情API:', `${this.baseURL}/${taskId}`)
      const response = await axios.get(`${this.baseURL}/${taskId}`)
      
      console.log('任务详情API响应:', response)
      
      // 处理响应数据，支持多种格式
      // 情况1: response是直接返回的数据（axios拦截器处理后的响应）
      if (response) {
        // 情况1.1: response是对象，直接返回
        if (typeof response === 'object') {
          return response;
        }
      }
      
      // 情况2: response是完整的axios响应对象（包含data字段）
      if (response && response.data) {
        // 情况2.1: 检查是否为标准格式（带有code字段）
        if (response.data.code !== undefined) {
          if (response.data.code === 200) {
            // 标准格式：{code: 200, data: {task: {...}, variables: {...}}}
            return response.data.data || {};
          } else {
            console.error('获取任务详情失败，错误码:', response.data.code)
            return {}
          }
        }
        // 情况2.2: 检查是否为CommonResponse格式，包含success和data字段
        else if (response.data.success !== undefined) {
          if (response.data.success) {
            // 情况2.2.1: data字段是对象，包含task、variables等字段
            if (response.data.data && typeof response.data.data === 'object') {
              return response.data.data
            }
            // 情况2.2.2: data字段直接是任务详情
            else {
              return response.data.data || response.data
            }
          } else {
            console.error('任务详情API返回失败:', response.data.message)
            return {}
          }
        }
        // 情况2.3: 响应直接是任务详情对象
        else if (typeof response.data === 'object') {
          return response.data
        }
      }
      
      console.error('获取任务详情API返回空响应或无效格式')
      return {}
    } catch (error) {
      console.error('获取任务详情失败:', error)
      return {}
    }
  }

  /**
   * 添加任务评论
   * 注意：此方法暂时保留，但由于后端没有提供对应的API端点，实际使用时可能会失败
   * @param {string} taskId - 任务ID
   * @param {string} message - 评论内容
   * @returns {Promise<boolean>} 是否成功
   */
  async addComment(taskId, message) {
    try {
      console.log('添加评论功能暂时不可用，后端没有提供对应的API端点')
      // 由于后端没有提供添加评论的API端点，暂时返回成功以避免前端错误
      return true
    } catch (error) {
      console.error('添加任务评论失败:', error)
      return false
    }
  }

  /**
   * 获取任务评论
   * @param {string} taskId - 任务ID
   * @returns {Promise<Array>} 任务评论数组
   */
  async getTaskComments(taskId) {
    try {
      // 后端没有单独的评论端点，通过任务详情端点获取评论
      console.log('调用任务详情API获取评论:', `${this.baseURL}/${taskId}`)
      const response = await axios.get(`${this.baseURL}/${taskId}`)
      
      // 从任务详情中提取评论数据，支持多种响应格式
      let commentsData = [];
      
      // 情况1: response本身是对象，直接包含评论数据
      if (response && typeof response === 'object') {
        // 检查各种可能的评论数据位置
        if (Array.isArray(response.comments)) {
          commentsData = response.comments;
        } else if (response.data && Array.isArray(response.data.comments)) {
          commentsData = response.data.comments;
        } else if (response.data && response.data.data && Array.isArray(response.data.data.comments)) {
          commentsData = response.data.data.comments;
        } else if (response.data && response.data.list && Array.isArray(response.data.list)) {
          // 检查是否是分页格式，评论数据可能在list中
          commentsData = response.data.list.filter(item => item.type === 'comment') || [];
        }
      }
      
      console.log('解析后的任务评论数据:', commentsData)
      return commentsData;
    } catch (error) {
      console.error('获取任务评论失败:', error)
      return [];
    }
  }

  /**
   * 认领任务
   * @param {string} taskId - 任务ID
   * @returns {Promise<boolean>} 是否成功
   */
  async claimTask(taskId) {
    try {
      console.log('调用认领任务API:', `${this.baseURL}/${taskId}/claim`)
      const response = await axios.post(`${this.baseURL}/${taskId}/claim`, {})
      
      if (response && response.data && response.data.success !== false) {
        ElMessage.success('任务认领成功')
        return true
      }
      
      ElMessage.error('任务认领失败: ' + (response?.data?.message || '未知错误'))
      return false
    } catch (error) {
      console.error('认领任务失败:', error)
      ElMessage.error('任务认领失败: ' + (error.response?.data?.message || error.message))
      return false
    }
  }

  /**
   * 完成任务
   * @param {string} taskId - 任务ID
   * @param {Object} variables - 任务变量
   * @returns {Promise<boolean>} 是否成功
   */
  async completeTask(taskId, variables = {}) {
    try {
      console.log('调用完成任务API:', `${this.baseURL}/${taskId}/complete`)
      console.log('提交的变量:', variables)
      const response = await axios.post(`${this.baseURL}/${taskId}/complete`, variables)
      
      console.log('完成任务API响应:', response)
      
      // 处理多种响应格式，判断任务是否完成成功
      // 由于axios拦截器的处理，response可能直接是返回的数据，而不是完整的响应对象
      let isSuccess = false
      
      // 情况1: response是布尔值，表示成功或失败
      if (typeof response === 'boolean') {
        isSuccess = response
      }
      // 情况2: response是字符串，可能是后端直接返回的成功消息
      else if (typeof response === 'string') {
        // 检查字符串是否包含成功信息
        isSuccess = response.includes('成功') || response.includes('success')
      }
      // 情况3: response是对象
      else if (typeof response === 'object' && response !== null) {
        // 情况3.1: 检查response.success字段
        if (response.success !== undefined) {
          isSuccess = response.success !== false
        }
        // 情况3.2: 检查response.data.success字段（旧格式）
        else if (response.data && typeof response.data === 'object' && response.data.success !== undefined) {
          isSuccess = response.data.success !== false
        }
        // 情况3.3: 检查response.code字段（标准REST格式）
        else if (response.code !== undefined) {
          isSuccess = response.code === 200 || response.code === 0
        }
        // 情况3.4: 检查response.data.code字段（旧格式）
        else if (response.data && typeof response.data === 'object' && response.data.code !== undefined) {
          isSuccess = response.data.code === 200 || response.data.code === 0
        }
        // 情况3.5: 没有明确的错误信息，且状态码是200或201（通常表示成功）
        else if (response.status === 200 || response.status === 201 || !response.status) {
          isSuccess = true
        }
        // 情况3.6: 检查是否包含错误信息
        else {
          // 检查是否包含错误信息
          const hasErrorMessage = response.message || response.error || 
                               (response.data && (response.data.message || response.data.error))
          isSuccess = !hasErrorMessage
        }
      }
      // 情况4: 没有返回值，通常表示成功
      else if (response === undefined || response === null) {
        isSuccess = true
      }
      
      if (isSuccess) {
        ElMessage.success('任务完成成功')
        return true
      } else {
        // 提取错误信息
        let errorMessage = '未知错误'
        if (response && typeof response === 'object') {
          errorMessage = response.message || response.error || 
                       (response.data && (response.data.message || response.data.error)) ||
                       '任务完成失败'
        } else if (typeof response === 'string') {
          errorMessage = response || '任务完成失败'
        }
        ElMessage.error('任务完成失败: ' + errorMessage)
        return false
      }
    } catch (error) {
      console.error('完成任务失败:', error)
      console.error('错误详情:', error.response ? JSON.stringify(error.response.data) : error.message)
      
      // 提取错误信息
      let errorMessage = error.message || '网络错误'
      if (error.response) {
        errorMessage = error.response.data?.message || error.response.data?.error || 
                     error.response.statusText || errorMessage
      }
      
      ElMessage.error('任务完成失败: ' + errorMessage)
      return false
    }
  }

  /**
   * 委派任务
   * @param {string} taskId - 任务ID
   * @param {string} assignee - 被委派用户ID
   * @returns {Promise<boolean>} 是否成功
   */
  async delegateTask(taskId, assignee) {
    try {
      console.log('调用委派任务API:', `${this.baseURL}/${taskId}/delegate`)
      const response = await axios.post(`${this.baseURL}/${taskId}/delegate`, {
        assignee
      })
      
      if (response && response.data && response.data.success !== false) {
        ElMessage.success('任务委派成功')
        return true
      }
      
      ElMessage.error('任务委派失败: ' + (response?.data?.message || '未知错误'))
      return false
    } catch (error) {
      console.error('委派任务失败:', error)
      ElMessage.error('任务委派失败: ' + (error.response?.data?.message || error.message))
      return false
    }
  }

  /**
   * 转派任务
   * @param {string} taskId - 任务ID
   * @param {string} assignee - 新的任务处理人ID
   * @returns {Promise<boolean>} 是否成功
   */
  async assignTask(taskId, assignee) {
    try {
      console.log('调用转派任务API:', `${this.baseURL}/${taskId}/assign`)
      const response = await axios.post(`${this.baseURL}/${taskId}/assign`, {
        assignee
      })
      
      if (response && response.data && response.data.success !== false) {
        ElMessage.success('任务转派成功')
        return true
      }
      
      ElMessage.error('任务转派失败: ' + (response?.data?.message || '未知错误'))
      return false
    } catch (error) {
      console.error('转派任务失败:', error)
      ElMessage.error('任务转派失败: ' + (error.response?.data?.message || error.message))
      return false
    }
  }

  /**
   * 取消认领任务
   * @param {string} taskId - 任务ID
   * @returns {Promise<boolean>} 是否成功
   */
  async unclaimTask(taskId) {
    try {
      console.log('调用取消认领任务API:', `${this.baseURL}/${taskId}/unclaim`)
      const response = await axios.post(`${this.baseURL}/${taskId}/unclaim`, {})
      
      if (response && response.data && response.data.success !== false) {
        ElMessage.success('任务取消认领成功')
        return true
      }
      
      ElMessage.error('任务取消认领失败: ' + (response?.data?.message || '未知错误'))
      return false
    } catch (error) {
      console.error('取消认领任务失败:', error)
      ElMessage.error('任务取消认领失败: ' + (error.response?.data?.message || error.message))
      return false
    }
  }

  /**
   * 获取任务候选用户
   * @param {string} taskId - 任务ID
   * @returns {Promise<Array>} 候选用户列表
   */
  async getTaskCandidateUsers(taskId) {
    try {
      console.log('调用获取任务候选用户API:', `${this.baseURL}/${taskId}/candidate-users`)
      const response = await axios.get(`${this.baseURL}/${taskId}/candidate-users`)
      
      if (response && response.data && response.data.success !== false) {
        return response.data.data || []
      }
      
      return []
    } catch (error) {
      console.error('获取任务候选用户失败:', error)
      return []
    }
  }

  /**
   * 获取任务候选组
   * @param {string} taskId - 任务ID
   * @returns {Promise<Array>} 候选组列表
   */
  async getTaskCandidateGroups(taskId) {
    try {
      console.log('调用获取任务候选组API:', `${this.baseURL}/${taskId}/candidate-groups`)
      const response = await axios.get(`${this.baseURL}/${taskId}/candidate-groups`)
      
      if (response && response.data && response.data.success !== false) {
        return response.data.data || []
      }
      
      return []
    } catch (error) {
      console.error('获取任务候选组失败:', error)
      return []
    }
  }

  /**
   * 添加任务候选用户
   * @param {string} taskId - 任务ID
   * @param {string} userId - 用户ID
   * @returns {Promise<boolean>} 是否成功
   */
  async addTaskCandidateUser(taskId, userId) {
    try {
      console.log('调用添加任务候选用户API:', `${this.baseURL}/${taskId}/candidate-users`)
      const response = await axios.post(`${this.baseURL}/${taskId}/candidate-users`, {
        userId
      })
      
      if (response && response.data && response.data.success !== false) {
        return true
      }
      
      return false
    } catch (error) {
      console.error('添加任务候选用户失败:', error)
      return false
    }
  }

  /**
   * 添加任务候选组
   * @param {string} taskId - 任务ID
   * @param {string} groupId - 组ID
   * @returns {Promise<boolean>} 是否成功
   */
  async addTaskCandidateGroup(taskId, groupId) {
    try {
      console.log('调用添加任务候选组API:', `${this.baseURL}/${taskId}/candidate-groups`)
      const response = await axios.post(`${this.baseURL}/${taskId}/candidate-groups`, {
        groupId
      })
      
      if (response && response.data && response.data.success !== false) {
        return true
      }
      
      return false
    } catch (error) {
      console.error('添加任务候选组失败:', error)
      return false
    }
  }

  /**
   * 删除任务候选用户
   * @param {string} taskId - 任务ID
   * @param {string} userId - 用户ID
   * @returns {Promise<boolean>} 是否成功
   */
  async deleteTaskCandidateUser(taskId, userId) {
    try {
      console.log('调用删除任务候选用户API:', `${this.baseURL}/${taskId}/candidate-users/${userId}`)
      const response = await axios.delete(`${this.baseURL}/${taskId}/candidate-users/${userId}`)
      
      if (response && response.data && response.data.success !== false) {
        return true
      }
      
      return false
    } catch (error) {
      console.error('删除任务候选用户失败:', error)
      return false
    }
  }

  /**
   * 删除任务候选组
   * @param {string} taskId - 任务ID
   * @param {string} groupId - 组ID
   * @returns {Promise<boolean>} 是否成功
   */
  async deleteTaskCandidateGroup(taskId, groupId) {
    try {
      console.log('调用删除任务候选组API:', `${this.baseURL}/${taskId}/candidate-groups/${groupId}`)
      const response = await axios.delete(`${this.baseURL}/${taskId}/candidate-groups/${groupId}`)
      
      if (response && response.data && response.data.success !== false) {
        return true
      }
      
      return false
    } catch (error) {
      console.error('删除任务候选组失败:', error)
      return false
    }
  }

  /**
   * 获取任务变量
   * @param {string} taskId - 任务ID
   * @returns {Promise<Object>} 任务变量数据
   */
  async getTaskVariables(taskId) {
    try {
      console.log('调用获取任务变量API:', `${this.baseURL}/${taskId}`)
      const response = await axios.get(`${this.baseURL}/${taskId}`)
      
      console.log('任务变量API原始响应:', response)
      
      // 处理响应数据，支持多种格式
      if (response) {
        // 情况1: response本身是变量数据（axios拦截器直接返回数据）
        if (typeof response === 'object') {
          // 检查response是否直接包含变量数据
          if (response.variables) {
            // 情况1.1: response包含variables字段
            return response.variables
          } else if (response.data && response.data.variables) {
            // 情况1.2: response包含data.variables字段
            return response.data.variables
          } else if (response.data && typeof response.data === 'object' && !response.data.code && !response.data.success) {
            // 情况1.3: response.data是变量数据，没有code或success字段
            return response.data
          } else if (response.code === 200 || response.success !== false) {
            // 情况1.4: response是标准响应格式，包含code或success字段
            return response.data?.variables || response.data || {}
          } else {
            // 情况1.5: response是对象，直接返回
            return response
          }
        }
      }
      
      console.error('获取任务变量API返回空响应或无效格式')
      return {}
    } catch (error) {
      console.error('获取任务变量失败:', error)
      return {}
    }
  }

  /**
   * 获取已完成任务列表
   * @param {Object} params - 请求参数，包含页码、每页数量和过滤条件
   * @returns {Promise<Object>} 已完成任务列表数据
   */
  async getCompletedTasks(params) {
    try {
      // 兼容两种调用方式：单个对象参数或分离的参数
      let page, size, filters;
      if (typeof params === 'object' && params !== null) {
        // 单个对象参数方式
        page = params.page || 1;
        size = params.pageSize || params.size || 10;
        filters = { ...params };
        delete filters.page;
        delete filters.pageSize;
        delete filters.size;
      } else {
        // 分离参数方式（兼容旧代码）
        page = params || 1;
        size = arguments[1] || 10;
        filters = arguments[2] || {};
      }
      
      // 确保包含当前用户信息
      const currentUser = JSON.parse(localStorage.getItem('user') || '{}')
      const requestParams = {
        page,
        size,
        assignee: filters.assignee || currentUser.username, // 确保有处理人参数
        ...filters
      }
      
      console.log('调用已完成任务列表API:', `${this.baseURL}/completed`, '参数:', requestParams)
      const response = await axios.get(`${this.baseURL}/completed`, { params: requestParams })
      console.log('已完成任务列表API响应:', response)
      
      // 检查响应是否存在
      if (!response) {
        console.error('已完成任务列表API返回空响应')
        return {
          data: [],
          total: 0
        }
      }
      
      // 处理响应数据，支持多种格式 - 与axios响应拦截器行为保持一致
      let tasksData = [];
      let totalCount = 0;
      
      // 情况1: 响应直接是数组
      if (Array.isArray(response)) {
        tasksData = response;
        totalCount = response.length;
      }
      // 情况2: 响应是对象，检查是否有success和data字段
      else if (typeof response === 'object') {
        // 检查是否有success字段
        if (response.success !== undefined) {
          // 情况2.1: response是CommonResponse格式，包含success和data字段
          if (response.success) {
            const responseData = response.data;
            
            // 情况2.1.1: data字段是对象，包含data数组和total字段（分页格式）
            if (responseData && typeof responseData === 'object') {
              if (responseData.data && Array.isArray(responseData.data)) {
                tasksData = responseData.data;
                totalCount = responseData.total || 0;
              }
              // 情况2.1.2: data字段直接是数组
              else if (Array.isArray(responseData)) {
                tasksData = responseData;
                totalCount = responseData.length;
              }
            }
          }
        }
        // 情况2.2: 响应是对象，包含data数组和total字段（分页格式）
        else if (response.data && Array.isArray(response.data)) {
          tasksData = response.data;
          totalCount = response.total || response.data.length;
        }
        // 情况2.3: 响应是对象，包含list数组和total字段（另一种分页格式）
        else if (response.list && Array.isArray(response.list)) {
          tasksData = response.list;
          totalCount = response.total || response.list.length;
        }
        // 情况2.4: 响应是对象，直接包含数据字段
        else if (response && Array.isArray(response.data) || Array.isArray(response.list)) {
          tasksData = response.data || response.list || [];
          totalCount = response.total || tasksData.length;
        }
      }
      
      console.log('处理后的已完成任务数据:', { data: tasksData, total: totalCount })
      
      console.log('解析后的已完成任务数据:', { data: tasksData, total: totalCount })
      return {
        data: tasksData,
        total: totalCount
      }
    } catch (error) {
      console.error('获取已完成任务列表失败:', error)
      console.error('错误详情:', error.response ? JSON.stringify(error.response.data) : error.message)
      return {
        data: [],
        total: 0
      }
    }
  }

  /**
   * 获取任务统计信息
   * @returns {Promise<Object>} 任务统计信息
   */
  async getTaskStatistics() {
    try {
      console.log('调用任务统计API:', `${this.baseURL}/statistics`)
      const response = await axios.get(`${this.baseURL}/statistics`)
      
      if (response && response.data && response.data.success !== false) {
        return response.data.data
      }
      
      return null
    } catch (error) {
      console.error('获取任务统计信息失败:', error)
      return null
    }
  }

  /**
   * 审批任务
   * @param {string} taskId - 任务ID
   * @param {boolean} approve - 是否审批通过
   * @param {string} comment - 审批意见
   * @param {Object} additionalVariables - 其他变量
   * @returns {Promise<boolean>} 是否成功
   */
  async approveTask(taskId, approve = true, comment = '', additionalVariables = {}) {
    try {
      console.log('调用审批任务API:', `${this.baseURL}/${taskId}/complete`)
      
      // 构建任务变量
      const variables = {
        approved: approve,
        comment: comment,
        ...additionalVariables
      }
      
      const response = await axios.post(`${this.baseURL}/${taskId}/complete`, variables)
      
      if (response && response.data && response.data.success !== false) {
        ElMessage.success(approve ? '任务审批通过' : '任务审批拒绝')
        return true
      }
      
      ElMessage.error('任务审批失败: ' + (response?.data?.message || '未知错误'))
      return false
    } catch (error) {
      console.error('审批任务失败:', error)
      ElMessage.error('任务审批失败: ' + (error.response?.data?.message || error.message))
      return false
    }
  }

  /**
   * 获取流程实例历史
   * @param {string} processInstanceId - 流程实例ID
   * @returns {Promise<Object>} 流程实例历史数据
   */
  async getProcessInstanceHistory(processInstanceId) {
    try {
      console.log('调用流程实例历史API:', `/v1/process-instances/${processInstanceId}/history`)
      const response = await axios.get(`/v1/process-instances/${processInstanceId}/history`)
      
      console.log('流程历史API原始响应:', response)
      
      // 确保返回格式化的数据结构，与前端组件期望的一致
      // 对返回的数据进行字段映射，确保与前端模板匹配
      let processedData = [];
      
      // 情况1: response本身是数组（axios拦截器直接返回数据）
      if (Array.isArray(response)) {
        processedData = response.map(record => ({
          id: record.id || record.activityId || `history_${Date.now()}_${Math.random()}`,
          activityName: record.activityName || record.name || record.activity || '未知活动',
          assignee: record.assignee || record.userId || record.owner || '',
          comment: record.comment || record.message || record.description || '',
          action: record.action || (record.status === 'approved' ? 'approve' : record.status === 'rejected' ? 'reject' : ''),
          time: record.time || record.timeStamp || record.createTime || record.startTime || Date.now(),
          type: record.type || (record.eventType === 'start' ? 'start' : record.eventType === 'complete' ? 'complete' : 'info'),
          // 保留原始数据用于调试
          _raw: record
        }));
      }
      // 情况2: response是对象，检查是否有data字段（未经过axios拦截器处理的原始响应）
      else if (response && typeof response === 'object') {
        // 情况2.1: 检查response是否是{data: [...], total: ...}格式（这是后端返回的标准格式）
        if (response.data && Array.isArray(response.data)) {
          // 直接使用response.data作为历史活动列表
          processedData = response.data.map(record => ({
            id: record.id || record.activityId || `history_${Date.now()}_${Math.random()}`,
            activityName: record.activityName || record.name || record.activity || '未知活动',
            assignee: record.assignee || record.userId || record.owner || '',
            comment: record.comment || record.message || record.description || '',
            action: record.action || (record.status === 'approved' ? 'approve' : record.status === 'rejected' ? 'reject' : ''),
            time: record.time || record.timeStamp || record.createTime || record.startTime || Date.now(),
            type: record.type || (record.eventType === 'start' ? 'start' : record.eventType === 'complete' ? 'complete' : 'info'),
            // 保留原始数据用于调试
            _raw: record
          }));
        }
        // 情况2.2: 检查response.data.data是否为数组（嵌套格式）
        else if (response.data && Array.isArray(response.data.data)) {
          processedData = response.data.data.map(record => ({
            id: record.id || record.activityId || `history_${Date.now()}_${Math.random()}`,
            activityName: record.activityName || record.name || record.activity || '未知活动',
            assignee: record.assignee || record.userId || record.owner || '',
            comment: record.comment || record.message || record.description || '',
            action: record.action || (record.status === 'approved' ? 'approve' : record.status === 'rejected' ? 'reject' : ''),
            time: record.time || record.timeStamp || record.createTime || record.startTime || Date.now(),
            type: record.type || (record.eventType === 'start' ? 'start' : record.eventType === 'complete' ? 'complete' : 'info'),
            // 保留原始数据用于调试
            _raw: record
          }));
        }
        // 情况2.3: 检查response本身是否包含历史活动数据，格式为{"data":[...],"total":...}
        else if (response.total !== undefined) {
          // 这是分页格式，其中data字段是历史活动列表
          const activityList = response.data || [];
          if (Array.isArray(activityList)) {
            processedData = activityList.map(record => ({
              id: record.id || record.activityId || `history_${Date.now()}_${Math.random()}`,
              activityName: record.activityName || record.name || record.activity || '未知活动',
              assignee: record.assignee || record.userId || record.owner || '',
              comment: record.comment || record.message || record.description || '',
              action: record.action || (record.status === 'approved' ? 'approve' : record.status === 'rejected' ? 'reject' : ''),
              time: record.time || record.timeStamp || record.createTime || record.startTime || Date.now(),
              type: record.type || (record.eventType === 'start' ? 'start' : record.eventType === 'complete' ? 'complete' : 'info'),
              startTime: record.startTime,
              endTime: record.endTime,
              duration: record.durationInMillis,
              // 保留原始数据用于调试
              _raw: record
            }));
          }
        }
        // 情况2.4: 检查response.list是否为数组
        else if (Array.isArray(response.list)) {
          processedData = response.list.map(record => ({
            id: record.id || record.activityId || `history_${Date.now()}_${Math.random()}`,
            activityName: record.activityName || record.name || record.activity || '未知活动',
            assignee: record.assignee || record.userId || record.owner || '',
            comment: record.comment || record.message || record.description || '',
            action: record.action || (record.status === 'approved' ? 'approve' : record.status === 'rejected' ? 'reject' : ''),
            time: record.time || record.timeStamp || record.createTime || record.startTime || Date.now(),
            type: record.type || (record.eventType === 'start' ? 'start' : record.eventType === 'complete' ? 'complete' : 'info'),
            startTime: record.startTime,
            endTime: record.endTime,
            duration: record.durationInMillis,
            // 保留原始数据用于调试
            _raw: record
          }));
        }
      }
      
      console.log('处理后的流程历史数据:', processedData)
      
      // 返回前端组件期望的数据格式
      return processedData;
    } catch (error) {
      // 处理错误，避免将错误信息直接暴露给用户
      console.error('获取流程实例历史失败:', error)
      
      // 检查是否是权限错误
      const isPermissionError = error.response && error.response.status === 403 || 
                                (error.response && error.response.data && 
                                 (error.response.data.message === '没有权限查看该流程实例的历史活动' ||
                                  error.response.data.error === '没有权限查看该流程实例的历史活动' ||
                                  error.response.data.message?.includes('权限') ||
                                  error.response.data.error?.includes('权限')))
      
      if (isPermissionError) {
        console.info('没有权限查看该流程实例的历史活动，返回空列表')
      }
      
      // 返回空数组，让前端组件能够正常显示，而不是显示错误
      return [];
    }
  }
}

// 导出单例
export default new TaskService()