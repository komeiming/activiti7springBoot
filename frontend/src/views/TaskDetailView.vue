<template>
  <div class="task-detail-container">
    <el-page-header
      @back="handleBack"
      content="任务详情"
    />
    
    <el-card shadow="hover" class="task-detail-card" v-if="task">
      <template #header>
        <div class="card-header">
          <span>{{ task.name || '任务详情' }}</span>
          <div>
            <el-tag :type="getPriorityType(task.priority)">
              优先级: {{ task.priority || 50 }}
            </el-tag>
            <el-button 
              v-if="!completed && (isTaskOwner || isTaskAssignee)" 
              size="small" 
              type="primary"
              plain
              @click="showTaskEditForm = true"
              style="margin-left: 10px"
            >
              编辑任务
            </el-button>
          </div>
        </div>
      </template>

      <el-descriptions :column="1" border>
        <el-descriptions-item label="任务ID">{{ task.id }}</el-descriptions-item>
        <el-descriptions-item label="任务名称">{{ task.name }}</el-descriptions-item>
        <el-descriptions-item label="流程定义ID">{{ task.processDefinitionId }}</el-descriptions-item>
        <el-descriptions-item label="流程实例ID">{{ task.processInstanceId }}</el-descriptions-item>
        <el-descriptions-item label="办理人">{{ task.assignee || '未分配' }}</el-descriptions-item>
        <el-descriptions-item label="拥有者">{{ task.owner || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDate(task.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="截止时间">
          <span v-if="task.dueDate" :class="{ 'overdue': isOverdue(task.dueDate) }">
            {{ formatDate(task.dueDate) }}
          </span>
          <span v-else>无</span>
        </el-descriptions-item>
        <el-descriptions-item label="优先级">{{ task.priority || 50 }}</el-descriptions-item>
        <el-descriptions-item label="描述">{{ task.description || '-' }}</el-descriptions-item>
      </el-descriptions>
      
      <!-- 任务变量 -->
      <div class="variables-section" v-if="taskVariables && Object.keys(taskVariables).length > 0">
        <h3 style="margin-top: 20px; margin-bottom: 10px;">任务变量</h3>
        <el-table :data="variableList" style="width: 100%">
          <el-table-column prop="name" label="变量名" />
          <el-table-column prop="value" label="变量值" />
          <el-table-column prop="type" label="类型" />
        </el-table>
      </div>
      
      <!-- 流程历史记录 -->
      <div class="history-section">
        <h3 style="margin-top: 20px; margin-bottom: 10px;">流程历史记录</h3>
        <div v-if="processHistory && processHistory.length > 0" class="history-container">
          <div v-for="record in processHistory" :key="record.id" class="history-item">
            <div class="history-header">
              <span class="history-activity">{{ record.activityName }}</span>
              <span class="history-time">{{ formatDate(record.time) }}</span>
            </div>
            <div class="history-content">
              <p v-if="record.assignee" class="history-assignee">操作人: {{ record.assignee }}</p>
              <p v-if="record.applicant" class="history-applicant">申请人: <span style="color: #409eff; font-weight: bold;">{{ record.applicant }}</span></p>
              <p v-if="record.comment" class="history-comment">意见: {{ record.comment }}</p>
              <p v-if="record.action" class="history-action">
                操作: {{ record.action === 'approve' ? '通过' : record.action === 'reject' ? '拒绝' : record.action }}
              </p>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无流程历史记录" />
      </div>
      
      <!-- 任务评论 -->
      <div class="comments-section">
        <h3 style="margin-top: 20px; margin-bottom: 10px;">任务评论</h3>
        <div v-if="comments.length > 0" class="comments-container">
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <div class="comment-header">
              <span class="comment-author">{{ comment.userId }}</span>
              <span class="comment-time">{{ formatDate(comment.time) }}</span>
            </div>
            <div class="comment-content">{{ comment.fullMessage }}</div>
          </div>
        </div>
        <el-empty v-else description="暂无评论" />
        
        <el-form v-if="!completed && (isTaskAssignee || isTaskOwner)" class="add-comment-form" :model="commentForm">
          <el-form-item>
            <el-input
              type="textarea"
              v-model="commentForm.content"
              placeholder="添加评论..."
              rows="2"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="addComment" size="small">添加评论</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 任务表单 -->
      <div class="form-section" v-if="!completed">
        <h3 style="margin-top: 20px; margin-bottom: 10px;">任务表单</h3>
        <el-form ref="taskFormRef" :model="taskForm" label-width="80px">
          <el-form-item label="审批意见">
            <el-input
              v-model="taskForm.comment"
              type="textarea"
              placeholder="请输入审批意见"
              rows="4"
            />
          </el-form-item>
          <el-form-item>
            <el-radio-group v-model="taskForm.approve">
              <el-radio :label="true">通过</el-radio>
              <el-radio :label="false">拒绝</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <!-- 动态表单字段 -->
          <template v-for="field in dynamicFormFields" :key="field.key">
            <el-form-item :label="field.label">
              <template v-if="field.type === 'string' || field.type === 'text'">
                <el-input
                  v-if="field.type === 'string'"
                  v-model="taskForm[field.key]"
                  :placeholder="`请输入${field.label}`"
                  :disabled="field.readonly"
                />
                <el-input
                  v-else
                  type="textarea"
                  v-model="taskForm[field.key]"
                  :placeholder="`请输入${field.label}`"
                  rows="3"
                  :disabled="field.readonly"
                />
              </template>
              <el-select
                v-else-if="field.type === 'select'"
                v-model="taskForm[field.key]"
                :placeholder="`请选择${field.label}`"
                :disabled="field.readonly"
              >
                <el-option
                  v-for="option in field.options"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
          </template>
        </el-form>
        
        <!-- 任务操作按钮 -->
        <div class="action-buttons">
          <el-button 
            v-if="!task.assignee || task.assignee === currentUsername" 
            @click="claimTask"
            :loading="loading"
          >
            认领任务
          </el-button>
          <el-button 
            v-if="task.assignee === currentUsername" 
            @click="unclaimTask"
            :loading="loading"
          >
            取消认领
          </el-button>
          <el-button 
            v-if="task.assignee === currentUsername" 
            type="warning" 
            @click="delegateTask"
            :loading="loading"
          >
            委派任务
          </el-button>
          <el-button 
            v-if="task.assignee === currentUsername" 
            type="primary" 
            @click="handleCompleteTask"
            :loading="loading"
          >
            完成任务
          </el-button>
          <el-button @click="handleBack">返回列表</el-button>
        </div>
      </div>
      
      <div class="completed-section" v-else>
        <el-alert
          title="任务已完成"
          type="success"
          description="该任务已经被完成，无法再进行操作"
          show-icon
        />
        <el-button @click="handleBack" style="margin-top: 20px;">返回列表</el-button>
      </div>
    </el-card>
    
    <el-empty v-else description="暂无任务数据" />
    
    <!-- 编辑任务对话框 -->
    <el-dialog
      title="编辑任务"
      v-model="showTaskEditForm"
      width="500px"
    >
      <el-form :model="editTaskForm" label-width="100px">
        <el-form-item label="任务名称">
          <el-input v-model="editTaskForm.name" />
        </el-form-item>
        <el-form-item label="任务描述">
          <el-input type="textarea" v-model="editTaskForm.description" rows="3" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="editTaskForm.priority" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker
            v-model="editTaskForm.dueDate"
            type="datetime"
            placeholder="选择日期时间"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTaskEditForm = false">取消</el-button>
        <el-button type="primary" @click="updateTask">保存</el-button>
      </template>
    </el-dialog>

    <!-- 委派任务对话框 -->
    <el-dialog
      title="委派任务"
      v-model="delegateDialogVisible"
      width="400px"
    >
      <el-form :model="delegateForm" label-width="80px">
        <el-form-item label="任务名称">
          <el-input v-model="delegateForm.taskName" disabled />
        </el-form-item>
        <el-form-item label="委派给" prop="assignee">
          <el-input 
            v-model="delegateForm.assignee" 
            placeholder="请输入委派的用户名"
            required
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="delegateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmDelegate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import TaskService from '../services/TaskService'
import UserService from '../services/UserService'
import { ElMessage } from 'element-plus'
export default {
  name: 'TaskDetailView',
  data() {
    return {
      task: null,
      taskVariables: {},
      taskForm: {
        comment: '',
        approve: true
      },
      loading: false,
      completed: false,
      currentUsername: UserService.getCurrentUser()?.username || '',
      comments: [],
      commentForm: {
        content: ''
      },
      dynamicFormFields: [],
      showTaskEditForm: false,
      editTaskForm: {
        name: '',
        description: '',
        priority: 50,
        dueDate: null
      },
      delegateDialogVisible: false,
      delegateForm: {
        taskId: '',
        taskName: '',
        assignee: ''
      },
      processHistory: [] // 流程历史记录
    }
  },
  computed: {
    variableList() {
      return Object.keys(this.taskVariables).map(key => ({
        name: key,
        value: JSON.stringify(this.taskVariables[key]),
        type: typeof this.taskVariables[key]
      }))
    },
    isTaskAssignee() {
      return this.task && this.task.assignee === this.currentUsername
    },
    isTaskOwner() {
      return this.task && this.task.owner === this.currentUsername
    }
  },
  mounted() {
    this.loadTaskDetail()
  },
  methods: {
    handleBack() {
      // 根据是否为已办任务决定返回页面
      const isCompleted = this.$route.query.completed === 'true'
      if (isCompleted) {
        this.$router.push({ name: 'completedTasks' })
      } else {
        this.$router.push({ name: 'taskList' })
      }
    },
    async loadTaskDetail() {
      try {
        const taskId = this.$route.params.id
        const isCompleted = this.$route.query.completed === 'true'
        
        // 设置任务状态
        this.completed = isCompleted
        
        // 使用TaskService获取任务详情，区分已完成和未完成任务
        let taskDetail = null;
        
        if (isCompleted) {
          // 对于已完成任务，我们需要从已完成任务列表中查找
          // 首先获取所有已完成任务（设置一个较大的pageSize）
          const completedTasksResponse = await TaskService.getCompletedTasks({ page: 1, pageSize: 100 });
          
          // 从列表中找到对应的任务
          let completedTasksList = [];
          
          // 处理不同的响应格式
          if (completedTasksResponse && completedTasksResponse.data) {
            if (Array.isArray(completedTasksResponse.data)) {
              completedTasksList = completedTasksResponse.data;
            } else if (completedTasksResponse.data.data && Array.isArray(completedTasksResponse.data.data)) {
              completedTasksList = completedTasksResponse.data.data;
            }
          }
          
          // 从列表中找到对应的任务
          const task = completedTasksList.find(t => t.id == taskId);
          if (task) {
            taskDetail = task;
            // 已完成任务直接设置task，不需要访问task属性
            this.task = taskDetail;
            // 对于已完成任务，我们可能没有variables，设置为空对象
            this.taskVariables = {};
            this.initializeEditForm();
            this.initializeDynamicFields();
            this.loadTaskComments();
            this.loadProcessHistory();
          }
        } else {
          // 对于未完成任务，使用常规的任务详情API
          taskDetail = await TaskService.getTaskDetail(taskId);
          
          if (taskDetail) {
            this.task = taskDetail.task || taskDetail;
            this.taskVariables = taskDetail.variables || {};
            this.initializeEditForm();
            this.initializeDynamicFields();
            this.loadTaskComments();
            this.loadProcessHistory();
          }
        }
      } catch (error) {
        console.error('获取任务详情错误:', error);
        // 显示友好的错误提示
        this.$message.error('获取任务详情失败: ' + (error.message || '请返回列表重试'));
      }
    },
    
    async loadTaskComments() {
      try {
        const taskId = this.$route.params.id
        // 使用TaskService获取任务评论
        this.comments = await TaskService.getTaskComments(taskId) || []
      } catch (error) {
        console.error('获取任务评论错误:', error)
        // 评论加载失败不影响主要功能
      }
    },
    
    async loadProcessHistory() {
      try {
        if (this.task && this.task.processInstanceId) {
          // 加载流程历史记录
          const historyResponse = await TaskService.getProcessInstanceHistory(this.task.processInstanceId)
          
          let historyData = []
          if (Array.isArray(historyResponse)) {
            historyData = historyResponse
          } else if (historyResponse && historyResponse.data && historyResponse.data.data) {
            historyData = historyResponse.data.data
          } else if (historyResponse && historyResponse.data) {
            historyData = Array.isArray(historyResponse.data) ? historyResponse.data : []
          }
          
          // 从当前任务数据中获取申请人信息，避免再次调用可能失败的API
          let applicantInfo = ''
          // 直接从task对象中获取申请人信息
          if (this.task) {
            // 尝试从任务对象的不同字段获取申请人信息
            applicantInfo = this.task.applicant || this.task.applyUser || 
                           this.task.createBy || this.task.owner || 
                           this.task.applyUsername || this.task.username || 
                           this.task.assignee || '申请人';
          }
          
          // 将申请人信息添加到流程历史的开始事件中
          this.processHistory = historyData.map(record => {
            if (record.type === 'start') {
              // 添加申请人信息到开始事件
              return {
                ...record,
                comment: record.comment || `申请人: ${applicantInfo}`,
                // 保存申请人信息用于显示
                applicant: applicantInfo
              }
            }
            return record
          })
        }
      } catch (error) {
        console.error('获取流程历史记录错误:', error)
        // 历史记录加载失败不影响主要功能
      }
    },
    
    async addComment() {
      if (!this.commentForm.content.trim()) {
        ElMessage.warning('请输入评论内容')
        return
      }
      
      try {
        const taskId = this.$route.params.id
        // 使用TaskService添加评论
        await TaskService.addTaskComment(taskId, this.commentForm.content)
        this.commentForm.content = ''
        this.loadTaskComments()
      } catch (error) {
        console.error('添加评论错误:', error)
        // TaskService内部已经处理了错误提示
      }
    },
    
    initializeEditForm() {
      if (!this.task) return
      this.editTaskForm = {
        name: this.task.name || '',
        description: this.task.description || '',
        priority: this.task.priority || 50,
        dueDate: this.task.dueDate ? new Date(this.task.dueDate) : null
      }
    },
    
    initializeDynamicFields() {
      // 清空现有字段
      this.dynamicFormFields = []
      
      // 根据任务名称或流程定义ID动态生成表单字段
      if (this.task) {
        // 请假流程相关字段
        if (this.task.name && this.task.name.includes('请假')) {
          this.dynamicFormFields.push(
            {
              key: 'leaveDays',
              label: '请假天数',
              type: 'string',
              placeholder: '请输入请假天数'
            },
            {
              key: 'leaveType',
              label: '请假类型',
              type: 'select',
              options: [
                { label: '年假', value: 'annual' },
                { label: '病假', value: 'sick' },
                { label: '事假', value: 'personal' },
                { label: '婚假', value: 'marriage' },
                { label: '产假/陪产假', value: 'maternity' },
                { label: '其他', value: 'other' }
              ]
            },
            {
              key: 'startDate',
              label: '开始日期',
              type: 'string',
              placeholder: 'YYYY-MM-DD'
            },
            {
              key: 'endDate',
              label: '结束日期',
              type: 'string',
              placeholder: 'YYYY-MM-DD'
            },
            {
              key: 'leaveReason',
              label: '请假原因',
              type: 'text',
              placeholder: '请详细描述请假原因'
            }
          )
        }
        
        // 入职流程相关字段
        if (this.task.name && (this.task.name.includes('入职') || this.task.name.includes('新员工'))) {
          this.dynamicFormFields.push(
            {
              key: 'employeeName',
              label: '员工姓名',
              type: 'string',
              placeholder: '请输入员工姓名'
            },
            {
              key: 'employeeId',
              label: '员工编号',
              type: 'string',
              placeholder: '请输入员工编号'
            },
            {
              key: 'position',
              label: '职位',
              type: 'string',
              placeholder: '请输入职位'
            },
            {
              key: 'department',
              label: '部门',
              type: 'select',
              options: [
                { label: '人力资源部', value: 'hr' },
                { label: '技术部', value: 'tech' },
                { label: '财务部', value: 'finance' },
                { label: '市场部', value: 'marketing' },
                { label: '行政部', value: 'admin' }
              ]
            },
            {
              key: 'startDate',
              label: '入职日期',
              type: 'string',
              placeholder: 'YYYY-MM-DD'
            },
            {
              key: 'manager',
              label: '直接上级',
              type: 'string',
              placeholder: '请输入直接上级姓名'
            }
          )
        }
        
        // 通用审批流程字段
        if (this.task.name && (this.task.name.includes('审批') || this.task.name.includes('申请')) &&
            !this.task.name.includes('请假') && !this.task.name.includes('入职')) {
          this.dynamicFormFields.push(
            {
              key: 'requestTitle',
              label: '申请标题',
              type: 'string',
              placeholder: '请输入申请标题'
            },
            {
              key: 'requestAmount',
              label: '申请金额',
              type: 'string',
              placeholder: '请输入申请金额（如有）'
            },
            {
              key: 'requestContent',
              label: '申请内容',
              type: 'text',
              placeholder: '请详细描述申请内容'
            },
            {
              key: 'urgency',
              label: '紧急程度',
              type: 'select',
              options: [
                { label: '一般', value: 'normal' },
                { label: '紧急', value: 'urgent' },
                { label: '非常紧急', value: 'very_urgent' }
              ]
            }
          )
        }
      }
      
      // 初始化表单数据
      this.dynamicFormFields.forEach(field => {
        if (!this.taskForm[field.key]) {
          // Vue 3中直接赋值即可触发响应式更新，不需要this.$set
          this.taskForm[field.key] = field.defaultValue || ''
        }
      })
    },
    
    async updateTask() {
      try {
        const taskId = this.$route.params.id
        
        const updatedTask = {
          name: this.editTaskForm.name,
          description: this.editTaskForm.description,
          priority: this.editTaskForm.priority,
          dueDate: this.editTaskForm.dueDate ? this.editTaskForm.dueDate.toISOString() : null
        }
        
        // 使用TaskService更新任务
        const success = await TaskService.updateTask(taskId, updatedTask)
        
        if (success) {
          this.showTaskEditForm = false
          this.loadTaskDetail()
        }
      } catch (error) {
        console.error('更新任务错误:', error)
        // TaskService内部已经处理了错误提示
      }
    },
    
    async handleCompleteTask() {
      this.loading = true
      try {
        const taskId = this.$route.params.id
        
        // 分离审批结果和其他字段
        const { approve, comment, ...additionalVariables } = this.taskForm
        
        // 使用TaskService进行任务审批
        const success = await TaskService.approveTask(
          taskId, 
          approve, 
          comment, 
          additionalVariables
        )
        
        if (success) {
          this.completed = true
        }
      } catch (error) {
        console.error('完成任务错误:', error)
        // TaskService内部已经处理了错误提示
      } finally {
        this.loading = false
      }
    },
    
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN')
    },
    
    getPriorityType(priority) {
      priority = priority || 50
      if (priority >= 80) return 'danger'
      if (priority >= 50) return 'warning'
      return 'success'
    },
    
    isOverdue(dueDate) {
      if (!dueDate) return false
      return new Date(dueDate) < new Date()
    },
    
    async claimTask() {
      this.loading = true
      try {
        const taskId = this.$route.params.id
        // 使用TaskService认领任务
        const success = await TaskService.claimTask(taskId)
        
        if (success) {
          this.loadTaskDetail()
        }
      } catch (error) {
        console.error('认领任务错误:', error)
        // TaskService内部已经处理了错误提示
      } finally {
        this.loading = false
      }
    },
    
    unclaimTask() {
      this.$confirm('确定要取消认领该任务吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        this.loading = true
        try {
          const taskId = this.$route.params.id
          // 使用TaskService取消认领任务
          const success = await TaskService.unclaimTask(taskId)
          
          if (success) {
            this.loadTaskDetail()
          }
        } catch (error) {
          console.error('取消认领任务错误:', error)
          ElMessage.error('取消认领失败: ' + (error.response?.data?.message || error.message))
        } finally {
          this.loading = false
        }
      }).catch(() => {
        // 取消操作
      })
    },
    
    delegateTask() {
      this.delegateForm.taskId = this.task.id
      this.delegateForm.taskName = this.task.name
      this.delegateForm.assignee = ''
      this.delegateDialogVisible = true
    },
    
    async confirmDelegate() {
      if (!this.delegateForm.assignee) {
        ElMessage.warning('请输入委派的用户名')
        return
      }
      
      this.loading = true
      try {
        // 使用TaskService委派任务
        const success = await TaskService.delegateTask(
          this.delegateForm.taskId, 
          this.delegateForm.assignee
        )
        
        if (success) {
          this.delegateDialogVisible = false
          this.loadTaskDetail()
        }
      } catch (error) {
        console.error('委派任务错误:', error)
        // TaskService内部已经处理了错误提示
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.task-detail-container {
  padding: 20px;
}

.task-detail-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.action-buttons {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}

.action-buttons button {
  margin-right: 10px;
}

.overdue {
  color: #f56c6c;
  font-weight: bold;
}

.comments-container {
  margin-top: 15px;
  max-height: 300px;
  overflow-y: auto;
}

.comment-item {
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}

.comment-author {
  font-weight: bold;
}

.comment-time {
  color: #909399;
  font-size: 12px;
}

.comment-content {
  line-height: 1.5;
}

.add-comment-form {
  margin-top: 15px;
}

/* 流程历史记录样式 */
.history-container {
  margin-top: 15px;
  max-height: 400px;
  overflow-y: auto;
}

.history-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.history-item:last-child {
  border-bottom: none;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-weight: bold;
}

.history-activity {
  color: #409eff;
}

.history-time {
  color: #909399;
  font-size: 12px;
  font-weight: normal;
}

.history-content {
  padding-left: 10px;
  border-left: 8px solid #e6f7ff;
}

.history-assignee {
  margin: 4px 0;
  color: #606266;
}

.history-comment {
  margin: 4px 0;
  color: #606266;
  font-style: italic;
}

.history-action {
  margin: 4px 0;
  color: #67c23a;
}
</style>