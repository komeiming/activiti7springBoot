<template>
  <div class="execution-logs-container">
    <h3 class="section-title">执行日志</h3>
    
    <!-- 操作按钮和筛选条件 -->
    <el-card shadow="hover" class="filter-card">
      <div class="filter-section">
        <div class="filter-right">
          <el-input
            v-model="filterForm.logId"
            placeholder="日志ID"
            clearable
            class="filter-input"
            @keyup.enter="handleSearch"
          />
          
          <el-input
            v-model="filterForm.instanceId"
            placeholder="实例ID"
            clearable
            class="filter-input"
          />
          
          <el-select
            v-model="filterForm.templateName"
            placeholder="模板名称"
            class="filter-select"
            clearable
          >
            <el-option label="请假流程" value="请假流程" />
            <el-option label="采购流程" value="采购流程" />
            <el-option label="报销流程" value="报销流程" />
          </el-select>
          
          <el-select
            v-model="filterForm.nodeName"
            placeholder="节点名称"
            class="filter-select"
            clearable
          >
            <el-option label="开始节点" value="开始节点" />
            <el-option label="部门经理审批" value="部门经理审批" />
            <el-option label="总经理审批" value="总经理审批" />
            <el-option label="结束节点" value="结束节点" />
          </el-select>
          
          <el-select
            v-model="filterForm.status"
            placeholder="执行状态"
            class="filter-select"
            clearable
          >
            <el-option label="成功" value="success" />
            <el-option label="失败" value="failed" />
          </el-select>
          
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          
          <el-button @click="handleResetFilter">重置</el-button>
          
          <el-button type="success" @click="handleExport">
            <el-icon><Download /></el-icon> 导出
          </el-button>
        </div>
      </div>
    </el-card>
    
    <!-- 日志列表 -->
    <el-card shadow="hover" class="logs-card">
      <el-table
        :data="logs"
        border
        style="width: 100%"
        @row-click="handleRowClick"
        row-key="id"
      >
        <el-table-column prop="id" label="日志ID" width="180" />
        <el-table-column prop="instanceId" label="实例ID" width="180" />
        <el-table-column prop="templateName" label="模板名称" width="150" />
        <el-table-column prop="nodeName" label="节点名称" width="150" />
        <el-table-column prop="executeTime" label="执行时间" width="180" />
        <el-table-column prop="status" label="执行状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)" size="small">
              {{ getStatusName(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="executeResult" label="执行结果" width="200">
          <template #default="scope">
            <el-tooltip v-if="scope.row.executeResult" :content="scope.row.executeResult" placement="top">
              <div class="execute-result">{{ scope.row.executeResult }}</div>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="errorMsg" label="失败原因" width="200">
          <template #default="scope">
            <el-tooltip v-if="scope.row.errorMsg" :content="scope.row.errorMsg" placement="top">
              <div class="error-msg">{{ scope.row.errorMsg }}</div>
            </el-tooltip>
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
              <el-icon><RefreshRight /></el-icon> 重试
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
    
    <!-- 执行详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="执行详情"
      width="600px"
    >
      <div v-if="selectedLog" class="detail-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="日志ID">{{ selectedLog.id }}</el-descriptions-item>
          <el-descriptions-item label="实例ID">{{ selectedLog.instanceId }}</el-descriptions-item>
          <el-descriptions-item label="模板名称">{{ selectedLog.templateName }}</el-descriptions-item>
          <el-descriptions-item label="节点名称">{{ selectedLog.nodeName }}</el-descriptions-item>
          <el-descriptions-item label="执行时间">{{ selectedLog.executeTime }}</el-descriptions-item>
          <el-descriptions-item label="执行状态">{{ getStatusName(selectedLog.status) }}</el-descriptions-item>
          <el-descriptions-item label="执行结果">
            <pre>{{ selectedLog.executeResult }}</pre>
          </el-descriptions-item>
          <el-descriptions-item label="失败原因" v-if="selectedLog.errorMsg">
            <pre>{{ selectedLog.errorMsg }}</pre>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Download, RefreshRight } from '@element-plus/icons-vue'

// 筛选条件
const filterForm = reactive({
  logId: '',
  instanceId: '',
  templateName: '',
  nodeName: '',
  status: ''
})

// 日志列表数据
const logs = ref([
  {
    id: 'LOG_20251208_001',
    instanceId: 'INSTANCE_20251208_001',
    templateName: '请假流程',
    nodeName: '开始节点',
    executeTime: '2025-12-08 10:00:00',
    status: 'success',
    executeResult: '流程启动成功',
    errorMsg: ''
  },
  {
    id: 'LOG_20251208_002',
    instanceId: 'INSTANCE_20251208_001',
    templateName: '请假流程',
    nodeName: '部门经理审批',
    executeTime: '2025-12-08 10:15:00',
    status: 'success',
    executeResult: '部门经理已审批通过',
    errorMsg: ''
  },
  {
    id: 'LOG_20251208_003',
    instanceId: 'INSTANCE_20251208_001',
    templateName: '请假流程',
    nodeName: '总经理审批',
    executeTime: '2025-12-08 11:30:00',
    status: 'success',
    executeResult: '总经理已审批通过',
    errorMsg: ''
  },
  {
    id: 'LOG_20251208_004',
    instanceId: 'INSTANCE_20251208_001',
    templateName: '请假流程',
    nodeName: '结束节点',
    executeTime: '2025-12-08 11:35:00',
    status: 'success',
    executeResult: '流程执行完成',
    errorMsg: ''
  },
  {
    id: 'LOG_20251208_005',
    instanceId: 'INSTANCE_20251208_002',
    templateName: '采购流程',
    nodeName: '财务审批',
    executeTime: '2025-12-08 09:45:00',
    status: 'failed',
    executeResult: '',
    errorMsg: '审批人不存在'
  }
])

// 分页数据
const currentPage = ref(1)
const pageSize = ref(10)
const totalLogs = ref(logs.value.length)

// 执行详情弹窗
const detailDialogVisible = ref(false)
const selectedLog = ref(null)

// 状态映射
const getStatusName = (status) => {
  const statusMap = {
    success: '成功',
    failed: '失败'
  }
  return statusMap[status] || status
}

// 状态标签样式
const getStatusTagType = (status) => {
  const statusMap = {
    success: 'success',
    failed: 'danger'
  }
  return statusMap[status] || 'info'
}

// 搜索
const handleSearch = () => {
  ElMessage.info('搜索功能开发中')
}

// 重置筛选条件
const handleResetFilter = () => {
  filterForm.logId = ''
  filterForm.instanceId = ''
  filterForm.templateName = ''
  filterForm.nodeName = ''
  filterForm.status = ''
}

// 导出
const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

// 行点击
const handleRowClick = (row) => {
  selectedLog.value = row
  detailDialogVisible.value = true
}

// 分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

// 页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
}

// 重试执行
const handleRetry = (row) => {
  row.status = 'success'
  row.errorMsg = ''
  row.executeResult = '重试执行成功'
  row.executeTime = new Date().toLocaleString('zh-CN')
  ElMessage.success(`已重试执行节点：${row.nodeName}`)
}
</script>

<style scoped>
.execution-logs-container {
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

.filter-section {
  display: flex;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.filter-input {
  width: 200px;
}

.filter-select {
  width: 150px;
}

.logs-card {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.execute-result, .error-msg {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  width: 100%;
  display: inline-block;
}

.detail-content pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
}
</style>