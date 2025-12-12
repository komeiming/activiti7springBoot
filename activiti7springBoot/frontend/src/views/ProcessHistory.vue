<template>
  <div class="process-history">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>流程历史</span>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-section">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="searchForm.processInstanceName"
              placeholder="流程实例名称"
              clearable
              @keyup.enter="handleSearch"
            ></el-input>
          </el-col>
          <el-col :span="6">
            <el-select v-model="searchForm.processDefinitionKey" placeholder="流程定义" clearable>
              <el-option
                v-for="def in processDefinitions"
                :key="def.key"
                :label="def.name"
                :value="def.key"
              ></el-option>
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-select v-model="searchForm.status" placeholder="流程状态" clearable>
              <el-option label="进行中" value="running"></el-option>
              <el-option label="已完成" value="completed"></el-option>
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              @change="handleSearch"
            ></el-date-picker>
          </el-col>
          <el-col :span="6">
            <el-input
              v-model="searchForm.startedBy"
              placeholder="发起人"
              clearable
              @keyup.enter="handleSearch"
            ></el-input>
          </el-col>
          <el-col :span="6">
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-col>
        </el-row>
      </div>

      <!-- 流程实例列表 -->
      <div class="process-instance-list">
        <el-table
          v-loading="loading"
          :data="processInstances"
          style="width: 100%"
          border
          @row-click="handleRowClick"
        >
          <el-table-column prop="id" label="实例ID" width="220" show-overflow-tooltip></el-table-column>
          <el-table-column prop="name" label="流程名称" width="180"></el-table-column>
          <el-table-column prop="processDefinitionName" label="流程定义" width="180"></el-table-column>
          <el-table-column prop="startedBy" label="发起人" width="120">
            <template #default="scope">
              {{ getUserName(scope.row.startedBy) }}
            </template>
          </el-table-column>
          <el-table-column prop="startTime" label="开始时间" width="180">
            <template #default="scope">
              {{ formatDate(scope.row.startTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="endTime" label="结束时间" width="180">
            <template #default="scope">
              {{ scope.row.endTime ? formatDate(scope.row.endTime) : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="duration" label="耗时" width="100">
            <template #default="scope">
              {{ scope.row.duration ? formatDuration(scope.row.duration) : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'running' ? 'primary' : 'success'">
                {{ scope.row.status === 'running' ? '进行中' : '已完成' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="scope">
              <el-button
                size="small"
                @click.stop="viewProcessDetails(scope.row)"
              >
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
            layout="total, sizes, prev, pager, next, jumper"
            :total="pagination.total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          ></el-pagination>
        </div>
      </div>
    </el-card>

    <!-- 流程详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="`流程详情：${selectedProcessInstance?.name || ''}`"
      width="900px"
    >
      <!-- 流程信息 -->
      <div class="process-info" v-if="selectedProcessInstance">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="流程实例ID">{{ selectedProcessInstance.id }}</el-descriptions-item>
          <el-descriptions-item label="流程名称">{{ selectedProcessInstance.name }}</el-descriptions-item>
          <el-descriptions-item label="流程定义ID">{{ selectedProcessInstance.processDefinitionId }}</el-descriptions-item>
          <el-descriptions-item label="流程定义名称">{{ selectedProcessInstance.processDefinitionName }}</el-descriptions-item>
          <el-descriptions-item label="发起人">{{ getUserName(selectedProcessInstance.startedBy) }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ formatDate(selectedProcessInstance.startTime) }}</el-descriptions-item>
          <el-descriptions-item label="结束时间" v-if="selectedProcessInstance.endTime">{{ formatDate(selectedProcessInstance.endTime) }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ selectedProcessInstance.status === 'running' ? '进行中' : '已完成' }}</el-descriptions-item>
          <el-descriptions-item label="耗时" v-if="selectedProcessInstance.duration">{{ formatDuration(selectedProcessInstance.duration) }}</el-descriptions-item>
          <el-descriptions-item label="业务键" v-if="selectedProcessInstance.businessKey">{{ selectedProcessInstance.businessKey }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 流程变量 -->
      <div class="process-variables" v-if="processVariables.length > 0">
        <h4 style="margin-top: 20px; margin-bottom: 10px;">流程变量</h4>
        <el-table :data="processVariables" size="small" border style="width: 100%">
          <el-table-column prop="name" label="变量名称" width="150"></el-table-column>
          <el-table-column prop="value" label="变量值" show-overflow-tooltip></el-table-column>
          <el-table-column prop="type" label="变量类型" width="100"></el-table-column>
        </el-table>
      </div>

      <!-- 流程图 -->
      <div class="process-diagram" v-if="selectedProcessInstance">
        <h4 style="margin-top: 20px; margin-bottom: 10px;">流程图</h4>
        <div class="diagram-container">
          <img 
            :src="processDiagramUrl" 
            alt="流程图" 
            class="process-image" 
            @error="handleImageError"
          />
          <div v-if="!processDiagramUrl" class="no-diagram">
            无法加载流程图
          </div>
        </div>
      </div>

      <!-- 流程活动历史 -->
      <div class="activity-history">
        <h4 style="margin-top: 20px; margin-bottom: 10px;">流程活动历史</h4>
        <el-timeline>
          <el-timeline-item
            v-for="(activity, index) in activityHistory"
            :key="activity.id"
            :timestamp="formatDate(activity.time)"
            :type="getActivityType(activity.type)"
          >
            <el-card>
              <h5>{{ activity.activityName }}</h5>
              <p v-if="activity.assignee" class="assignee">处理人：{{ getUserName(activity.assignee) }}</p>
              <p v-if="activity.comment" class="comment">备注：{{ activity.comment }}</p>
              <p v-if="activity.action" class="action">操作：{{ activity.action === 'approve' ? '同意' : '拒绝' }}</p>
              <p v-if="activity.startTime && activity.endTime" class="duration">
                耗时：{{ formatDuration(activity.duration || (new Date(activity.endTime) - new Date(activity.startTime))) }}
              </p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>

      <!-- 任务列表 -->
      <div class="tasks-container" v-if="processTasks.length > 0">
        <h4 style="margin-top: 20px; margin-bottom: 10px;">任务列表</h4>
        <el-table :data="processTasks" size="small" border style="width: 100%">
          <el-table-column prop="id" label="任务ID" width="180" show-overflow-tooltip></el-table-column>
          <el-table-column prop="name" label="任务名称" width="180"></el-table-column>
          <el-table-column prop="assignee" label="处理人" width="100">
            <template #default="scope">
              {{ getUserName(scope.row.assignee) }}
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="160">
            <template #default="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="claimTime" label="认领时间" width="160">
            <template #default="scope">
              {{ scope.row.claimTime ? formatDate(scope.row.claimTime) : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="endTime" label="完成时间" width="160">
            <template #default="scope">
              {{ scope.row.endTime ? formatDate(scope.row.endTime) : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'completed' ? 'success' : 'primary'">
                {{ scope.row.status === 'completed' ? '已完成' : '进行中' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="exportProcessHistory" v-if="selectedProcessInstance">
            导出历史
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import TaskService from '../services/TaskService'
import ProcessDefinitionService from '../services/ProcessDefinitionService'
import UserService from '../services/UserService'

export default {
  name: 'ProcessHistory',
  setup() {
    // 响应式数据
    const loading = ref(false)
    const detailDialogVisible = ref(false)
    const processInstances = ref([])
    const processDefinitions = ref([])
    const selectedProcessInstance = ref(null)
    const processVariables = ref([])
    const activityHistory = ref([])
    const processTasks = ref([])
    const processDiagramUrl = ref('')
    const allUsers = ref([])
    
    // 搜索表单
    const searchForm = reactive({
      processInstanceName: '',
      processDefinitionKey: '',
      status: '',
      startedBy: '',
      startTime: '',
      endTime: ''
    })
    
    const dateRange = ref([])
    
    // 分页
    const pagination = reactive({
      currentPage: 1,
      pageSize: 10,
      total: 0
    })
    
    // 加载用户列表
    const loadUsers = async () => {
      try {
        const response = await UserService.getUserList()
        
        // 多种响应格式兼容处理 - 由于axios响应拦截器已经处理了response.data，所以直接使用response
        if (Array.isArray(response)) {
          // 直接返回数组的格式
          allUsers.value = response
        } else if (typeof response === 'object' && response) {
          // 响应是对象
          if (response.code === 200 || response.success !== false) {
            // 标准响应格式: {code: 200, data: [...]} 或 {success: true, data: [...]} 
            allUsers.value = Array.isArray(response.data) ? response.data : []
          } else if (Array.isArray(response.data)) {
            // response.data是数组
            allUsers.value = response.data
          } else {
            // 其他情况，使用空数组
            allUsers.value = []
          }
        } else {
          // 响应不是数组也不是对象，使用空数组
          allUsers.value = []
        }
      } catch (error) {
        console.error('加载用户列表错误:', error)
        allUsers.value = []
      }
    }
    
    // 加载流程定义列表
    const loadProcessDefinitions = async () => {
      try {
        const response = await ProcessDefinitionService.getProcessDefinitions()
        if (response.data.code === 200) {
          processDefinitions.value = response.data.data || []
        }
      } catch (error) {
        console.error('加载流程定义错误:', error)
      }
    }
    
    // 获取用户名称
    const getUserName = (username) => {
      if (!username) return '系统'
      const user = allUsers.value.find(u => u.username === username)
      return user ? (user.fullName || user.username) : username
    }
    
    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    }
    
    // 格式化持续时间
    const formatDuration = (milliseconds) => {
      if (!milliseconds) return '0秒'
      
      const seconds = Math.floor(milliseconds / 1000)
      if (seconds < 60) {
        return `${seconds}秒`
      }
      
      const minutes = Math.floor(seconds / 60)
      const remainingSeconds = seconds % 60
      if (minutes < 60) {
        return `${minutes}分${remainingSeconds}秒`
      }
      
      const hours = Math.floor(minutes / 60)
      const remainingMinutes = minutes % 60
      if (hours < 24) {
        return `${hours}时${remainingMinutes}分`
      }
      
      const days = Math.floor(hours / 24)
      const remainingHours = hours % 24
      return `${days}天${remainingHours}时`
    }
    
    // 获取活动类型对应的标签类型
    const getActivityType = (type) => {
      switch (type) {
        case 'start':
          return 'primary'
        case 'complete':
          return 'success'
        case 'transition':
          return 'info'
        case 'cancel':
          return 'danger'
        default:
          return 'warning'
      }
    }
    
    // 加载流程实例列表
    const loadProcessInstances = async () => {
      loading.value = true
      try {
        // 设置日期范围
        if (dateRange.value && dateRange.value.length === 2) {
          searchForm.startTime = dateRange.value[0]
          searchForm.endTime = dateRange.value[1]
        } else {
          searchForm.startTime = ''
          searchForm.endTime = ''
        }
        
        // 准备请求参数
        const requestParams = {
          ...searchForm,
          page: pagination.currentPage,
          pageSize: pagination.pageSize
        }
        
        console.log('加载流程实例，参数:', requestParams);
        
        const response = await ProcessDefinitionService.getProcessInstances(requestParams)
        
        console.log('API响应:', response);
        
        // 响应拦截器已经处理了CommonResponse格式，直接使用response
        if (response) {
          processInstances.value = response.data || []
          pagination.total = response.total || 0
          console.log('加载到的流程实例数量:', processInstances.value.length)
        } else {
          console.error('API调用失败或返回不正确的数据结构:', response)
          ElMessage.error('加载流程实例失败: ' + (response?.message || '未知错误'))
          processInstances.value = []
          pagination.total = 0
        }
      } catch (error) {
        console.error('加载流程实例时出错:', error)
        ElMessage.error('加载流程实例失败: ' + (error.message || '网络错误'))
        processInstances.value = []
        pagination.total = 0
      } finally {
        loading.value = false
      }
    }
    
    // 搜索
    const handleSearch = () => {
      pagination.currentPage = 1
      loadProcessInstances()
    }
    
    // 重置搜索
    const resetSearch = () => {
      Object.keys(searchForm).forEach(key => {
        searchForm[key] = ''
      })
      dateRange.value = []
      pagination.currentPage = 1
      loadProcessInstances()
    }
    
    // 分页大小变化
    const handleSizeChange = (size) => {
      pagination.pageSize = size
      loadProcessInstances()
    }
    
    // 当前页变化
    const handleCurrentChange = (current) => {
      pagination.currentPage = current
      loadProcessInstances()
    }
    
    // 行点击
    const handleRowClick = (row) => {
      viewProcessDetails(row)
    }
    
    // 处理图片加载错误
    const handleImageError = () => {
      processDiagramUrl.value = ''
    }
    
    // 查看流程详情
    const viewProcessDetails = async (instance) => {
      selectedProcessInstance.value = instance
      processVariables.value = []
      activityHistory.value = []
      processTasks.value = []
      
      try {
        // 加载流程变量
        const variablesResponse = await ProcessDefinitionService.getProcessInstanceVariables(instance.id)
        // ProcessDefinitionService.getProcessInstanceVariables returns the variables object directly
        const variables = variablesResponse.data || variablesResponse || {}
        processVariables.value = Object.entries(variables).map(([name, value]) => ({
          name,
          value: typeof value === 'object' ? JSON.stringify(value) : String(value),
          type: typeof value
        }))
        
        // 加载活动历史
        const historyResponse = await TaskService.getProcessInstanceHistory(instance.id)
        // TaskService.getProcessInstanceHistory returns an array directly
        activityHistory.value = historyResponse || []
        
        // 暂时移除任务列表加载，因为没有对应的API方法
        processTasks.value = []
        
        // 获取流程图
        try {
          // 直接使用instance对象中的processDefinitionId属性作为流程定义ID
          const diagramUrl = ProcessDefinitionService.getProcessDiagramUrlByInstanceId(instance.processDefinitionId || instance.id);
          processDiagramUrl.value = `${diagramUrl}?_t=${Date.now()}`;
        } catch (err) {
          console.warn('获取流程图失败:', err);
          processDiagramUrl.value = '';
        }
        
        detailDialogVisible.value = true
      } catch (error) {
        console.error('加载流程详情错误:', error)
        ElMessage.error('加载流程详情失败')
      }
    }
    
    // 导出流程历史
    const exportProcessHistory = async () => {
      if (!selectedProcessInstance.value) return
      
      try {
        const response = await TaskService.exportProcessHistory(selectedProcessInstance.value.id)
        
        // 处理导出文件
        const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `${selectedProcessInstance.value.name}_流程历史.xlsx`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        
        ElMessage.success('导出成功')
      } catch (error) {
        console.error('导出流程历史错误:', error)
        ElMessage.error('导出失败')
      }
    }
    
    // 初始化
    onMounted(async () => {
      await loadUsers()
      await loadProcessDefinitions()
      await loadProcessInstances()
    })
    
    return {
      loading,
      detailDialogVisible,
      processInstances,
      processDefinitions,
      selectedProcessInstance,
      processVariables,
      activityHistory,
      processTasks,
      processDiagramUrl,
      searchForm,
      dateRange,
      pagination,
      handleSearch,
      resetSearch,
      handleSizeChange,
      handleCurrentChange,
      handleRowClick,
      viewProcessDetails,
      exportProcessHistory,
      handleImageError,
      getUserName,
      formatDate,
      formatDuration,
      getActivityType
    }
  }
}
</script>

<style scoped>
.process-history {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-section {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.process-instance-list {
  margin-top: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.process-info {
  margin-bottom: 20px;
}

.process-variables {
  margin-bottom: 20px;
}

.process-diagram {
  margin-bottom: 20px;
}

.diagram-container {
  overflow: auto;
  max-height: 500px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 10px;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

.process-image {
  max-width: 100%;
  max-height: 500px;
}

.no-diagram {
  color: #909399;
  font-size: 14px;
}

.activity-history {
  margin-top: 20px;
}

.tasks-container {
  margin-top: 20px;
}

.el-timeline-item__timestamp {
  font-size: 12px;
  color: #909399;
}

.assignee {
  color: #409eff;
  margin: 5px 0;
}

.comment {
  color: #606266;
  margin: 5px 0;
}

.action {
  color: #67c23a;
  margin: 5px 0;
}

.duration {
  color: #909399;
  margin: 5px 0;
  font-size: 12px;
}
</style>