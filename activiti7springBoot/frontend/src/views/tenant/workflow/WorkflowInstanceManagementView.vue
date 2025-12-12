<template>
  <div class="workflow-instance-container">
    <h3 class="section-title">流程实例管理</h3>
    
    <!-- 操作按钮和筛选条件 -->
    <el-card shadow="hover" class="filter-card">
      <div class="filter-section">
        <div class="filter-left">
          <el-button type="primary" @click="handleCreateInstance">
            <el-icon><Plus /></el-icon> 创建流程实例
          </el-button>
        </div>
        
        <div class="filter-right">
          <el-input
            v-model="filterForm.instanceId"
            placeholder="实例ID"
            clearable
            class="filter-input"
            @keyup.enter="handleSearch"
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
            v-model="filterForm.status"
            placeholder="状态"
            class="filter-select"
            clearable
          >
            <el-option label="运行中" value="running" />
            <el-option label="已暂停" value="paused" />
            <el-option label="已完成" value="completed" />
            <el-option label="已终止" value="terminated" />
          </el-select>
          
          <el-date-picker
            v-model="filterForm.timeRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            class="filter-date"
          />
          
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          
          <el-button @click="handleResetFilter">重置</el-button>
        </div>
      </div>
    </el-card>
    
    <!-- 实例列表 -->
    <el-card shadow="hover" class="instance-card">
      <el-table
        :data="instances"
        border
        style="width: 100%"
        @row-click="handleRowClick"
        row-key="id"
      >
        <el-table-column prop="id" label="实例ID" width="180" />
        <el-table-column prop="templateName" label="模板名称" width="150" />
        <el-table-column prop="startTime" label="启动时间" width="180" />
        <el-table-column prop="currentNode" label="当前节点" width="150" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getInstanceStatusTagType(scope.row.status)" size="small">
              {{ getInstanceStatusName(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="执行时长" width="120" />
        <el-table-column prop="action" label="操作" width="200">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleViewDetail(scope.row)">
              <el-icon><View /></el-icon> 查看详情
            </el-button>
            <el-button
              type="warning"
              size="small"
              @click="handlePause(scope.row)"
              :disabled="scope.row.status !== 'running'"
            >
              <el-icon><VideoPause /></el-icon> 暂停
            </el-button>
            <el-button
              type="success"
              size="small"
              @click="handleResume(scope.row)"
              :disabled="scope.row.status !== 'paused'"
            >
              <el-icon><VideoPlay /></el-icon> 恢复
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleTerminate(scope.row)"
              :disabled="scope.row.status === 'completed' || scope.row.status === 'terminated'"
            >
              <el-icon><CircleClose /></el-icon> 终止
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
          :total="totalInstances"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Plus, Search, View, VideoPause, VideoPlay, CircleClose 
} from '@element-plus/icons-vue'

// 筛选条件
const filterForm = reactive({
  instanceId: '',
  templateName: '',
  status: '',
  timeRange: []
})

// 实例列表数据
const instances = ref([
  {
    id: 'INSTANCE_20251208_001',
    templateName: '请假流程',
    startTime: '2025-12-08 10:00:00',
    currentNode: '部门经理审批',
    status: 'running',
    duration: '1小时30分钟'
  },
  {
    id: 'INSTANCE_20251208_002',
    templateName: '采购流程',
    startTime: '2025-12-08 09:00:00',
    currentNode: '财务审批',
    status: 'running',
    duration: '2小时'
  },
  {
    id: 'INSTANCE_20251207_001',
    templateName: '报销流程',
    startTime: '2025-12-07 16:00:00',
    currentNode: '',
    status: 'completed',
    duration: '3小时20分钟'
  },
  {
    id: 'INSTANCE_20251207_002',
    templateName: '请假流程',
    startTime: '2025-12-07 14:00:00',
    currentNode: '总经理审批',
    status: 'paused',
    duration: '1小时45分钟'
  },
  {
    id: 'INSTANCE_20251206_001',
    templateName: '采购流程',
    startTime: '2025-12-06 11:00:00',
    currentNode: '采购部门审核',
    status: 'terminated',
    duration: '45分钟'
  }
])

// 分页数据
const currentPage = ref(1)
const pageSize = ref(10)
const totalInstances = ref(instances.value.length)

// 实例状态映射
const getInstanceStatusName = (status) => {
  const statusMap = {
    running: '运行中',
    paused: '已暂停',
    completed: '已完成',
    terminated: '已终止'
  }
  return statusMap[status] || status
}

// 实例状态标签样式
const getInstanceStatusTagType = (status) => {
  const statusMap = {
    running: 'success',
    paused: 'warning',
    completed: 'info',
    terminated: 'danger'
  }
  return statusMap[status] || 'info'
}

// 搜索
const handleSearch = () => {
  ElMessage.info('搜索功能开发中')
}

// 重置筛选条件
const handleResetFilter = () => {
  filterForm.instanceId = ''
  filterForm.templateName = ''
  filterForm.status = ''
  filterForm.timeRange = []
}

// 行点击
const handleRowClick = (row) => {
  // 可以在这里处理行点击事件
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

// 创建流程实例
const handleCreateInstance = () => {
  ElMessage.info('创建流程实例功能开发中')
}

// 查看详情
const handleViewDetail = (row) => {
  ElMessage.info('查看详情功能开发中')
}

// 暂停
const handlePause = (row) => {
  row.status = 'paused'
  ElMessage.success(`已暂停流程实例：${row.id}`)
}

// 恢复
const handleResume = (row) => {
  row.status = 'running'
  ElMessage.success(`已恢复流程实例：${row.id}`)
}

// 终止
const handleTerminate = (row) => {
  row.status = 'terminated'
  ElMessage.success(`已终止流程实例：${row.id}`)
}
</script>

<style scoped>
.workflow-instance-container {
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
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.filter-left {
  display: flex;
  gap: 10px;
}

.filter-right {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.filter-input {
  width: 200px;
}

.filter-select {
  width: 150px;
}

.filter-date {
  width: 350px;
}

.instance-card {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>