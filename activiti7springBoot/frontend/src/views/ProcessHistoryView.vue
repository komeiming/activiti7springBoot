<template>
  <div class="process-history-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>流程历史</span>
          <el-button type="primary" @click="refreshList">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>

      <!-- 搜索和过滤区域 -->
      <div class="search-filter-section">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="流程类型">
            <el-select v-model="searchForm.processType" placeholder="请选择流程类型">
              <el-option label="全部" value=""></el-option>
              <el-option label="请假申请" value="leave-process"></el-option>
              <el-option label="入职申请" value="onboarding-process"></el-option>
              <el-option label="通用审批" value="general-approval"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="申请时间">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 240px"
            ></el-date-picker>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 流程历史表格 -->
      <div class="table-container">
        <el-table
          :data="processInstances"
          style="width: 100%"
          @row-click="handleRowClick"
          v-loading="loading"
        >
          <el-table-column prop="processInstanceId" label="流程ID" width="180"></el-table-column>
          <el-table-column prop="processKey" label="流程类型" width="120">
            <template #default="scope">
              <el-tag :type="getProcessTypeTagType(scope.row.processKey)">
                {{ getProcessTypeName(scope.row.processKey) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="流程名称"></el-table-column>
          <el-table-column prop="startUser" label="发起人" width="120"></el-table-column>
          <el-table-column prop="startTime" label="发起时间" width="180" sortable>
            <template #default="scope">
              {{ formatDateTime(scope.row.startTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="endTime" label="结束时间" width="180" sortable>
            <template #default="scope">
              {{ formatDateTime(scope.row.endTime) || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusTagType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="scope">
              <el-button type="text" @click.stop="viewProcessDetails(scope.row)">
                <el-icon><View /></el-icon>
                详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页控件 -->
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

    <!-- 流程详情抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      title="流程详情"
      size="80%"
      :before-close="handleDrawerClose"
    >
      <div class="process-detail-container">
        <!-- 基本信息 -->
        <el-card class="detail-card">
          <template #header>
            <div class="card-header">
              <span>基本信息</span>
            </div>
          </template>
          <el-descriptions border column="2" :column="{ xs: 1, sm: 2 }">
            <el-descriptions-item label="流程ID">{{ selectedProcess?.processInstanceId }}</el-descriptions-item>
            <el-descriptions-item label="流程类型">{{ getProcessTypeName(selectedProcess?.processKey) }}</el-descriptions-item>
            <el-descriptions-item label="流程名称">{{ selectedProcess?.name }}</el-descriptions-item>
            <el-descriptions-item label="发起人">{{ selectedProcess?.startUser }}</el-descriptions-item>
            <el-descriptions-item label="发起时间">{{ formatDateTime(selectedProcess?.startTime) }}</el-descriptions-item>
            <el-descriptions-item label="结束时间">{{ formatDateTime(selectedProcess?.endTime) || '-' }}</el-descriptions-item>
            <el-descriptions-item label="当前状态">{{ getStatusText(selectedProcess?.status) }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 表单数据 -->
        <el-card class="detail-card" v-if="processVariables.length > 0">
          <template #header>
            <div class="card-header">
              <span>表单数据</span>
            </div>
          </template>
          <el-descriptions border column="1">
            <el-descriptions-item v-for="(value, key) in processVariables" :key="key" :label="formatVariableName(key)">
              {{ formatVariableValue(value) }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 流程历史活动 -->
        <el-card class="detail-card">
          <template #header>
            <div class="card-header">
              <span>流程历史</span>
            </div>
          </template>
          <div class="timeline-container">
            <el-timeline>
              <el-timeline-item
                v-for="(activity, index) in processActivities"
                :key="index"
                :timestamp="formatDateTime(activity.startTime)"
                :type="getActivityType(activity.activityType)"
                :size="activity.activityType === 'endEvent' ? 'large' : 'normal'"
                :icon="getActivityIcon(activity.activityType)"
              >
                <div class="activity-content">
                  <h4>{{ activity.activityName }}</h4>
                  <p v-if="activity.assignee">办理人：{{ activity.assignee }}</p>
                  <p v-if="activity.endTime">结束时间：{{ formatDateTime(activity.endTime) }}</p>
                  <p v-if="activity.duration">耗时：{{ formatDuration(activity.duration) }}</p>
                  <p v-if="activity.description" class="activity-description">{{ activity.description }}</p>
                </div>
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-card>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, View } from '@element-plus/icons-vue'
import ProcessDefinitionService from '../services/ProcessDefinitionService'
import UserService from '../services/UserService'

export default {
  name: 'ProcessHistoryView',
  components: {
    Refresh,
    View
  },
  setup() {
    // 响应式数据
    const loading = ref(false)
    const processInstances = ref([])
    const selectedProcess = ref(null)
    const drawerVisible = ref(false)
    const processVariables = ref({})
    const processActivities = ref([])
    const currentUser = ref({})
    
    // 分页数据
    const pagination = reactive({
      currentPage: 1,
      pageSize: 10,
      total: 0
    })
    
    // 搜索表单
    const searchForm = reactive({
      processType: '',
      dateRange: null
    })
    
    // 获取当前用户信息
    const getUserInfo = () => {
      try {
        // getCurrentUser是同步方法，不需要await
        const user = UserService.getCurrentUser()
        console.log('获取当前用户信息:', user)
        currentUser.value = user || { username: 'admin' } // 提供默认值，防止user为null
      } catch (error) {
        ElMessage.error('获取用户信息失败')
        console.error('获取用户信息失败:', error)
        currentUser.value = { username: 'admin' } // 错误时提供默认值
      }
    }
    
    // 加载流程实例列表
    const loadProcessInstances = async () => {
      loading.value = true
      try {
        // 构建查询参数
        const params = {
          page: pagination.currentPage,
          size: pagination.pageSize,
          processKey: searchForm.processType || undefined,
          userId: currentUser.value.username || undefined
        }
        
        // 添加时间范围过滤
        if (searchForm.dateRange && searchForm.dateRange.length === 2) {
          params.startTimeFrom = searchForm.dateRange[0].toISOString()
          params.startTimeTo = searchForm.dateRange[1].toISOString()
        }
        
        const response = await ProcessDefinitionService.getProcessInstances(params)
        
        console.log('获取流程实例API响应:', response)
        
        // 处理不同的返回格式
        let processData = []
        let totalCount = 0
        
        // 统一处理不同的响应格式
        if (response && typeof response === 'object') {
          // 检查response.data是否存在，这是axios拦截器处理后的CommonResponse格式
          const responseData = response.data || response
          
          // 提取processData和totalCount
          if (responseData.data && Array.isArray(responseData.data)) {
            // 标准格式: { data: [...], total: 0 }
            processData = responseData.data
            totalCount = responseData.total || processData.length
          } else if (Array.isArray(responseData)) {
            // 响应直接是数组格式
            processData = responseData
            totalCount = responseData.length
          } else {
            // 其他格式，返回空数据
            console.warn('API返回数据格式不符合预期:', response)
            processData = []
            totalCount = 0
          }
        } else if (Array.isArray(response)) {
          // 响应直接是数组
          processData = response
          totalCount = response.length
        } else {
          // 无效响应
          console.error('API返回数据类型错误，期望对象或数组，实际为', typeof response)
          processData = []
          totalCount = 0
        }
        
        console.log('处理后的数据:', {
          processDataLength: processData.length,
          totalCount,
          firstData: processData[0] || '没有数据'
        })
        
        // 映射后端返回的字段到前端期望的字段
        const mappedProcessData = processData.map(process => {
          return {
            // 映射流程ID
            processInstanceId: process.id || process.processInstanceId || '',
            // 映射流程类型
            processKey: process.processDefinitionKey || process.processKey || '',
            // 映射流程名称
            name: process.processDefinitionName || process.name || '',
            // 映射发起人
            startUser: process.startUserId || process.startUser || '',
            // 映射发起时间
            startTime: process.startTime || '',
            // 映射结束时间
            endTime: process.endTime || '',
            // 映射状态
            status: process.isActive !== undefined ? (process.isActive ? 'active' : 'completed') : process.status || '',
            // 保留原始数据，以便后续使用
            originalData: process
          }
        })
        
        processInstances.value = mappedProcessData
        pagination.total = totalCount
      } catch (error) {
        ElMessage.error('获取流程实例列表失败')
        console.error('获取流程实例列表失败:', error)
      } finally {
        loading.value = false
      }
    }
    
    // 查看流程详情
    const viewProcessDetails = async (process) => {
      selectedProcess.value = process
      drawerVisible.value = true
      
      try {
        // 加载流程变量
        const variablesResponse = await ProcessDefinitionService.getProcessInstanceVariables(process.processInstanceId)
        processVariables.value = variablesResponse.data || {}
        
        // 加载流程历史活动
        const activitiesResponse = await ProcessDefinitionService.getProcessInstanceHistory(process.processInstanceId)
        // 处理不同的返回格式
        processActivities.value = []
        
        if (activitiesResponse && activitiesResponse.data && Array.isArray(activitiesResponse.data)) {
          // 格式1: { data: [] }
          processActivities.value = activitiesResponse.data
        } else if (activitiesResponse && Array.isArray(activitiesResponse)) {
          // 格式2: []
          processActivities.value = activitiesResponse
        } else if (activitiesResponse && activitiesResponse.data && activitiesResponse.data.data && Array.isArray(activitiesResponse.data.data)) {
          // 格式3: { data: { data: [] } }
          processActivities.value = activitiesResponse.data.data
        }
      } catch (error) {
        ElMessage.error('获取流程详情失败')
        console.error('获取流程详情失败:', error)
      }
    }
    
    // 搜索处理
    const handleSearch = () => {
      pagination.currentPage = 1
      loadProcessInstances()
    }
    
    // 重置搜索
    const resetSearch = () => {
      Object.assign(searchForm, {
        processType: '',
        dateRange: null
      })
      pagination.currentPage = 1
      loadProcessInstances()
    }
    
    // 刷新列表
    const refreshList = () => {
      loadProcessInstances()
    }
    
    // 分页处理
    const handleSizeChange = (size) => {
      pagination.pageSize = size
      loadProcessInstances()
    }
    
    const handleCurrentChange = (current) => {
      pagination.currentPage = current
      loadProcessInstances()
    }
    
    // 关闭抽屉
    const handleDrawerClose = () => {
      drawerVisible.value = false
      // 清空数据
      selectedProcess.value = null
      processVariables.value = {}
      processActivities.value = []
    }
    
    // 点击行处理
    const handleRowClick = (row) => {
      viewProcessDetails(row)
    }
    
    // 格式化日期时间
    const formatDateTime = (dateString) => {
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
      if (!milliseconds) return ''
      
      const seconds = Math.floor(milliseconds / 1000)
      const minutes = Math.floor(seconds / 60)
      const hours = Math.floor(minutes / 60)
      const days = Math.floor(hours / 24)
      
      if (days > 0) {
        return `${days}天${hours % 24}小时`
      } else if (hours > 0) {
        return `${hours}小时${minutes % 60}分钟`
      } else if (minutes > 0) {
        return `${minutes}分钟${seconds % 60}秒`
      } else {
        return `${seconds}秒`
      }
    }
    
    // 获取流程类型标签类型
    const getProcessTypeTagType = (processKey) => {
      const typeMap = {
        'leave-process': 'primary',
        'onboarding-process': 'success',
        'general-approval': 'warning'
      }
      return typeMap[processKey] || 'info'
    }
    
    // 获取流程类型名称
    const getProcessTypeName = (processKey) => {
      const nameMap = {
        'leave-process': '请假申请',
        'onboarding-process': '入职申请',
        'general-approval': '通用审批'
      }
      return nameMap[processKey] || processKey
    }
    
    // 获取状态文本
    const getStatusText = (status) => {
      const statusMap = {
        'active': '运行中',
        'completed': '已完成',
        'suspended': '已挂起',
        'terminated': '已终止'
      }
      return statusMap[status] || status
    }
    
    // 获取状态标签类型
    const getStatusTagType = (status) => {
      const typeMap = {
        'active': 'primary',
        'completed': 'success',
        'suspended': 'warning',
        'terminated': 'danger'
      }
      return typeMap[status] || 'info'
    }
    
    // 获取活动类型
    const getActivityType = (activityType) => {
      const typeMap = {
        'startEvent': 'primary',
        'endEvent': 'success',
        'userTask': 'info',
        'exclusiveGateway': 'warning',
        'parallelGateway': 'info'
      }
      return typeMap[activityType] || 'info'
    }
    
    // 获取活动图标
    const getActivityIcon = (activityType) => {
      const iconMap = {
        'startEvent': 'el-icon-circle-check',
        'endEvent': 'el-icon-circle-close',
        'userTask': 'el-icon-user',
        'exclusiveGateway': 'el-icon-split',
        'parallelGateway': 'el-icon-s-operation'
      }
      return iconMap[activityType] || ''
    }
    
    // 格式化变量名称
    const formatVariableName = (key) => {
      const nameMap = {
        'applicantName': '申请人',
        'department': '部门',
        'leaveType': '请假类型',
        'startTime': '开始时间',
        'endTime': '结束时间',
        'days': '请假天数',
        'reason': '申请原因',
        'employeeName': '员工姓名',
        'position': '职位',
        'hireDate': '入职日期',
        'contact': '联系方式',
        'title': '标题',
        'content': '内容',
        'priority': '优先级'
      }
      return nameMap[key] || key
    }
    
    // 格式化变量值
    const formatVariableValue = (value) => {
      if (typeof value === 'boolean') {
        return value ? '是' : '否'
      } else if (value instanceof Date || (typeof value === 'string' && value.includes('T'))) {
        return formatDateTime(value)
      } else if (typeof value === 'object' && value !== null) {
        return JSON.stringify(value)
      } else {
        // 处理一些特殊值的映射
        const valueMap = {
          'annual': '年假',
          'sick': '病假',
          'personal': '事假',
          'marriage': '婚假',
          'maternity': '产假',
          'other': '其他',
          'normal': '普通',
          'urgent': '紧急',
          'very_urgent': '非常紧急'
        }
        return valueMap[value] || value
      }
    }
    
    // 组件挂载时初始化
    onMounted(async () => {
      await getUserInfo()
      await loadProcessInstances()
    })
    
    return {
      loading,
      processInstances,
      selectedProcess,
      drawerVisible,
      processVariables,
      processActivities,
      pagination,
      searchForm,
      handleSearch,
      resetSearch,
      refreshList,
      handleSizeChange,
      handleCurrentChange,
      handleDrawerClose,
      handleRowClick,
      viewProcessDetails,
      formatDateTime,
      formatDuration,
      getProcessTypeTagType,
      getProcessTypeName,
      getStatusText,
      getStatusTagType,
      getActivityType,
      getActivityIcon,
      formatVariableName,
      formatVariableValue
    }
  }
}
</script>

<style scoped>
.process-history-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-filter-section {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.search-form {
  width: 100%;
}

.table-container {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.process-detail-container {
  padding: 10px;
}

.detail-card {
  margin-bottom: 20px;
}

.timeline-container {
  max-height: 500px;
  overflow-y: auto;
}

.activity-content {
  padding: 5px 0;
}

.activity-description {
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  margin-top: 5px;
  word-break: break-all;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .process-history-container {
    padding: 10px;
  }
  
  .search-form {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
  
  .card-header {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
  
  .pagination-container {
    justify-content: center;
  }
}
</style>