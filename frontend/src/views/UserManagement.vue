<template>
  <div class="user-management-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="handleAddUser">新增用户</el-button>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-form">
        <el-form :inline="true" :model="searchForm" class="demo-form-inline">
          <el-form-item label="用户名">
            <el-input v-model="searchForm.username" placeholder="请输入用户名"></el-input>
          </el-form-item>
          <el-form-item label="角色">
            <el-select v-model="searchForm.roleCode" placeholder="请选择角色">
              <el-option label="全部" value=""></el-option>
              <el-option v-for="role in roles" :key="role.roleCode" :label="role.roleName" :value="role.roleCode"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 用户列表 -->
      <el-table :data="userList" style="width: 100%" @selection-change="handleSelectionChange" ref="userTable">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="username" label="用户名"></el-table-column>
        <el-table-column prop="fullName" label="昵称"></el-table-column>
        <el-table-column prop="email" label="邮箱"></el-table-column>
        <el-table-column prop="phone" label="手机号"></el-table-column>
        <el-table-column label="角色" width="200">
          <template #default="scope">
            <span v-for="role in scope.row.roles" :key="role.roleCode || role.id" class="role-tag">
              {{ role.roleName || role.name || role }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180" prop="createTime"></el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEditUser(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDeleteUser(scope.row.id)">删除</el-button>
            <el-button :type="scope.row.status === 'ACTIVE' ? 'warning' : 'success'" size="small" @click="handleToggleStatus(scope.row)">
              {{ scope.row.status === 'ACTIVE' ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 批量操作工具栏 -->
      <div class="batch-operations" v-if="selectedUsers.length > 0">
        <el-button type="danger" @click="handleBatchDelete">批量删除</el-button>
      </div>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>

    <!-- 用户表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入用户名" :disabled="dialogType === 'edit' && formData.username === 'admin'"></el-input>
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="formData.nickname" placeholder="请输入昵称"></el-input>
        </el-form-item>
        <el-form-item v-if="dialogType === 'add'" label="密码" prop="password">
          <el-input v-model="formData.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="角色分配" prop="roleIds" required>
            <el-select
              v-model="formData.roleIds"
              multiple
              placeholder="请选择角色"
              clearable
              collapse-tags
              style="width: 100%"
            >
              <el-option
                v-for="role in roles"
                :key="role.id"
                :label="role.roleName"
                :value="role.id"
              ></el-option>
            </el-select>
          </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleDialogClose">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from '../utils/axiosConfig'
import userService from '../services/UserService'

export default {
  name: 'UserManagement',
  setup() {
    // 直接使用导入的userService实例
    
    // 响应式数据
    const userList = ref([])
    const roles = ref([])
    const dialogVisible = ref(false)
    const dialogType = ref('add') // add 或 edit
    const dialogTitle = ref('新增用户')
    const formRef = ref(null)
    const userTable = ref(null)
    const selectedUsers = ref([])
    
    // 搜索表单
    const searchForm = reactive({
      username: '',
      roleCode: ''
    })
    
    // 分页信息
    const pagination = reactive({
      currentPage: 1,
      pageSize: 10,
      total: 0
    })
    
    // 表单数据
    const formData = reactive({
      id: '',
      username: '',
      nickname: '',
      password: '',
      email: '',
      phone: '',
      roleIds: []
    })
    
    // 表单验证规则
    const formRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
      ],
      nickname: [
        { required: true, message: '请输入昵称', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度至少 6 个字符', trigger: 'blur' }
      ],
      email: [
        { message: '请输入正确的邮箱地址', trigger: 'blur', type: 'email' }
      ]
    }
    
    // 获取角色列表
    const getRoles = async () => {
      try {
        console.log('开始获取角色列表:', new Date().toISOString())
        // 直接使用userService获取角色列表
        const roleList = await userService.getRoles()
        
        if (Array.isArray(roleList)) {
          console.log(`成功获取 ${roleList.length} 个角色`)
          roles.value = roleList
        } else {
          console.error('获取角色列表响应格式错误:', roleList)
          ElMessage.error('获取角色列表失败，格式错误')
          // 使用默认角色数据作为备选
          roles.value = [
            { id: 1, roleName: '管理员', roleCode: 'ADMIN' },
            { id: 2, roleName: '部门经理', roleCode: 'DEPT_MANAGER' },
            { id: 3, roleName: '普通用户', roleCode: 'USER' }
          ]
        }
      } catch (error) {
        console.error('获取角色列表失败:', error)
        ElMessage.error(`获取角色列表失败: ${error.message || '未知错误'}`)
        // 出错时使用默认角色数据
        roles.value = [
          { id: 1, roleName: '管理员', roleCode: 'ADMIN' },
          { id: 2, roleName: '部门经理', roleCode: 'DEPT_MANAGER' },
          { id: 3, roleName: '普通用户', roleCode: 'USER' }
        ]
      }
    }
    
    // 获取用户列表
    const getUsers = async () => {
      try {
        // 使用UserService获取用户列表，不传递任何参数
        const users = await userService.getUsers()
        
        // 处理响应数据
        if (Array.isArray(users)) {
          userList.value = users
          pagination.total = users.length
        } else {
          // 使用模拟数据作为备选方案
          userList.value = [
            {
              id: '1',
              username: 'admin',
              fullName: '系统管理员',
              nickname: '系统管理员',
              email: 'admin@example.com',
              phone: '13800138000',
              department: '技术部',
              createTime: '2024-01-01 00:00:00',
              roles: [{ roleCode: 'ADMIN', roleName: '管理员' }]
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
              roles: [{ roleCode: 'MANAGER', roleName: '部门经理' }]
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
              roles: [{ roleCode: 'USER', roleName: '普通用户' }]
            }
          ]
          pagination.total = userList.value.length
        }
        
        // 确保每个用户都有角色信息
        for (const user of userList.value) {
          if (!user.roles) {
            user.roles = []
          }
        }
      } catch (error) {
        console.error('获取用户列表异常:', error)
        ElMessage.error('获取用户列表时发生异常')
        
        // 使用模拟数据作为最后的备选
        userList.value = [
          {
            id: '1',
            username: 'admin',
            fullName: '系统管理员',
            nickname: '系统管理员',
            email: 'admin@example.com',
            phone: '13800138000',
            department: '技术部',
            createTime: '2024-01-01 00:00:00',
            roles: [{ roleCode: 'ADMIN', roleName: '管理员' }]
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
            roles: [{ roleCode: 'MANAGER', roleName: '部门经理' }]
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
            roles: [{ roleCode: 'USER', roleName: '普通用户' }]
          }
        ]
        pagination.total = userList.value.length
      }
    }
    
    // 新增用户
    const handleAddUser = () => {
      dialogType.value = 'add'
      dialogTitle.value = '新增用户'
      // 重置表单
      if (formRef.value) {
        formRef.value.resetFields()
      }
      Object.assign(formData, {
        id: '',
        username: '',
        nickname: '',
        password: '',
        email: '',
        phone: '',
        roleIds: []
      })
      dialogVisible.value = true
    }
    
    // 编辑用户
    const handleEditUser = async (user) => {
      dialogType.value = 'edit'
      dialogTitle.value = '编辑用户'
      
      // 填充表单数据
      Object.assign(formData, {
        id: user.id,
        username: user.username,
        nickname: user.fullName || user.nickname || user.name,
        email: user.email || '',
        phone: user.phone || '',
        roleIds: []
      })
      
      // 获取用户的角色ID
      try {
        // 优先从用户对象中获取角色信息
        if (user.roles && user.roles.length > 0) {
          formData.roleIds = user.roles.map(role => role.id || role.roleId || role.roleCode)
        } else {
          // 尝试从API获取
          const res = await axios.get(`/user-roles/user/${user.id}`)
          if (res.data && (res.data.code === 200 || res.status === 200)) {
            formData.roleIds = res.data.data || []
          }
        }
      } catch (error) {
        console.error('获取用户角色失败:', error)
        // 不中断流程，继续编辑
      }
      
      dialogVisible.value = true
    }
    
    // 删除单个用户
    const handleDeleteUser = (userId) => {
      // 阻止删除管理员账户
      const user = userList.value.find(u => u.id === userId)
      if (user && user.username === 'admin') {
        ElMessage.warning('不能删除管理员账户')
        return
      }
      
      ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 使用UserService删除用户
        const response = await userService.deleteUser(userId)
        
        // 处理不同的响应格式
        // 响应拦截器会处理CommonResponse，直接返回成功结果或抛出错误
        // 所以如果能执行到这里，说明删除成功
        const success = true
        
        if (success) {
            ElMessage.success('删除成功')
            // 清空选中项
            if (userTable.value) {
              userTable.value.clearSelection()
            }
            selectedUsers.value = []
            // 重新加载用户列表
            getUsers()
          } else {
            ElMessage.error(response.data?.message || '删除失败')
          }
        } catch (error) {
          console.error('删除用户失败:', error)
          ElMessage.error(error.response?.data?.message || '删除失败')
        }
      }).catch(() => {
        // 取消删除
      })
    }
    
    // 批量删除用户
    const handleBatchDelete = () => {
      // 检查是否包含管理员账户
      const hasAdmin = selectedUsers.value.some(user => user.username === 'admin')
      if (hasAdmin) {
        ElMessage.warning('不能批量删除包含管理员的用户')
        return
      }
      
      const userIds = selectedUsers.value.map(user => user.id)
      
      ElMessageBox.confirm(`确定要删除选中的 ${userIds.length} 个用户吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 使用UserService批量删除用户
        const response = await userService.batchDeleteUsers(userIds)
        
        // 处理批量删除结果
        if (response && response.success !== false) {
          ElMessage.success(`成功删除 ${userIds.length} 个用户`)
          // 清空选中项
          if (userTable.value) {
            userTable.value.clearSelection()
          }
          selectedUsers.value = []
          // 重新加载用户列表
          getUsers()
        } else {
          ElMessage.error('批量删除失败')
        }
        } catch (error) {
          console.error('批量删除用户失败:', error)
          ElMessage.error(error.response?.data?.message || '批量删除失败')
        }
      }).catch(() => {
        // 取消删除
      })
    }
    
    // 切换用户状态
    const handleToggleStatus = (user) => {
      // 阻止修改管理员状态
      if (user.username === 'admin') {
        ElMessage.warning('不能修改管理员账户状态')
        return
      }
      
      const newStatus = user.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
      const actionText = newStatus === 'ACTIVE' ? '启用' : '禁用'
      
      ElMessageBox.confirm(`确定要${actionText}该用户吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 尝试使用updateUserStatus方法
          const response = await userService.updateUserStatus(user.id, newStatus)
          
          // 处理不同的响应格式
          const success = (response && response.success) || 
                         (response && response.data && (response.data.code === 200 || response.data.code === 0)) ||
                         (response && response.status === 200)
          
          if (success) {
            ElMessage.success(`${actionText}成功`)
            // 重新加载用户列表
            getUsers()
          } else {
            ElMessage.error(`${actionText}失败`)
          }
        } catch (error) {
          console.error(`${actionText}用户失败:`, error)
          ElMessage.error(error.response?.data?.message || `${actionText}失败`)
        }
      }).catch(() => {
        // 取消操作
      })
    }
    
    // 处理选择变化
    const handleSelectionChange = (selection) => {
      selectedUsers.value = selection
    }
    
    // 提交表单
    const handleSubmit = async () => {
      try {
        await formRef.value.validate()
        
        // 准备请求数据，确保字段简洁明了
        const requestData = {
          username: formData.username.trim(), // 去除首尾空格
          fullName: formData.nickname.trim(),  // 只使用一个字段避免混淆
          email: (formData.email || '').trim(),
          phone: (formData.phone || '').trim()
        }
        
        // 处理角色数据
        if (formData.roleIds && formData.roleIds.length > 0) {
          requestData.roles = formData.roleIds.map(roleId => ({ id: roleId }))
          console.log('用户角色数据:', requestData.roles)
        }
        
        // 如果是新增，添加密码字段
        if (dialogType.value === 'add') {
          requestData.password = formData.password
          console.log('准备创建新用户:', requestData.username)
        }
        
        // 提交用户数据
        let response
        if (dialogType.value === 'add') {
          console.log('提交创建用户请求:', requestData)
          response = await userService.createUser(requestData)
        } else {
          console.log('提交更新用户请求:', formData.id, requestData)
          response = await userService.updateUser(formData.id, requestData)
        }
        
        // 处理不同的响应格式
        const success = response.success || 
                       (response.data && (response.data.code === 200 || response.data.code === 0)) ||
                       response.status === 200
        
        if (success) {
          ElMessage.success(dialogType.value === 'add' ? '创建成功' : '更新成功')
          dialogVisible.value = false
          // 清空选中项
          if (userTable.value) {
            userTable.value.clearSelection()
          }
          selectedUsers.value = []
          // 重新加载用户列表
          getUsers()
        } else {
          ElMessage.error(response.data?.message || (dialogType.value === 'add' ? '创建失败' : '更新失败'))
        }
      } catch (error) {
        console.error('提交表单异常:', error)
        if (error.response) {
          ElMessage.error(error.response.data?.message || '操作失败')
        } else if (error.message) {
          ElMessage.error(error.message)
        } else {
          // 表单验证失败会自动提示
        }
      }
    }
    
    // 关闭对话框
    const handleDialogClose = () => {
      dialogVisible.value = false
      if (formRef.value) {
        formRef.value.resetFields()
      }
    }
    
    // 搜索
    const handleSearch = () => {
      pagination.currentPage = 1
      getUsers()
    }
    
    // 重置搜索
    const resetSearch = () => {
      searchForm.username = ''
      searchForm.roleCode = ''
      pagination.currentPage = 1
      getUsers()
    }
    
    // 分页处理
    const handleSizeChange = (size) => {
      pagination.pageSize = size
      getUsers()
    }
    
    const handleCurrentChange = (current) => {
      pagination.currentPage = current
      getUsers()
    }
    
    // 初始化
    onMounted(() => {
      getRoles()
      getUsers()
    })
    
    return {
      userList,
      roles,
      dialogVisible,
      dialogType,
      dialogTitle,
      formRef,
      userTable,
      selectedUsers,
      formData,
      formRules,
      searchForm,
      pagination,
      handleAddUser,
      handleEditUser,
      handleDeleteUser,
      handleBatchDelete,
      handleToggleStatus,
      handleSubmit,
      handleDialogClose,
      handleSearch,
      resetSearch,
      handleSizeChange,
      handleCurrentChange,
      handleSelectionChange
    }
  }
}
</script>

<style scoped>
.user-management-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.batch-operations {
  margin-top: 15px;
  margin-bottom: 15px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.role-tag {
  display: inline-block;
  background-color: #409eff;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  margin-right: 4px;
  margin-bottom: 4px;
  font-size: 12px;
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .el-table-column--fixed-right {
    width: 220px !important;
  }
}

@media (max-width: 992px) {
  .el-table-column--fixed-right {
    width: 180px !important;
  }
}
</style>