<template>
  <div class="send-logs-container">
    <h3 class="section-title">发送日志</h3>
    
    <!-- 筛选条件 -->
    <el-card shadow="hover" class="filter-card">
      <el-form ref="filterFormRef" :model="filterForm" label-width="120px" class="filter-form">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="任务ID">
              <el-input v-model="filterForm.taskId" placeholder="请输入任务ID" clearable @input="handleFilterChange" @keyup.enter="handleSearch" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="模板名称">
              <el-input v-model="filterForm.templateName" placeholder="请输入模板名称" clearable @input="handleFilterChange" @keyup.enter="handleSearch" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="发送状态">
              <el-select v-model="filterForm.status" placeholder="请选择发送状态" clearable @change="handleFilterChange">
                <el-option label="待发送" value="pending" />
                <el-option label="发送中" value="sending" />
                <el-option label="成功" value="success" />
                <el-option label="失败" value="failed" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="发送时间">
              <el-date-picker
                v-model="filterForm.timeRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                @change="handleFilterChange"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="接收人">
              <el-input v-model="filterForm.recipient" placeholder="请输入接收人" clearable @input="handleFilterChange" @keyup.enter="handleSearch" />
            </el-form-item>
          </el-col>
          <el-col :span="18" style="text-align: right;">
            <el-form-item>
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon> 搜索
              </el-button>
              <el-button @click="handleReset">重置</el-button>
              <el-button type="success" @click="handleExport">
                <el-icon><Download /></el-icon> 导出
              </el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
    
    <!-- 日志列表 -->
    <el-card shadow="hover" class="logs-card">
      <el-table
        :data="logs"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
        row-key="id"
        v-loading="loading"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="日志ID" width="150" />
        <el-table-column prop="taskId" label="任务ID" width="150" />
        <el-table-column prop="templateName" label="模板名称" width="200" />
        <el-table-column prop="recipient" label="接收人" width="180" />
        <el-table-column prop="sendTime" label="发送时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)" size="small">
              {{ getStatusName(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="errorMsg" label="失败原因" min-width="150">
          <template #default="scope">
            <el-tooltip v-if="scope.row.errorMsg" :content="scope.row.errorMsg" placement="top">
              <span class="error-msg">{{ scope.row.errorMsg }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="variables" label="变量值" min-width="120">
          <template #default="scope">
            <el-button type="text" @click="handleViewVariables(scope.row)">
              查看
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="action" label="操作" width="120">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="handleRetry(scope.row)"
              :disabled="scope.row.status !== 'failed'"
            >
              重试
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalLogs"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 变量详情弹窗 -->
    <el-dialog
      v-model="variablesDialogVisible"
      title="变量值详情"
      width="500px"
    >
      <div v-if="selectedLog" class="variables-dialog">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="任务ID">{{ selectedLog.taskId }}</el-descriptions-item>
          <el-descriptions-item label="接收人">{{ selectedLog.recipient }}</el-descriptions-item>
          <el-descriptions-item label="变量值">
            <div class="variables-content">
              <pre>{{ JSON.stringify(selectedLog.variables, null, 2) }}</pre>
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="variablesDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Download } from '@element-plus/icons-vue'
import axios from '@/utils/axiosConfig'

// 筛选条件
const filterForm = reactive({
  taskId: '',
  templateName: '',
  status: '',
  timeRange: [],
  recipient: ''
})

// 防抖定时器
let debounceTimer = null

// 日志列表数据
const logs = ref([])
const loading = ref(false)

// 分页数据
const currentPage = ref(1)
const pageSize = ref(10)
const totalLogs = ref(0)
const selectedLogs = ref([])

// 获取日志列表
const getLogs = async () => {
  try {
    loading.value = true
    
    // 构建请求参数
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      taskId: filterForm.taskId,
      templateName: filterForm.templateName,
      status: filterForm.status,
      recipient: filterForm.recipient
    }
    
    // 添加时间范围参数
    if (filterForm.timeRange && filterForm.timeRange.length === 2) {
      params.startDate = filterForm.timeRange[0]
      params.endDate = filterForm.timeRange[1]
    }
    
    console.log('搜索参数:', params)
    
    // 调用后端API获取日志列表
    const response = await axios.get('/api/notification/logs', { params })
    console.log('日志列表响应:', response)
    
    // 检查response数据结构，确保正确获取list和total
    if (response && response.list) {
      // 添加调试日志，查看第一条日志的数据结构
      if (response.list.length > 0) {
        console.log('第一条日志数据:', response.list[0])
      }
      
      // 转换数据，适配前端字段期望
      logs.value = response.list.map(log => {
        // 解析params为JSON对象
        let variables = {}
        try {
          variables = log.params ? JSON.parse(log.params) : {}
        } catch (error) {
          console.error('解析params失败:', error)
          variables = {}
        }
        
        return {
          ...log,
          // 字段名映射
          recipient: log.receiver || '',
          // 保留后端返回的原始status值，不覆盖
          status: log.status || (log.success ? 'success' : 'failed'),
          // 解析变量值
          variables: variables,
          // 使用id作为任务ID的替代
          taskId: log.taskId || log.id || ''
        }
      })
      totalLogs.value = response.total || 0
    } else {
      logs.value = []
      totalLogs.value = 0
    }
  } catch (error) {
    console.error('获取发送日志失败:', error)
    ElMessage.error('获取发送日志失败: ' + (error.message || '请稍后重试'))
    logs.value = []
    totalLogs.value = 0
  } finally {
    loading.value = false
  }
}

// 组件挂载时获取日志列表
onMounted(() => {
  getLogs()
})

// 变量详情弹窗
const variablesDialogVisible = ref(false)
const selectedLog = ref(null)

// 状态映射
const getStatusName = (status) => {
  const statusMap = {
    pending: '待发送',
    sending: '发送中',
    success: '成功',
    failed: '失败'
  }
  return statusMap[status] || status
}

// 状态标签样式
const getStatusTagType = (status) => {
  const statusMap = {
    pending: 'info',
    sending: 'warning',
    success: 'success',
    failed: 'danger'
  }
  return statusMap[status] || 'info'
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  getLogs()
}

// 重置
const handleReset = () => {
  filterForm.taskId = ''
  filterForm.templateName = ''
  filterForm.status = ''
  filterForm.timeRange = []
  filterForm.recipient = ''
  currentPage.value = 1
  getLogs()
}

// 导出
const handleExport = async () => {
  try {
    // 构建请求参数
    const params = {
      page: 1,
      size: 1000, // 导出最多1000条记录
      taskId: filterForm.taskId,
      templateName: filterForm.templateName,
      status: filterForm.status,
      recipient: filterForm.recipient
    }
    
    // 添加时间范围参数
    if (filterForm.timeRange && filterForm.timeRange.length === 2) {
      params.startDate = filterForm.timeRange[0]
      params.endDate = filterForm.timeRange[1]
    }
    
    console.log('导出请求参数:', params)
    
    // 调用后端API导出日志
    const response = await axios.get('/api/notification/logs', {
      params,
      responseType: 'blob'
    })
    
    console.log('导出响应:', response)
    
    // 处理导出文件
    const blob = new Blob([response.data], { type: response.headers['content-type'] || 'application/octet-stream' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `notification_logs_${new Date().toISOString().split('T')[0]}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('日志导出成功')
  } catch (error) {
    console.error('导出日志失败:', error)
    console.error('导出错误详情:', error.response)
    ElMessage.error('导出日志失败: ' + (error.message || '请稍后重试'))
  }
}

// 状态变化处理
const handleStatusChange = (value) => {
  console.log('状态变化:', value)
  // 确保filterForm.status已正确更新
  console.log('filterForm.status:', filterForm.status)
}

// 过滤条件变化处理，带防抖
const handleFilterChange = () => {
  // 清除之前的定时器
  if (debounceTimer) {
    clearTimeout(debounceTimer)
  }
  
  // 设置新的定时器，500毫秒后执行搜索
  debounceTimer = setTimeout(() => {
    currentPage.value = 1 // 重新搜索时重置到第一页
    getLogs()
  }, 500)
}

// 选择日志
const handleSelectionChange = (selection) => {
  selectedLogs.value = selection
}

// 分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  getLogs()
}

// 页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  getLogs()
}

// 查看变量
const handleViewVariables = (row) => {
  selectedLog.value = row
  variablesDialogVisible.value = true
}

// 重试发送
const handleRetry = async (row) => {
  try {
    row.status = 'sending'
    ElMessage.success('已开始重试发送')
    
    // 调用后端API重试发送
    await axios.put(`/api/notification/logs/${row.id}/retry`)
    
    ElMessage.success('重试发送成功')
    // 刷新日志列表
    getLogs()
  } catch (error) {
    console.error('重试发送失败:', error)
    ElMessage.error('重试发送失败: ' + (error.message || '请稍后重试'))
    // 刷新日志列表
    getLogs()
  }
}
</script>

<style scoped>
.send-logs-container {
  padding: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #303133;
}

.filter-card {
  margin-bottom: 16px;
}

.filter-form {
  margin: 0;
}

.logs-card {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.error-msg {
  color: #f56c6c;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  width: 100%;
  display: inline-block;
}

.variables-content {
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
}

.variables-content pre {
  margin: 0;
  font-family: monospace;
  font-size: 14px;
}
</style>