<template>
  <div class="completed-tasks-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>已办任务</span>
        </div>
      </template>

      <!-- 搜索和筛选区域 -->
      <div class="search-filter-section">
        <el-form :inline="true" :model="searchForm" class="demo-form-inline">
          <el-form-item label="任务名称">
            <el-input v-model="searchForm.taskName" placeholder="请输入任务名称" clearable></el-input>
          </el-form-item>
          <el-form-item label="流程名称">
            <el-input v-model="searchForm.processName" placeholder="请输入流程名称" clearable></el-input>
          </el-form-item>
          <el-form-item label="开始日期">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
            ></el-date-picker>
          </el-form-item>
          <el-form-item label="处理人">
            <el-select v-model="searchForm.assignee" placeholder="请选择处理人" clearable>
              <el-option
                v-for="user in users"
                :key="user.username"
                :label="user.fullName || user.username"
                :value="user.username"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
          </el-form-item>
          <el-form-item>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 任务列表 -->
      <div class="task-list-section">
        <el-table
          v-loading="loading"
          :data="completedTasks"
          style="width: 100%"
          @sort-change="handleSort"
        >
          <el-table-column prop="id" label="任务ID" width="180" sortable>
            <template #default="{ row }">
              <el-tooltip content="点击查看详情" placement="top">
                <span class="task-id">{{ row.id }}</span>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="任务名称" min-width="180" sortable>
            <template #default="{ row }">
              <el-link type="primary" @click="viewTaskDetail(row.id)">
                {{ row.name }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column prop="processDefinitionName" label="流程名称" min-width="180">
            <template #default="{ row }">
              {{ row.processDefinitionName || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="assigneeName" label="处理人" width="120">
            <template #default="{ row }">
              {{ row.assigneeName || row.assignee || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180" sortable>
            <template #default="{ row }">
              {{ formatDate(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="claimTime" label="认领时间" width="180" sortable>
            <template #default="{ row }">
              {{ row.claimTime ? formatDate(row.claimTime) : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="endTime" label="完成时间" width="180" sortable>
            <template #default="{ row }">
              {{ formatDate(row.endTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="duration" label="处理时长" width="120">
            <template #default="{ row }">
              {{ formatDuration(row.duration) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button
                size="small"
                type="primary"
                @click="viewTaskDetail(row.id)"
              >
                详情
              </el-button>
              <el-button
                size="small"
                v-if="row.processInstanceId"
                @click="viewProcessInstance(row.processInstanceId)"
              >
                流程
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import TaskService from '../services/TaskService'
import UserService from '../services/UserService'

export default {
  name: 'CompletedTasksView',
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const completedTasks = ref([])
    const users = ref([])
    
    // 搜索表单
    const searchForm = reactive({
      taskName: '',
      processName: '',
      assignee: '',
      startTime: '',
      endTime: ''
    })
    
    const dateRange = ref([])
    
    // 分页信息
    const pagination = reactive({
      currentPage: 1,
      pageSize: 10,
      total: 0
    })
    
    // 排序信息
    const sortInfo = reactive({
      prop: '',
      order: ''
    })
    
    // 加载用户数据
    const loadUsers = async () => {
      try {
        const userList = await UserService.getUsers()
        users.value = userList;
      } catch (error) {
        console.error('加载用户数据失败:', error)
        ElMessage.error('加载用户数据失败: ' + (error.response?.data?.message || error.message))
      }
    }
    
    // 加载已办任务数据
    const loadCompletedTasks = async () => {
      loading.value = true
      try {
        // 构建查询参数
        const params = {
          page: pagination.currentPage,
          size: pagination.pageSize,
          taskName: searchForm.taskName,
          processName: searchForm.processName,
          assignee: searchForm.assignee,
          startTime: searchForm.startTime,
          endTime: searchForm.endTime,
          sort: sortInfo.prop,
          order: sortInfo.order
        }
        
        // 使用TaskService.getCompletedTasks方法获取已办任务
        const response = await TaskService.getCompletedTasks(params)
        
        // 验证响应格式
        if (!response || typeof response !== 'object') {
          console.error('已办任务响应格式错误')
          completedTasks.value = []
          pagination.total = 0
          return
        }
        
        // 安全地访问数据，处理不同的响应格式
        if (Array.isArray(response)) {
          // 直接返回数组的情况
          completedTasks.value = response
          pagination.total = response.length
        } else if (response.data && Array.isArray(response.data)) {
          // 格式: { data: [], total: 0 }
          completedTasks.value = response.data
          pagination.total = response.total || response.data.length
        } else if (response.data && response.data.data && Array.isArray(response.data.data)) {
          // 格式: { data: { data: [], total: 0 } }
          completedTasks.value = response.data.data
          pagination.total = response.data.total || response.data.data.length
        } else {
          // 默认空数组
          completedTasks.value = []
          pagination.total = 0
        }
      } catch (error) {
        console.error('加载已办任务异常:', error)
        
        // 区分真正的错误和"任务不存在"的正常状态
        if (error.message && error.message.includes('任务不存在')) {
          // "任务不存在"是正常状态而非错误，不显示错误提示
          console.info('没有找到已办任务，这是正常状态')
        } else {
          // 只对真正的错误（网络错误、服务器错误等）显示错误提示
          ElMessage.error('加载已办任务失败，请稍后重试')
        }
        
        // 确保设置默认值，避免界面崩溃
        completedTasks.value = []
        pagination.total = 0
      } finally {
        loading.value = false
      }
    }
    
    // 处理搜索
    const handleSearch = () => {
      // 处理日期范围
      if (dateRange.value && dateRange.value.length === 2) {
        searchForm.startTime = dateRange.value[0]
        searchForm.endTime = dateRange.value[1]
      } else {
        searchForm.startTime = ''
        searchForm.endTime = ''
      }
      
      pagination.currentPage = 1
      loadCompletedTasks()
    }
    
    // 重置搜索
    const resetSearch = () => {
      searchForm.taskName = ''
      searchForm.processName = ''
      searchForm.assignee = ''
      dateRange.value = []
      searchForm.startTime = ''
      searchForm.endTime = ''
      
      sortInfo.prop = ''
      sortInfo.order = ''
      pagination.currentPage = 1
      
      loadCompletedTasks()
    }
    
    // 处理排序
    const handleSort = ({ prop, order }) => {
      sortInfo.prop = prop
      sortInfo.order = order === 'ascending' ? 'asc' : 'desc'
      loadCompletedTasks()
    }
    
    // 分页处理
    const handleSizeChange = (size) => {
      pagination.pageSize = size
      pagination.currentPage = 1
      loadCompletedTasks()
    }
    
    const handleCurrentChange = (current) => {
      pagination.currentPage = current
      loadCompletedTasks()
    }
    
    // 查看任务详情
    const viewTaskDetail = (taskId) => {
      router.push(`/tasks/${taskId}?completed=true`)
    }
    
    // 查看流程实例
    const viewProcessInstance = (processInstanceId) => {
      router.push(`/process-history?processInstanceId=${processInstanceId}`)
    }
    
    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return ''
      
      try {
        const date = new Date(dateString)
        const year = date.getFullYear()
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')
        const hours = String(date.getHours()).padStart(2, '0')
        const minutes = String(date.getMinutes()).padStart(2, '0')
        
        return `${year}-${month}-${day} ${hours}:${minutes}`
      } catch (error) {
        console.error('日期格式化错误:', error)
        return dateString
      }
    }
    
    // 格式化时长
    const formatDuration = (milliseconds) => {
      if (!milliseconds) return '-' 
      
      const seconds = Math.floor(milliseconds / 1000)
      const minutes = Math.floor(seconds / 60)
      const hours = Math.floor(minutes / 60)
      const days = Math.floor(hours / 24)
      
      if (days > 0) {
        return `${days}天${hours % 24}小时`
      } else if (hours > 0) {
        return `${hours}小时${minutes % 60}分钟`
      } else if (minutes > 0) {
        return `${minutes}分钟`
      } else {
        return `${seconds}秒`
      }
    }
    
    // 获取状态标签类型
    const getStatusTagType = (status) => {
      const statusMap = {
        'completed': 'success',
        'canceled': 'danger',
        'deleted': 'warning'
      }
      
      return statusMap[status] || 'info'
    }
    
    // 获取状态文本
    const getStatusText = (status) => {
      const statusMap = {
        'completed': '已完成',
        'canceled': '已取消',
        'deleted': '已删除'
      }
      
      return statusMap[status] || status
    }
    
    // 初始化
    onMounted(async () => {
      await loadUsers()
      loadCompletedTasks()
    })
    
    return {
      loading,
      completedTasks,
      searchForm,
      dateRange,
      users,
      pagination,
      handleSearch,
      resetSearch,
      handleSort,
      handleSizeChange,
      handleCurrentChange,
      viewTaskDetail,
      viewProcessInstance,
      formatDate,
      formatDuration,
      getStatusTagType,
      getStatusText
    }
  }
}
</script>

<style scoped>
.completed-tasks-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-filter-section {
  margin-bottom: 20px;
}

.task-list-section {
  margin-bottom: 20px;
}

.pagination-section {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.task-id {
  font-family: monospace;
  font-size: 12px;
  color: #606266;
  cursor: pointer;
}

.task-id:hover {
  color: #409eff;
  text-decoration: underline;
}
</style>