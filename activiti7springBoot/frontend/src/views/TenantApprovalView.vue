<template>
  <div class="tenant-approval-view">
    <h2 class="page-title">租户审批管理</h2>
    
    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="租户状态">
          <el-select v-model="filterForm.status" placeholder="请选择状态" clearable>
            <el-option label="全部" value=""></el-option>
            <el-option label="待审批" value="pending"></el-option>
            <el-option label="已通过" value="approved"></el-option>
            <el-option label="已拒绝" value="rejected"></el-option>
            <el-option label="已暂停" value="suspended"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchTenants">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 租户列表 -->
    <el-card class="list-card">
      <template #header>
        <div class="card-header">
          <span>租户列表</span>
          <el-button type="primary" size="small" @click="refreshList">刷新</el-button>
        </div>
      </template>
      
      <el-table v-loading="loading" :data="tenants" style="width: 100%">
        <el-table-column prop="id" label="租户ID" width="180"></el-table-column>
        <el-table-column prop="systemName" label="系统名称" width="180"></el-table-column>
        <el-table-column prop="enterpriseName" label="企业名称"></el-table-column>
        <el-table-column prop="contactName" label="联系人" width="120"></el-table-column>
        <el-table-column prop="contactEmail" label="联系邮箱" width="200"></el-table-column>
        <el-table-column prop="serviceModules" label="服务模块" width="150"></el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag 
              :type="getStatusTagType(scope.row.status)" 
              :effect="getStatusTagEffect(scope.row.status)"
            >
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180" :formatter="formatDate"></el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewDetail(scope.row)">详情</el-button>
            <template v-if="scope.row.status === 'pending'">
              <el-button type="success" size="small" @click="approveTenant(scope.row)">通过</el-button>
              <el-button type="danger" size="small" @click="rejectTenant(scope.row)">拒绝</el-button>
            </template>
            <template v-else-if="scope.row.status === 'approved'">
              <el-button type="warning" size="small" @click="suspendTenant(scope.row)">暂停</el-button>
            </template>
            <template v-else-if="scope.row.status === 'suspended'">
              <el-button type="success" size="small" @click="resumeTenant(scope.row)">恢复</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
        ></el-pagination>
      </div>
    </el-card>
    
    <!-- 租户详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="租户详情"
      width="70%"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="租户ID">{{ selectedTenant?.id || '-' }}</el-descriptions-item>
        <el-descriptions-item label="系统名称">{{ selectedTenant?.systemName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="企业名称">{{ selectedTenant?.enterpriseName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="统一社会信用代码">{{ selectedTenant?.enterpriseCreditCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ selectedTenant?.contactName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ selectedTenant?.contactPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系邮箱">{{ selectedTenant?.contactEmail || '-' }}</el-descriptions-item>
        <el-descriptions-item label="服务模块">{{ selectedTenant?.serviceModules || '-' }}</el-descriptions-item>
        <el-descriptions-item label="调用场景">{{ selectedTenant?.callScenarios || '-' }}</el-descriptions-item>
        <el-descriptions-item label="APP ID">{{ selectedTenant?.appId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="密钥">
          <el-input
            v-if="selectedTenant?.secretKey"
            v-model="selectedTenant.secretKey"
            :type="secretKeyVisible ? 'text' : 'password'"
            readonly
            class="secret-key-input"
          >
            <template #append>
              <el-button
                @click="secretKeyVisible = !secretKeyVisible"
                :icon="secretKeyVisible ? 'el-icon-view' : 'el-icon-view-off'"
              ></el-button>
            </template>
          </el-input>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="回调地址">{{ selectedTenant?.callbackUrl || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ selectedTenant?.status ? getStatusText(selectedTenant.status) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ selectedTenant?.createdAt ? formatDate(selectedTenant.createdAt) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ selectedTenant?.approvedAt ? formatDate(selectedTenant.approvedAt) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核人">{{ selectedTenant?.approvedBy || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ selectedTenant?.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 拒绝原因对话框 -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="拒绝租户申请"
      width="50%"
    >
      <el-form :model="rejectForm" ref="rejectFormRef" label-width="100px">
        <el-form-item label="拒绝原因" required>
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            placeholder="请输入拒绝原因"
            :rows="4"
            maxlength="500"
            show-word-limit
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="submitReject">确定拒绝</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from '../utils/axiosConfig'

// 状态管理
const loading = ref(false)
const tenants = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 筛选表单
const filterForm = reactive({
  status: ''
})

// 详情对话框
const detailDialogVisible = ref(false)
const selectedTenant = ref({})
const secretKeyVisible = ref(false)

// 拒绝对话框
const rejectDialogVisible = ref(false)
const rejectForm = reactive({
  reason: ''
})
const rejectFormRef = ref()
const currentRejectTenant = ref(null)

// 页面加载时获取数据
onMounted(() => {
  fetchTenants()
})

// 获取租户列表
const fetchTenants = async () => {
  loading.value = true
  try {
    let url = '/api/admin/tenant/list'
    if (filterForm.status) {
      url = `/api/admin/tenant/list-by-status?status=${filterForm.status}`
    }
    const response = await axios.get(url)
    
    // 响应拦截器已经处理了code，直接返回数据
    tenants.value = response
    total.value = response.length
  } catch (error) {
    console.error('获取租户列表失败:', error)
    ElMessage.error('获取租户列表失败: ' + (error.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

// 刷新列表
const refreshList = () => {
  fetchTenants()
}

// 重置筛选条件
const resetFilter = () => {
  filterForm.status = ''
  fetchTenants()
}

// 查看详情
const viewDetail = (tenant) => {
  selectedTenant.value = { ...tenant }
  detailDialogVisible.value = true
}

// 审核通过租户
const approveTenant = (tenant) => {
  ElMessageBox.confirm('确定要审核通过该租户吗？', '审核确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await axios.post('/api/admin/tenant/approve', {
        tenantId: tenant.id,
        approvedBy: 'admin' // 实际应该从登录用户获取
      })
      
      // 响应拦截器已经处理了code，成功时直接返回数据，失败时会抛出异常
      ElMessage.success('审核通过成功')
      fetchTenants() // 刷新列表
    } catch (error) {
      console.error('审核通过租户失败:', error)
      ElMessage.error('审核通过租户失败: ' + (error.message || '网络错误'))
    }
  }).catch(() => {
    // 取消操作，不做处理
  })
}

// 拒绝租户申请
const rejectTenant = (tenant) => {
  currentRejectTenant.value = tenant
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

// 提交拒绝申请
const submitReject = async () => {
  // 表单验证
  try {
    await rejectFormRef.value.validate()
  } catch (err) {
    console.error('表单验证失败:', err)
    return
  }
  
  try {
    const response = await axios.post('/api/admin/tenant/reject', {
        tenantId: currentRejectTenant.value.id,
        approvedBy: 'admin', // 实际应该从登录用户获取
        reason: rejectForm.reason
      })
    
    // 响应拦截器已经处理了code，成功时直接返回数据，失败时会抛出异常
    ElMessage.success('拒绝申请成功')
    rejectDialogVisible.value = false
    fetchTenants() // 刷新列表
  } catch (error) {
    console.error('拒绝租户申请失败:', error)
    ElMessage.error('拒绝租户申请失败: ' + (error.message || '网络错误'))
  }
}

// 暂停租户服务
const suspendTenant = (tenant) => {
  ElMessageBox.confirm('确定要暂停该租户服务吗？', '暂停确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await axios.post(`/api/admin/tenant/suspend?tenantId=${tenant.id}`)
      
      // 响应拦截器已经处理了code，成功时直接返回数据，失败时会抛出异常
      ElMessage.success('暂停服务成功')
      fetchTenants() // 刷新列表
    } catch (error) {
      console.error('暂停租户服务失败:', error)
      ElMessage.error('暂停租户服务失败: ' + (error.message || '网络错误'))
    }
  }).catch(() => {
    // 取消操作，不做处理
  })
}

// 恢复租户服务
const resumeTenant = (tenant) => {
  ElMessageBox.confirm('确定要恢复该租户服务吗？', '恢复确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await axios.post(`/api/admin/tenant/resume?tenantId=${tenant.id}`)
      
      // 响应拦截器已经处理了code，成功时直接返回数据，失败时会抛出异常
      ElMessage.success('恢复服务成功')
      fetchTenants() // 刷新列表
    } catch (error) {
      console.error('恢复租户服务失败:', error)
      ElMessage.error('恢复租户服务失败: ' + (error.message || '网络错误'))
    }
  }).catch(() => {
    // 取消操作，不做处理
  })
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'pending': '待审批',
    'approved': '已通过',
    'rejected': '已拒绝',
    'suspended': '已暂停',
    'cancelled': '已注销'
  }
  return statusMap[status] || status
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  const typeMap = {
    'pending': 'warning',
    'approved': 'success',
    'rejected': 'danger',
    'suspended': 'info',
    'cancelled': 'error'
  }
  return typeMap[status] || 'info'
}

// 获取状态标签效果
const getStatusTagEffect = (status) => {
  return status === 'pending' ? 'dark' : 'light'
}

// 分页大小改变
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchTenants()
}

// 当前页码改变
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchTenants()
}
</script>

<style scoped>
.tenant-approval-view {
  padding: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #333;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  align-items: center;
  gap: 10px;
}

.list-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.dialog-footer {
  text-align: right;
}
</style>
