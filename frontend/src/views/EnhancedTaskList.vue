<template>
  <div class="enhanced-task-list fade-in">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="page-title">任务管理</span>
          <div class="header-actions">
            <el-button 
              type="text" 
              icon="Refresh" 
              @click="refreshTasks" 
              :loading="loading"
              circle
              v-tooltip="{ content: '刷新任务列表', placement: 'top' }"
            ></el-button>
            <el-select v-model="taskType" placeholder="任务类型" @change="handleSearch">
              <el-option label="待办任务" value="todo"></el-option>
              <el-option label="已办任务" value="done"></el-option>
            </el-select>
          </div>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-section">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="searchForm.taskName"
              placeholder="任务名称"
              clearable
              @keyup.enter="handleSearch"
            ></el-input>
          </el-col>
          <el-col :span="6">
            <el-input
              v-model="searchForm.processInstanceName"
              placeholder="流程实例名称"
              clearable
              @keyup.enter="handleSearch"
            ></el-input>
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
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-col>
        </el-row>
      </div>

      <!-- 任务列表 -->
      <div class="task-list">
        <el-skeleton v-if="loading && tasksData.length === 0" :rows="10" animated></el-skeleton>
        <el-table
          v-else
          :data="tasksData"
          style="width: 100%"
          border
          @row-click="handleRowClick"
          :row-class-name="(row) => row._visible ? 'table-row-visible' : ''"
          :row-style="{ cursor: 'pointer', transition: 'background-color 0.2s' }"
          :highlight-current-row="true"
        >
          <el-table-column prop="id" label="任务ID" width="180" show-overflow-tooltip></el-table-column>
          <el-table-column prop="name" label="任务名称" width="180"></el-table-column>
          <el-table-column prop="processInstanceId" label="流程实例ID" width="180" show-overflow-tooltip></el-table-column>
          <el-table-column prop="processDefinitionName" label="流程名称" width="180"></el-table-column>
          <el-table-column prop="assignee" label="处理人" width="120">
            <template #default="scope">
              {{ getUserName(scope.row.assignee) }}
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180">
            <template #default="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="dueDate" label="截止时间" width="180">
            <template #default="scope">
              {{ scope.row.dueDate ? formatDate(scope.row.dueDate) : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="priority" label="优先级" width="80">
            <template #default="scope">
              <el-tag :type="getPriorityType(scope.row.priority)">
                {{ getPriorityText(scope.row.priority) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="scope">
              <el-button
                size="small"
                type="primary"
                @click.stop="handleTaskComplete(scope.row)"
                v-if="!scope.row.completed && scope.row.assignee === currentUser.username"
                :disabled="loading"
              >
                处理
              </el-button>
              <el-button
                size="small"
                @click.stop="viewProcessHistory(scope.row)"
                :disabled="loading"
              >
                详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 空状态 -->
        <div v-if="!loading && tasksData.length === 0" class="empty-state">
          <el-empty 
            :description="taskType === 'todo' ? '暂无待办任务' : '暂无已办任务'"
            image-size="120"
          >
            <el-button 
              v-if="taskType === 'todo'" 
              type="primary" 
              @click="navigateToApply"
            >
              去发起流程
            </el-button>
          </el-empty>
        </div>

        <!-- 分页 -->
        <div class="pagination-container" v-if="!loading && pagination.total > 0">
          <el-pagination
            v-model:current-page="pagination.currentPage"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="pagination.total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :disabled="loading"
          ></el-pagination>
        </div>
      </div>
    </el-card>

    <!-- 任务处理对话框 -->
    <el-dialog
      v-model="taskCompleteDialogVisible"
      :title="`处理任务：${selectedTask?.name || ''}`"
      width="600px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <!-- 任务信息 -->
      <el-skeleton v-if="loadingTaskDetail" :rows="15" animated></el-skeleton>
      <div class="task-info" v-else-if="selectedTask">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="任务名称">{{ selectedTask.name }}</el-descriptions-item>
          <el-descriptions-item label="流程名称">{{ selectedTask.processDefinitionName }}</el-descriptions-item>
          <el-descriptions-item label="流程实例ID">{{ selectedTask.processInstanceId }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ selectedTask.applicantName || '未知申请人' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(selectedTask.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="getPriorityType(selectedTask.priority)" size="small">
              {{ getPriorityText(selectedTask.priority) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        
        <!-- 申请详情 -->
        <div class="application-details" v-if="selectedTask.leaveForm && typeof selectedTask.leaveForm === 'object'">
          <h4 class="section-title">申请详情</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="请假类型">{{ 
              selectedTask.leaveForm.leaveType === 'annual' ? '年假' : 
              selectedTask.leaveForm.leaveType === 'sick' ? '病假' : 
              selectedTask.leaveForm.leaveType === 'personal' ? '事假' : 
              selectedTask.leaveForm.leaveType === 'marriage' ? '婚假' : 
              selectedTask.leaveForm.leaveType === 'maternity' ? '产假/陪产假' : 
              selectedTask.leaveForm.leaveType === 'other' ? '其他' : 
              selectedTask.leaveForm.leaveType || '未知类型' 
            }}</el-descriptions-item>
            <el-descriptions-item label="开始日期">{{ selectedTask.leaveForm.startDate || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="结束日期">{{ selectedTask.leaveForm.endDate || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="请假天数">{{ selectedTask.leaveForm.leaveDays || 0 }} 天</el-descriptions-item>
            <el-descriptions-item label="请假原因">{{ selectedTask.leaveForm.leaveReason || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="紧急程度">{{ 
              selectedTask.leaveForm.urgency === 'normal' ? '一般' : 
              selectedTask.leaveForm.urgency === 'urgent' ? '紧急' : 
              selectedTask.leaveForm.urgency === 'very_urgent' ? '非常紧急' : 
              selectedTask.leaveForm.urgency || '一般' 
            }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <!-- 流程变量信息 -->
      <div class="process-variables" v-if="!loadingTaskDetail && taskVariables.length > 0">
        <h4 class="section-title">流程信息</h4>
        <el-table :data="taskVariables" size="small" border style="width: 100%">
          <el-table-column prop="name" label="名称" width="150"></el-table-column>
          <el-table-column prop="value" label="值" show-overflow-tooltip></el-table-column>
          <el-table-column prop="type" label="类型" width="100"></el-table-column>
        </el-table>
      </div>

      <!-- 审批操作 -->
      <div class="approval-section" v-if="!loadingTaskDetail">
        <h4 class="section-title">审批操作</h4>
        <el-radio-group v-model="approvalAction" class="approval-action">
          <el-radio label="approve" border>同意</el-radio>
          <el-radio label="reject" border>拒绝</el-radio>
        </el-radio-group>
        
        <el-form-item label="审批意见" style="margin-top: 15px;">
          <el-input
            v-model="approvalComment"
            type="textarea"
            rows="4"
            placeholder="请输入审批意见"
            show-word-limit
            maxlength="500"
          ></el-input>
        </el-form-item>

        <!-- 动态审批表单字段 -->
        <div v-if="dynamicFormFields.length > 0">
          <el-divider>补充信息</el-divider>
          <el-form :model="approvalFormData" label-width="100px">
            <template v-for="field in dynamicFormFields" :key="field.key">
              <el-form-item :label="field.label">
                <template v-if="field.type === 'select'">
                  <el-select
                    v-model="approvalFormData[field.key]"
                    :placeholder="`请选择${field.label}`"
                    style="width: 100%"
                  >
                    <el-option
                      v-for="option in field.options"
                      :key="option.value"
                      :label="option.label"
                      :value="option.value"
                    ></el-option>
                  </el-select>
                </template>
                <template v-else-if="field.type === 'input'">
                  <el-input
                    v-model="approvalFormData[field.key]"
                    :placeholder="`请输入${field.label}`"
                  ></el-input>
                </template>
                <template v-else-if="field.type === 'textarea'">
                  <el-input
                    v-model="approvalFormData[field.key]"
                    type="textarea"
                    rows="3"
                    :placeholder="`请输入${field.label}`"
                  ></el-input>
                </template>
                <template v-else>
                  <el-input
                    v-model="approvalFormData[field.key]"
                    :placeholder="`请输入${field.label}`"
                  ></el-input>
                </template>
              </el-form-item>
            </template>
          </el-form>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="taskCompleteDialogVisible = false" :disabled="submittingTask">取消</el-button>
          <el-button type="primary" @click="submitTaskComplete" :loading="submittingTask">
            <el-icon v-if="submittingTask"><Loading /></el-icon>
            <span v-else>提交</span>
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 流程历史对话框 -->
    <el-dialog
      v-model="processHistoryDialogVisible"
      :title="`流程历史：${selectedTask?.processDefinitionName || ''}`"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-skeleton v-if="loadingProcessHistory" :rows="15" animated></el-skeleton>
      <template v-else>
        <!-- 流程图 -->
        <div class="process-diagram" v-if="processImageUrl">
          <h4 class="section-title">流程图</h4>
          <div class="diagram-container">
            <el-image
              :src="processImageUrl"
              alt="流程图"
              class="process-image"
              fit="contain"
              lazy
            >
              <template #error>
                <div class="image-slot">
                  <el-icon><InfoFilled /></el-icon>
                  <span>图片加载失败</span>
                </div>
              </template>
            </el-image>
          </div>
        </div>

        <!-- 流程历史 -->
        <div class="process-history">
          <h4 class="section-title">流程历史</h4>
          <el-empty v-if="processHistory.length === 0" description="暂无流程历史记录"></el-empty>
          <el-timeline v-else>
            <el-timeline-item
              v-for="(record, index) in processHistory"
              :key="record.id"
              :timestamp="formatDate(record.time)"
              :type="record.type === 'start' ? 'primary' : record.type === 'complete' ? 'success' : 'info'"
              :placement="index % 2 === 0 ? 'top' : 'bottom'"
            >
              <el-card shadow="hover" class="timeline-card">
                <h5 class="timeline-title">{{ record.activityName }}</h5>
                <p v-if="record.assignee" class="timeline-content">处理人：<el-tag size="small">{{ getUserName(record.assignee) }}</el-tag></p>
                <p v-if="record.applicant" class="timeline-content">申请人：<el-tag size="small" type="primary">{{ getUserName(record.applicant) }}</el-tag></p>
                <p v-if="record.comment" class="timeline-content">备注：{{ record.comment }}</p>
                <p v-if="record.action" class="timeline-content">
                  操作：
                  <el-tag 
                    size="small" 
                    :type="record.action === 'approve' ? 'success' : 'danger'"
                  >
                    {{ record.action === 'approve' ? '同意' : '拒绝' }}
                  </el-tag>
                </p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </div>

        <!-- 任务评论 -->
        <div class="task-comments" v-if="taskComments.length > 0">
          <h4 class="section-title">任务评论</h4>
          <div v-for="comment in taskComments" :key="comment.id" class="comment-item">
            <div class="comment-header">
              <span class="comment-user">{{ getUserName(comment.userId) }}</span>
              <span class="comment-time">{{ formatDate(comment.time) }}</span>
            </div>
            <div class="comment-content">{{ comment.message }}</div>
          </div>
        </div>
      </template>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="processHistoryDialogVisible = false" :disabled="loadingProcessHistory">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElTooltip } from 'element-plus'
import { Loading, Refresh, InfoFilled } from '@element-plus/icons-vue'
import TaskService from '../services/TaskService'
import ProcessDefinitionService from '../services/ProcessDefinitionService'
import UserService from '../services/UserService'

export default {
  name: 'EnhancedTaskList',
  setup() {
    // 初始化router
    const router = useRouter()
    
    // 响应式数据
    const loading = ref(false)
    const loadingTaskDetail = ref(false)
    const loadingProcessHistory = ref(false)
    const submittingTask = ref(false)
    const taskCompleteDialogVisible = ref(false)
    const processHistoryDialogVisible = ref(false)
    const tasks = ref([])
    const selectedTask = ref(null)
    const taskVariables = ref([])
    const processHistory = ref([])
    const taskComments = ref([])
    const processImageUrl = ref('')
    const currentUser = ref({})
    const allUsers = ref([])
    
    // 任务类型筛选
    const taskType = ref('todo')
    
    // 搜索表单
    const searchForm = reactive({
      taskName: '',
      processInstanceName: '',
      assignee: '',
      startTime: '',
      endTime: ''
    })
    
    const dateRange = ref([])
    
    // 审批相关
    const approvalAction = ref('approve')
    const approvalComment = ref('')
    const approvalFormData = reactive({})
    
    // 分页
    const pagination = reactive({
      currentPage: 1,
      pageSize: 10,
      total: 0
    })
    
    // 根据流程类型获取动态表单字段
    const dynamicFormFields = computed(() => {
      if (!selectedTask.value) return []
      
      // 这里可以根据流程定义的key来返回不同的动态表单字段
      const processKey = selectedTask.value.processDefinitionKey
      
      // 示例：请假流程可能需要主管添加意见或调整天数
      if (processKey?.includes('leave')) {
        return [
          {
            key: 'managerComment',
            label: '主管意见',
            type: 'textarea',
            required: true
          },
          {
            key: 'adjustedDays',
            label: '调整天数',
            type: 'select',
            options: [
              { label: '保持原天数', value: 'original' },
              { label: '减少0.5天', value: 'reduce_0.5' },
              { label: '减少1天', value: 'reduce_1' }
            ]
          }
        ]
      }
      
      // 示例：入职流程可能需要IT主管确认设备准备情况
      if (processKey?.includes('onboarding')) {
        return [
          {
            key: 'equipmentPrepared',
            label: '设备准备',
            type: 'select',
            options: [
              { label: '已准备', value: 'prepared' },
              { label: '准备中', value: 'preparing' },
              { label: '未准备', value: 'not_prepared' }
            ]
          },
          {
            key: 'itComment',
            label: 'IT备注',
            type: 'textarea'
          }
        ]
      }
      
      return []
    })
    
    // 计算当前页显示的数据
    const tasksData = computed(() => {
      return tasks.value
    })
    
    // 加载当前用户信息
    const loadCurrentUser = () => {
      try {
        const userInfoStr = localStorage.getItem('user')
        if (userInfoStr) {
          currentUser.value = JSON.parse(userInfoStr)
          searchForm.assignee = currentUser.value.username
        }
      } catch (error) {
        console.error('加载用户信息错误:', error)
      }
    }
    
    // 加载用户列表
    const loadUsers = async () => {
      try {
        const response = await UserService.getUserList()
        
        // 多种响应格式兼容处理
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
          } else if (response.data && typeof response.data === 'object') {
            // response.data是对象，检查是否有data或list字段
            if (Array.isArray(response.data.data)) {
              allUsers.value = response.data.data
            } else if (Array.isArray(response.data.list)) {
              // 分页格式: {data: {list: [...]}}
              allUsers.value = response.data.list
            } else {
              // 其他情况，使用空数组
              allUsers.value = []
            }
          } else {
            // 其他情况，使用空数组
            allUsers.value = []
          }
        } else {
          // 响应不是数组也不是对象，使用空数组
          console.warn('用户列表响应格式异常，使用空数组', response)
          allUsers.value = []
        }
        
        console.log('用户列表加载完成，共', allUsers.value.length, '个用户')
      } catch (error) {
        console.error('加载用户列表错误:', error)
        // 出错时提供模拟用户数据，避免页面崩溃
        allUsers.value = []
      }
    }
    
    // 获取用户名称
    const getUserName = (username) => {
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
    
    // 获取优先级类型
    const getPriorityType = (priority) => {
      if (priority >= 100) return 'danger'
      if (priority >= 50) return 'warning'
      return 'success'
    }
    
    // 获取优先级文本
    const getPriorityText = (priority) => {
      if (priority >= 100) return '高'
      if (priority >= 50) return '中'
      return '低'
    }
    
    // 加载任务列表
    const loadTasks = async () => {
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
        
        console.log('开始加载任务，类型:', taskType.value, '搜索条件:', searchForm)
        
        let result
        if (taskType.value === 'todo') {
          // 待办任务
          result = await TaskService.getTasks({
            ...searchForm,
            page: pagination.currentPage,
            pageSize: pagination.pageSize
          })
        } else {
          // 已办任务
          result = await TaskService.getCompletedTasks({
            ...searchForm,
            page: pagination.currentPage,
            pageSize: pagination.pageSize
          })
        }
        
        console.log('任务加载结果:', result)
        
        // 直接使用TaskService返回的数据格式
        tasks.value = (result.data || []).map(task => ({
          ...task,
          // 如果processDefinitionName缺失，从processDefinitionId中提取流程名称
          processDefinitionName: task.processDefinitionName || 
            (task.processDefinitionId ? task.processDefinitionId.split(':')[0] : '未知流程')
        }))
        pagination.total = result.total || 0
        
        // 添加进入动画类
        setTimeout(() => {
          tasks.value = tasks.value.map(task => ({ ...task, _visible: true }))
        }, 100)
        
        // 添加成功提示
        if (tasks.value.length === 0) {
          ElMessage.info(taskType.value === 'todo' ? '暂无待办任务' : '暂无已办任务')
        }
      } catch (error) {
        console.error('加载任务错误:', error)
        ElMessage.error('加载任务失败: ' + (error.message || '网络错误'))
        tasks.value = []
        pagination.total = 0
      } finally {
        loading.value = false
      }
    }
    
    // 搜索
    const handleSearch = () => {
      pagination.currentPage = 1
      loadTasks()
    }
    
    // 重置搜索
    const resetSearch = () => {
      searchForm.taskName = ''
      searchForm.processInstanceName = ''
      dateRange.value = []
      pagination.currentPage = 1
      loadTasks()
    }
    
    // 分页大小变化
    const handleSizeChange = (size) => {
      pagination.pageSize = size
      loadTasks()
    }
    
    // 当前页变化
    const handleCurrentChange = (current) => {
      pagination.currentPage = current
      loadTasks()
    }
    
    // 行点击
    const handleRowClick = (row) => {
      if (!row.completed && row.assignee === currentUser.value.username) {
        handleTaskComplete(row)
      } else {
        viewProcessHistory(row)
      }
    }
    
    // 处理任务
    const handleTaskComplete = async (task) => {
      selectedTask.value = task
      approvalAction.value = 'approve'
      approvalComment.value = ''
      
      // 清空审批表单
      Object.keys(approvalFormData).forEach(key => {
        delete approvalFormData[key]
      })
      
      // 加载任务变量
      try {
        loadingTaskDetail.value = true
        const variables = await TaskService.getTaskVariables(task.id)
        console.log('[EnhancedTaskList] 任务变量:', variables)
        
        // 从任务变量中提取申请详情
        let applicant = '未知申请人'
        let leaveForm = {}
        
        // 检查任务变量的结构，支持多种可能的格式
        if (variables && typeof variables === 'object') {
          // 情况1: variables是扁平对象，直接包含申请人信息和申请详情
          if (variables.applicantName || variables.applicant || variables.username) {
            applicant = variables.applicantName || variables.applicant || variables.username
            leaveForm = {
              leaveType: variables.leaveType,
              startDate: variables.startDate,
              endDate: variables.endDate,
              leaveDays: variables.leaveDays,
              leaveReason: variables.leaveReason,
              urgency: variables.urgency
            }
          }
          // 情况2: variables包含leaveForm对象
          else if (variables.leaveForm && typeof variables.leaveForm === 'object') {
            leaveForm = variables.leaveForm
            applicant = leaveForm.applicantName || leaveForm.applicant || leaveForm.username || '未知申请人'
          }
          // 情况3: variables包含嵌套的data对象
          else if (variables.data && typeof variables.data === 'object') {
            if (variables.data.leaveForm) {
              leaveForm = variables.data.leaveForm
              applicant = leaveForm.applicantName || leaveForm.applicant || leaveForm.username || '未知申请人'
            } else {
              applicant = variables.data.applicantName || variables.data.applicant || variables.data.username || '未知申请人'
              leaveForm = {
                leaveType: variables.data.leaveType,
                startDate: variables.data.startDate,
                endDate: variables.data.endDate,
                leaveDays: variables.data.leaveDays,
                leaveReason: variables.data.leaveReason,
                urgency: variables.data.urgency
              }
            }
          }
        }
        
        // 将申请人信息和申请详情保存到selectedTask中
        selectedTask.value = {
          ...selectedTask.value,
          applicantName: applicant,
          leaveForm: leaveForm
        };
        
        // 处理所有任务变量，显示在流程信息部分
        taskVariables.value = Object.entries(variables)
          .filter(([name, value]) => value !== undefined && value !== null && typeof value !== 'object')
          .map(([name, value]) => ({
            name,
            value: JSON.stringify(value),
            type: typeof value
          }))
      } catch (error) {
        console.error('加载任务变量错误:', error)
        ElMessage.warning('加载任务变量失败，但不影响任务处理')
      } finally {
        loadingTaskDetail.value = false
        taskCompleteDialogVisible.value = true
      }
    }
    
    // 提交任务完成
    const submitTaskComplete = async () => {
      if (!selectedTask.value) {
        ElMessage.warning('未选择任务')
        return
      }
      
      // 二次确认
      try {
        await ElMessageBox.confirm(
          approvalAction.value === 'approve' ? '确定同意此任务吗？' : '确定拒绝此任务吗？',
          '确认操作',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: approvalAction.value === 'approve' ? 'success' : 'warning'
          }
        )
      } catch {
        return // 用户取消操作
      }
      
      // 构建任务变量 - 确保变量格式正确并包含必要的流程变量
      const variables = {
        approvalAction: approvalAction.value,
        approvalComment: approvalComment.value,
        processedTime: new Date().toISOString(),
        processor: currentUser.value.username,
        // 添加approval变量，与后端期望的变量名保持一致
        approval: approvalAction.value === 'approve',
        // 确保approved变量存在（流程网关条件判断需要）
        approved: approvalAction.value === 'approve',
        // 流程表单期望的comment变量
        comment: approvalComment.value.trim() || '',
        // 确保managerApproval变量存在（与流程监听器期望保持一致）
        managerApproval: approvalAction.value === 'approve' ? 'approved' : 'rejected',
        managerComments: approvalComment.value.trim() || ''
      }
      
      // 添加动态表单字段的值
      dynamicFormFields.value.forEach(field => {
        if (approvalFormData[field.key] !== undefined) {
          variables[field.key] = approvalFormData[field.key]
        }
      })
      
      // 构建完成任务请求体 - 简化格式以匹配后端处理逻辑
      const completeRequest = {
        variables,
        withLocalVariables: true
      }
      
      try {
        submittingTask.value = true
        console.log('提交任务完成请求:', { taskId: selectedTask.value.id, variables })
        const success = await TaskService.completeTask(selectedTask.value.id, completeRequest)
        if (success) {
          ElMessage.success('任务处理成功')
          taskCompleteDialogVisible.value = false
          
          // 任务完成后保持在当前页面并刷新任务列表
          setTimeout(() => {
            // 直接刷新当前任务列表，保持在待办任务页面
            loadTasks()
          }, 300)
          
          // 通知相关用户刷新列表（可以通过WebSocket或其他方式实现）
          console.log('任务已完成，后续任务将自动分配给相关用户')
        } else {
          // TaskService.completeTask内部已经处理了错误消息显示
          console.log('任务处理失败')
        }
      } catch (error) {
        console.error('完成任务错误:', error)
        ElMessage.error('任务处理失败: ' + (error.message || '网络错误'))
      } finally {
        submittingTask.value = false
      }
    }
    
    // 查看流程历史
    const viewProcessHistory = async (task) => {
      selectedTask.value = task
      processHistory.value = []
      taskComments.value = []
      processImageUrl.value = ''
      
      try {
        loadingProcessHistory.value = true
        
        // 只加载流程历史和任务评论，不加载可能导致错误的任务详情
        // 使用try-catch包裹每个API调用，确保一个API失败不会影响其他功能
        
        // 1. 加载流程历史
        let historyData = []
        try {
          const historyResponse = await TaskService.getProcessInstanceHistory(task.processInstanceId)
          console.log('[EnhancedTaskList] 流程历史API响应:', historyResponse)
          
          // 智能处理多种响应格式
          if (Array.isArray(historyResponse)) {
            historyData = historyResponse
          } else if (historyResponse && typeof historyResponse === 'object') {
            if (Array.isArray(historyResponse.data)) {
              historyData = historyResponse.data
            } else if (historyResponse.data && Array.isArray(historyResponse.data.data)) {
              historyData = historyResponse.data.data
            } else if (historyResponse.data && Array.isArray(historyResponse.data.list)) {
              historyData = historyResponse.data.list
            } else if (Array.isArray(historyResponse.list)) {
              historyData = historyResponse.list
            } else if ((historyResponse.code === 200 || historyResponse.success !== false) && 
                      (Array.isArray(historyResponse.data) || Array.isArray(historyResponse.result))) {
              historyData = historyResponse.data || historyResponse.result || []
            }
          }
        } catch (error) {
          console.error('[EnhancedTaskList] 加载流程历史失败:', error)
          historyData = []
        }
        
        // 2. 加载任务评论
        try {
          const commentsResponse = await TaskService.getTaskComments(task.id)
          console.log('[EnhancedTaskList] 任务评论API响应:', commentsResponse)
          
          // 智能处理多种响应格式
          if (Array.isArray(commentsResponse)) {
            taskComments.value = commentsResponse
          } else if (commentsResponse && typeof commentsResponse === 'object') {
            if (Array.isArray(commentsResponse.data)) {
              taskComments.value = commentsResponse.data
            } else if (commentsResponse.data && Array.isArray(commentsResponse.data.data)) {
              taskComments.value = commentsResponse.data.data
            } else if (commentsResponse.data && Array.isArray(commentsResponse.data.list)) {
              taskComments.value = commentsResponse.data.list
            } else if (Array.isArray(commentsResponse.list)) {
              taskComments.value = commentsResponse.list
            } else if ((commentsResponse.code === 200 || commentsResponse.success !== false) && 
                      (Array.isArray(commentsResponse.data) || Array.isArray(commentsResponse.result))) {
              taskComments.value = commentsResponse.data || commentsResponse.result || []
            } else {
              taskComments.value = []
            }
          } else {
            taskComments.value = []
          }
        } catch (error) {
          console.error('[EnhancedTaskList] 加载任务评论失败:', error)
          taskComments.value = []
        }
        
        // 3. 不再加载可能导致错误的任务详情，避免"获取任务详情API返回空响应或无效格式"错误
        
        // 4. 直接使用真实的后端数据，不添加模拟的开始事件
        // 过滤掉可能的重复事件
        const uniqueEvents = []
        const eventIds = new Set()
        
        for (const event of historyData) {
          // 使用activityId、id或时间戳作为唯一标识
          const eventId = event.activityId || event.id || `${event.activityName}_${event.time}`
          if (!eventIds.has(eventId)) {
            eventIds.add(eventId)
            uniqueEvents.push(event)
          }
        }
        
        // 5. 按时间顺序排序，确保事件按照发生时间正确显示
        uniqueEvents.sort((a, b) => {
          const timeA = new Date(a.time || a.startTime || a.createTime || 0).getTime()
          const timeB = new Date(b.time || b.startTime || b.createTime || 0).getTime()
          return timeA - timeB
        })
        
        // 6. 直接使用真实的后端数据，不添加任何模拟数据
        processHistory.value = uniqueEvents
        
        // 7. 尝试获取流程图（静默失败，不阻止历史查看）
        try {
          const diagramUrl = ProcessDefinitionService.getProcessDiagramUrlByInstanceId(task.processDefinitionId)
          processImageUrl.value = `${diagramUrl}?_t=${Date.now()}`
        } catch (err) {
          console.warn('获取流程图失败:', err)
          processImageUrl.value = ''
        }
        
        processHistoryDialogVisible.value = true
      } catch (error) {
        console.error('[EnhancedTaskList] 查看流程历史失败:', error)
        ElMessage.error('查看流程历史失败: ' + (error.message || '网络错误'))
      } finally {
        loadingProcessHistory.value = false
      }
    }
    
    // 刷新任务列表
    const refreshTasks = () => {
      pagination.currentPage = 1
      loadTasks()
    }
    
    // 导航到流程申请页面
    const navigateToApply = () => {
      router.push('/generic-process-apply')
    }
    
    // 初始化
    onMounted(async () => {
      loadCurrentUser()
      await loadUsers()
      await loadTasks()
    })
    
    return {
      loading,
      loadingTaskDetail,
      loadingProcessHistory,
      submittingTask,
      taskCompleteDialogVisible,
      processHistoryDialogVisible,
      tasksData,
      selectedTask,
      taskVariables,
      processHistory,
      taskComments,
      processImageUrl,
      currentUser,
      taskType,
      searchForm,
      dateRange,
      approvalAction,
      approvalComment,
      approvalFormData,
      dynamicFormFields,
      pagination,
      handleSearch,
      resetSearch,
      handleSizeChange,
      handleCurrentChange,
      handleRowClick,
      handleTaskComplete,
      submitTaskComplete,
      viewProcessHistory,
      refreshTasks,
      navigateToApply,
      getUserName,
      formatDate,
      getPriorityType,
      getPriorityText
    }
  }
}
</script>

<style scoped>
.enhanced-task-list {
  padding: var(--spacing-xl, 20px);
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  font-size: 18px;
  font-weight: 500;
  color: var(--text-primary, #303133);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-md, 10px);
}

.search-section {
  margin-bottom: var(--spacing-lg, 20px);
  padding: var(--spacing-md, 15px);
  background-color: var(--bg-secondary, #f5f7fa);
  border-radius: var(--border-radius-base, 4px);
}

.task-list {
  margin-top: var(--spacing-lg, 20px);
}

.pagination-container {
  margin-top: var(--spacing-lg, 20px);
  display: flex;
  justify-content: flex-end;
}

.task-info {
  margin-bottom: var(--spacing-lg, 20px);
}

.section-title {
  margin-bottom: var(--spacing-md, 15px);
  font-size: 16px;
  font-weight: 500;
  color: var(--text-primary, #303133);
  padding-bottom: var(--spacing-xs, 8px);
  border-bottom: 1px solid var(--border-light, #ebeef5);
}

.process-variables {
  margin-bottom: var(--spacing-lg, 20px);
}

.approval-section {
  margin-top: var(--spacing-lg, 20px);
}

.approval-action {
  margin-bottom: var(--spacing-md, 15px);
}

.process-diagram {
  margin-bottom: var(--spacing-lg, 20px);
}

.diagram-container {
  overflow: auto;
  max-height: 400px;
  border: 1px solid var(--border-light, #ebeef5);
  border-radius: var(--border-radius-base, 4px);
  padding: var(--spacing-md, 10px);
  background-color: var(--bg-regular, #ffffff);
}

.process-image {
  max-width: 100%;
  max-height: 400px;
  transition: opacity 0.3s;
}

.process-history {
  margin-top: var(--spacing-lg, 20px);
}

.task-comments {
  margin-top: var(--spacing-lg, 20px);
}

.comment-item {
  padding: var(--spacing-md, 10px);
  margin-bottom: var(--spacing-md, 10px);
  background-color: var(--bg-secondary, #f5f7fa);
  border-radius: var(--border-radius-base, 4px);
  transition: all var(--transition-fast, 0.2s);
}

.comment-item:hover {
  box-shadow: var(--shadow-light, 0 2px 12px 0 rgba(0, 0, 0, 0.1));
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: var(--spacing-xs, 5px);
}

.comment-user {
  font-weight: bold;
  color: var(--primary-color, #409eff);
}

.comment-time {
  color: var(--text-secondary, #909399);
  font-size: 12px;
}

.comment-content {
  color: var(--text-regular, #606266);
  line-height: 1.6;
}

.el-timeline-item__timestamp {
  font-size: 12px;
  color: var(--text-secondary, #909399);
}

.timeline-card {
  border-radius: var(--border-radius-base, 4px);
  transition: all var(--transition-normal, 0.3s);
}

.timeline-card:hover {
  box-shadow: var(--shadow-light, 0 2px 12px 0 rgba(0, 0, 0, 0.1));
}

.timeline-title {
  margin-bottom: var(--spacing-xs, 8px);
  color: var(--text-primary, #303133);
  font-weight: 500;
}

.timeline-content {
  margin-bottom: var(--spacing-xs, 5px);
  color: var(--text-regular, #606266);
  font-size: 13px;
}

.empty-state {
  padding: var(--spacing-xxl, 40px) 0;
  text-align: center;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 200px;
  height: 100px;
  background-color: var(--bg-secondary, #f5f7fa);
  color: var(--text-secondary, #909399);
  flex-direction: column;
  gap: var(--spacing-xs, 8px);
}

/* 表格行进入动画 */
:deep(.table-row-visible > td) {
  animation: tableRowSlideIn var(--transition-normal, 0.3s) ease-out;
}

/* 表格行悬停效果 */
:deep(.el-table__row:hover > td) {
  background-color: var(--bg-hover, #f5f7fa) !important;
}

/* 动画效果 */
.fade-in {
  animation: fadeIn var(--transition-normal, 0.3s) ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes tableRowSlideIn {
  from {
    transform: translateX(-10px);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

/* 响应式调整 */
@media (max-width: 768px) {
  .enhanced-task-list {
    padding: var(--spacing-md, 10px);
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-md, 10px);
    width: 100%;
  }
  
  .header-actions {
    width: 100%;
    justify-content: space-between;
  }
  
  .search-section .el-row {
    flex-direction: column;
  }
  
  .search-section .el-col {
    width: 100% !important;
    margin-bottom: var(--spacing-sm, 8px);
  }
  
  .el-table {
    font-size: 12px;
  }
  
  .el-table__header th,
  .el-table__body td {
    padding: 8px 4px;
  }
  
  .el-dialog {
    width: 95% !important;
    margin: 10px auto;
  }
  
  .diagram-container {
    padding: var(--spacing-sm, 5px);
    max-height: 300px;
  }
  
  .section-title {
    font-size: 14px;
  }
}
</style>