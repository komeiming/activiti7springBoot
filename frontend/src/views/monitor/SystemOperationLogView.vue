<template>
  <div class="system-operation-log-container">
    <h1 class="page-title">系统操作日志</h1>
    
    <!-- 操作按钮区域 -->
    <div class="action-bar">
      <el-button type="primary" @click="handleRefresh">
        <el-icon><RefreshRight /></el-icon>
        刷新日志
      </el-button>
      <el-button type="success" @click="handleExport">
        <el-icon><Download /></el-icon>
        导出日志
      </el-button>
    </div>
    
    <!-- 筛选条件区域 -->
    <el-card shadow="hover" class="filter-card">
      <el-form :inline="true" :model="filterForm" @submit.prevent>
        <el-form-item label="操作人">
          <el-input v-model="filterForm.operator" placeholder="请输入操作人用户名" />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="filterForm.operationType" placeholder="选择操作类型">
            <el-option label="所有操作" value="" />
            <el-option label="登录" value="login" />
            <el-option label="登出" value="logout" />
            <el-option label="创建" value="create" />
            <el-option label="更新" value="update" />
            <el-option label="删除" value="delete" />
            <el-option label="导出" value="export" />
            <el-option label="导入" value="import" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作时间">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 日志列表 -->
    <el-card shadow="hover" class="log-list-card">
      <el-table
        :data="logList"
        border
        style="width: 100%"
        v-loading="loading"
        element-loading-text="加载中..."
        element-loading-spinner="el-icon-loading"
        element-loading-background="rgba(0, 0, 0, 0.8)"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column prop="operationType" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getOperationTypeTagType(row.operationType)">
              {{ getOperationTypeName(row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetType" label="操作对象" width="120" />
        <el-table-column prop="targetId" label="对象ID" width="100" />
        <el-table-column prop="targetName" label="对象名称" width="150" />
        <el-table-column prop="description" label="操作描述" min-width="200" />
        <el-table-column prop="ipAddress" label="IP地址" width="150" />
        <el-table-column prop="operationTime" label="操作时间" width="180" sortable>
          <template #default="{ row }">
            <span>{{ formatDate(row.operationTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewDetail(row)">
              详情
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
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 日志详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="日志详情"
      width="600px"
      destroy-on-close
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="日志ID">{{ currentLog.id }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentLog.operator }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ getOperationTypeName(currentLog.operationType) }}</el-descriptions-item>
        <el-descriptions-item label="操作对象">{{ currentLog.targetType }}</el-descriptions-item>
        <el-descriptions-item label="对象ID">{{ currentLog.targetId }}</el-descriptions-item>
        <el-descriptions-item label="对象名称">{{ currentLog.targetName }}</el-descriptions-item>
        <el-descriptions-item label="操作描述">{{ currentLog.description }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentLog.ipAddress }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ formatDate(currentLog.operationTime) }}</el-descriptions-item>
        <el-descriptions-item label="详细内容" v-if="currentLog.details">
          <el-card shadow="never" class="detail-content">
            <pre>{{ JSON.stringify(currentLog.details, null, 2) }}</pre>
          </el-card>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { RefreshRight, Download, Search, RefreshLeft } from '@element-plus/icons-vue'
import MonitorService from '../../services/MonitorService'

// 日志列表数据
const logList = ref([])
// 加载状态
const loading = ref(false)
// 筛选表单
const filterForm = reactive({
  operator: '',
  operationType: '',
  dateRange: []
})
// 分页参数
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})
// 详情对话框
const detailVisible = ref(false)
const currentLog = ref({})



// 获取操作类型名称
const getOperationTypeName = (type) => {
  const typeMap = {
    'login': '登录',
    'logout': '登出',
    'create': '创建',
    'update': '更新',
    'delete': '删除',
    'export': '导出',
    'import': '导入'
  }
  return typeMap[type] || type
}

// 获取操作类型标签颜色
const getOperationTypeTagType = (type) => {
  const typeMap = {
    'login': 'success',
    'logout': 'info',
    'create': 'primary',
    'update': 'warning',
    'delete': 'danger',
    'export': 'success',
    'import': 'success'
  }
  return typeMap[type] || 'info'
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString()
}

// 刷新日志
const handleRefresh = () => {
  loadLogs()
  ElMessage.success('日志已刷新')
}

// 导出日志
const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

// 搜索日志
const handleSearch = () => {
  pagination.currentPage = 1
  loadLogs()
  ElMessage.success('搜索完成')
}

// 重置筛选条件
const handleReset = () => {
  Object.assign(filterForm, {
    operator: '',
    operationType: '',
    dateRange: []
  })
  pagination.currentPage = 1
  loadLogs()
  ElMessage.success('筛选条件已重置')
}

// 查看详情
const handleViewDetail = (row) => {
  currentLog.value = { ...row }
  detailVisible.value = true
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadLogs()
}

// 当前页变化
const handleCurrentChange = (current) => {
  pagination.currentPage = current
  loadLogs()
}

// 加载日志数据
const loadLogs = async () => {
  loading.value = true
  try {
    // 构建查询参数
    const params = {
      operator: filterForm.operator,
      operationType: filterForm.operationType,
      page: pagination.currentPage,
      pageSize: pagination.pageSize
    }
    
    // 添加时间范围参数
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startDate = filterForm.dateRange[0]
      params.endDate = filterForm.dateRange[1]
    }
    
    // 调用后端API获取真实数据
    const response = await MonitorService.getSystemLogs(params)
    
    // 更新数据
    if (response && response.data) {
      logList.value = response.data || []
      pagination.total = response.total || 0
    } else {
      logList.value = response || []
      pagination.total = logList.value.length
    }
    
    loading.value = false
  } catch (error) {
    console.error('加载系统操作日志失败:', error)
    ElMessage.error('加载日志失败，请稍后重试')
    loading.value = false
  }
}

// 组件挂载时加载日志
onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.system-operation-log-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #333;
}

.action-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 15px;
}

.filter-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.detail-content {
  margin-top: 10px;
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
}

.detail-content pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>
