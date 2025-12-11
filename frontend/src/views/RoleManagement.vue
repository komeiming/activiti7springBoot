<template>
  <div class="role-management-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" @click="handleAddRole">新增角色</el-button>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-form">
        <el-form :inline="true" :model="searchForm" class="demo-form-inline">
          <el-form-item label="角色名称">
            <el-input v-model="searchForm.roleName" placeholder="请输入角色名称"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 角色列表 -->
      <el-table :data="roleList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="roleName" label="角色名称"></el-table-column>
        <el-table-column prop="roleCode" label="角色编码"></el-table-column>
        <el-table-column prop="description" label="描述"></el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEditRole(scope.row)">
              编辑
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleDeleteRole(scope.row)"
              :disabled="scope.row.roleCode === 'ROLE_ADMIN'"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

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

    <!-- 角色表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="formData.roleName" placeholder="请输入角色名称"></el-input>
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input 
            v-model="formData.roleCode" 
            placeholder="请输入角色编码(如: ROLE_ADMIN)" 
            :disabled="dialogType === 'edit'"
          ></el-input>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input 
            v-model="formData.description" 
            placeholder="请输入角色描述" 
            type="textarea" 
            :rows="3"
          ></el-input>
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
import roleService from '../services/RoleService'

export default {
  name: 'RoleManagement',
  setup() {
    
    // 响应式数据
    const roleList = ref([])
    const dialogVisible = ref(false)
    const dialogType = ref('add') // add 或 edit
    const dialogTitle = ref('新增角色')
    const formRef = ref(null)
    
    // 搜索表单
    const searchForm = reactive({
      roleName: ''
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
      roleName: '',
      roleCode: '',
      description: ''
    })
    
    // 表单验证规则
    const formRules = {
      roleName: [
        { required: true, message: '请输入角色名称', trigger: 'blur' }
      ],
      roleCode: [
        { required: true, message: '请输入角色编码', trigger: 'blur' },
        { pattern: /^ROLE_[A-Z_]+$/, message: '角色编码必须以ROLE_开头，只包含大写字母和下划线', trigger: 'blur' }
      ]
    }
    
    // 获取角色列表
    const getRoles = async () => {
      try {
        // 使用RoleService获取角色列表
        const roles = await roleService.getRoles({
          roleName: searchForm.roleName
        })
        
        roleList.value = roles
        pagination.total = roles.length
      } catch (error) {
        console.error('获取角色列表异常:', error)
        ElMessage.error('获取角色列表失败')
      }
    }
    
    // 新增角色
    const handleAddRole = () => {
      dialogType.value = 'add'
      dialogTitle.value = '新增角色'
      // 重置表单
      formRef.value?.resetFields()
      Object.assign(formData, {
        id: '',
        roleName: '',
        roleCode: '',
        description: ''
      })
      dialogVisible.value = true
    }
    
    // 编辑角色
    const handleEditRole = (role) => {
      dialogType.value = 'edit'
      dialogTitle.value = '编辑角色'
      
      // 填充表单数据
      Object.assign(formData, {
        id: role.id,
        roleName: role.roleName,
        roleCode: role.roleCode,
        description: role.description
      })
      
      dialogVisible.value = true
    }
    
    // 删除角色
    const handleDeleteRole = (role) => {
      if (role.roleCode === 'ROLE_ADMIN') {
        ElMessage.warning('管理员角色不能删除')
        return
      }
      
      ElMessageBox.confirm('确定要删除该角色吗？删除后相关用户的权限将被移除。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 使用RoleService删除角色
          await roleService.deleteRole(role.id)
          ElMessage.success('删除成功')
          getRoles()
        } catch (error) {
          console.error('删除角色异常:', error)
          ElMessage.error('删除失败: ' + (error.message || '请稍后重试'))
        }
      }).catch(() => {
        // 取消删除
      })
    }
    
    // 提交表单
    const handleSubmit = async () => {
      try {
        await formRef.value.validate()
        
        // 使用RoleService处理添加或更新
        if (dialogType.value === 'add') {
          await roleService.addRole(formData)
          ElMessage.success('创建成功')
        } else {
          await roleService.updateRole(formData.id, formData)
          ElMessage.success('更新成功')
        }
        
        dialogVisible.value = false
        getRoles()
      } catch (error) {
        console.error('提交表单异常:', error)
        ElMessage.error((dialogType.value === 'add' ? '创建失败: ' : '更新失败: ') + (error.message || '未知错误'))
        // 表单验证失败会自动提示
      }
    }
    
    // 关闭对话框
    const handleDialogClose = () => {
      dialogVisible.value = false
      formRef.value?.resetFields()
    }
    
    // 搜索
    const handleSearch = () => {
      pagination.currentPage = 1
      getRoles()
    }
    
    // 重置搜索
    const resetSearch = () => {
      searchForm.roleName = ''
      pagination.currentPage = 1
      getRoles()
    }
    
    // 分页处理
    const handleSizeChange = (size) => {
      pagination.pageSize = size
      getRoles()
    }
    
    const handleCurrentChange = (current) => {
      pagination.currentPage = current
      getRoles()
    }
    
    // 初始化
    onMounted(() => {
      getRoles()
    })
    
    return {
      roleList,
      dialogVisible,
      dialogType,
      dialogTitle,
      formRef,
      formData,
      formRules,
      searchForm,
      pagination,
      handleAddRole,
      handleEditRole,
      handleDeleteRole,
      handleSubmit,
      handleDialogClose,
      handleSearch,
      resetSearch,
      handleSizeChange,
      handleCurrentChange
    }
  }
}
</script>

<style scoped>
.role-management-container {
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

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>