import axios from '../utils/axiosConfig'
import { ElMessage } from 'element-plus'

class RoleService {
  // 获取角色列表
  async getRoles(params = {}) {
    try {
      const res = await axios.get('/api/roles', { params }) 
      
      console.log('角色API返回结果:', res)
      
      // 处理不同的响应格式
      // 注意：由于axios拦截器的存在，res可能已经是处理后的数据，而不是完整的axios响应对象
      
      // 情况1: res直接是数组（最常见的情况，因为axios拦截器会返回res.data || res）
      if (Array.isArray(res)) {
        console.log('直接返回数组:', res)
        return res;
      }
      // 情况2: res是对象，包含data字段（可能是未经过拦截器处理的完整响应）
      else if (res.data) {
        console.log('res包含data字段:', res.data)
        // 情况2.1: res.data是数组
        if (Array.isArray(res.data)) {
          return res.data;
        }
        // 情况2.2: res.data是对象，包含data数组
        else if (res.data.data && Array.isArray(res.data.data)) {
          return res.data.data;
        }
        // 情况2.3: res.data是对象，包含list数组
        else if (res.data.list && Array.isArray(res.data.list)) {
          return res.data.list;
        }
        // 情况2.4: res.data是对象，直接返回
        else if (res.data) {
          return res.data;
        }
      }
      // 情况3: res是对象，直接返回
      else if (res && typeof res === 'object') {
        console.log('直接返回对象:', res)
        return res;
      }
      
      console.error('获取角色列表失败，使用模拟数据:', res)
      return this.getMockRoles()
    } catch (error) {
      console.error('获取角色列表异常:', error)
      ElMessage.warning('获取角色列表失败，显示模拟数据')
      return this.getMockRoles()
    }
  }
  
  // 获取模拟角色数据
  getMockRoles() {
    return [
      { id: 1, roleName: '管理员', roleCode: 'ROLE_ADMIN', description: '系统管理员' },
      { id: 2, roleName: '部门经理', roleCode: 'ROLE_MANAGER', description: '部门经理' },
      { id: 3, roleName: '普通用户', roleCode: 'ROLE_USER', description: '普通用户' },
      { id: 4, roleName: 'IT人员', roleCode: 'ROLE_IT', description: 'IT技术支持' },
      { id: 5, roleName: '财务人员', roleCode: 'ROLE_FINANCE', description: '财务部门' }
    ]
  }
  
  // 新增角色
  async addRole(roleData) {
    try {
      // 创建新对象，排除id字段，避免主键约束冲突
      const newRoleData = { ...roleData }
      delete newRoleData.id
      
      const res = await axios.post('/api/roles', newRoleData)
      
      console.log('新增角色API响应:', res)
      
      // 检查是否成功 - 兼容多种响应格式
      // 情况1: res本身是对象（可能是axios拦截器处理后的结果）
      if (typeof res === 'object' && res !== null) {
        // 情况1.1: 检查code字段
        if (res.code === 200 || res.code === 0) {
          return res
        }
        // 情况1.2: 检查success字段
        else if (res.success !== undefined) {
          return res
        }
        // 情况1.3: 检查data.code字段
        else if (res.data && res.data.code === 200) {
          return res.data
        }
        // 情况1.4: 检查data.success字段
        else if (res.data && res.data.success !== undefined) {
          return res.data
        }
      }
      
      // 情况2: 直接返回成功，因为如果失败会在axios拦截器中抛出异常
      return res
    } catch (error) {
      console.error('新增角色异常:', error)
      // 即使出错也返回错误信息，让调用者处理
      throw error
    }
  }
  
  // 更新角色
  async updateRole(roleId, roleData) {
    try {
      const res = await axios.put(`/api/roles/${roleId}`, roleData)
      
      // 检查是否成功
      if (res.data && (res.data.code === 200 || res.status === 200 || res.data.success)) {
        return res.data
      } else {
        throw new Error(res.data?.message || '更新角色失败')
      }
    } catch (error) {
      console.error('更新角色异常:', error)
      throw error
    }
  }
  
  // 删除角色
  async deleteRole(roleId) {
    try {
      const res = await axios.delete(`/api/roles/${roleId}`)
      
      // 检查是否成功
      if (res.data && (res.data.code === 200 || res.status === 200 || res.data.success)) {
        return res.data
      } else {
        throw new Error(res.data?.message || '删除角色失败')
      }
    } catch (error) {
      console.error('删除角色异常:', error)
      throw error
    }
  }
  
  // 根据用户ID获取角色
  async getRolesByUserId(userId) {
    try {
      const res = await axios.get(`/api/roles/by-user/${userId}`)
      
      // 处理不同的响应格式
      if (res.data && (res.data.code === 200 || res.status === 200)) {
        return res.data.data || res.data || []
      } else {
        console.error('获取用户角色失败:', res.data)
        return []
      }
    } catch (error) {
      console.error('获取用户角色异常:', error)
      return []
    }
  }
}

export default new RoleService()