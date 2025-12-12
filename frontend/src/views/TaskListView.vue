<template>
  <div class="task-list-container">
    <el-page-header
      @back="handleBack"
      content="任务管理"
    />
    
    <el-card shadow="hover" class="task-card">
      <!-- 搜索和筛选区域 -->
      <div class="search-filter-container">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="searchForm.taskName"
              placeholder="任务名称"
              clearable
              @keyup.enter="searchTasks"
            />
          </el-col>
          <el-col :span="6">
            <el-input
              v-model="searchForm.assignee"
              placeholder="办理人"
              clearable
              @keyup.enter="searchTasks"
            />
          </el-col>
          <el-col :span="6">
            <el-select v-model="searchForm.status" placeholder="任务状态" clearable>
              <el-option label="未认领" value="unclaimed" />
              <el-option label="已认领" value="claimed" />
              <el-option label="已完成" value="completed" />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-button type="primary" @click="searchTasks">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-col>
        </el-row>
      </div>

      <!-- 任务列表 -->
      <el-table 
        :data="tasks" 
        style="width: 100%"
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="任务ID" width="180" />
        <el-table-column prop="name" label="任务名称" />
        <el-table-column prop="processInstanceId" label="流程实例ID" width="180" />
        <el-table-column prop="assignee" label="办理人" width="120">
          <template #default="scope">
            <span v-if="scope.row.assignee">{{ scope.row.assignee }}</span>
            <span v-else style="color: #909399;">未认领</span>
          </template>
        </el-table-column>
        <el-table-column prop="owner" label="拥有者" width="120" />
        <el-table-column prop="priority" label="优先级" width="80">
          <template #default="scope">
            <el-tag :type="getPriorityType(scope.row.priority)">
              {{ scope.row.priority || 50 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" :formatter="formatDate" />
        <el-table-column prop="dueDate" label="截止时间" width="180">
          <template #default="scope">
            <span v-if="scope.row.dueDate" :class="{ 'overdue': isOverdue(scope.row.dueDate) }">
              {{ formatDate(null, null, scope.row.dueDate) }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              text
              size="small"
              @click="handleTaskDetail(scope.row.id)"
            >
              查看
            </el-button>
            <el-button 
              v-if="!scope.row.assignee || scope.row.assignee === currentUsername"
              size="small" 
              @click="claimTask(scope.row.id)"
            >
              认领
            </el-button>
            <el-button 
              v-if="scope.row.assignee === currentUsername"
              size="small" 
              @click="unclaimTask(scope.row.id)"
            >
              取消认领
            </el-button>
            <el-button 
              v-if="scope.row.assignee === currentUsername"
              size="small" 
              @click="delegateTask(scope.row)"
              type="warning"
            >
              委派
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 批量操作按钮 -->
      <div class="batch-operations" v-if="selectedTasks.length > 0">
        <el-button @click="batchClaim" size="small">批量认领</el-button>
        <el-button @click="batchUnclaim" size="small">批量取消认领</el-button>
        <el-button type="warning" @click="batchDelegate" size="small">批量委派</el-button>
      </div>
      
      <!-- 分页控件 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

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

    <!-- 批量委派对话框 -->
    <el-dialog
      title="批量委派任务"
      v-model="batchDelegateDialogVisible"
      width="400px"
    >
      <el-form :model="batchDelegateForm" label-width="80px">
        <el-form-item label="任务数量">
          <el-input v-model="selectedTasks.length" disabled />
        </el-form-item>
        <el-form-item label="委派给" prop="assignee">
          <el-input 
            v-model="batchDelegateForm.assignee" 
            placeholder="请输入委派的用户名"
            required
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDelegateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBatchDelegate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import axios from '../utils/axiosConfig'
import { ElMessage } from 'element-plus'
import TaskService from '../services/TaskService'

export default {
  name: 'TaskListView',
  data() {
    return {
      tasks: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      currentUsername: localStorage.getItem('username') || '',
      searchForm: {
        taskName: '',
        assignee: '',
        status: ''
      },
      selectedTasks: [],
      delegateDialogVisible: false,
      delegateForm: {
        taskId: '',
        taskName: '',
        assignee: ''
      },
      batchDelegateDialogVisible: false,
      batchDelegateForm: {
        assignee: ''
      }
    }
  },
  mounted() {
    this.loadTasks()
  },
  methods: {
    handleBack() {
      this.$router.push('/dashboard')
    },
    
    // 加载任务列表
    async loadTasks() {
      try {
        const params = {
          page: this.currentPage,
          size: this.pageSize
        }
        
        // 添加搜索条件
        if (this.searchForm.taskName) {
          params.name = this.searchForm.taskName
        }
        if (this.searchForm.assignee) {
          params.assignee = this.searchForm.assignee
        }
        if (this.searchForm.status) {
          params.status = this.searchForm.status
        }
        
        console.log('加载任务列表，参数:', params)
        // 使用TaskService加载任务
        const response = await TaskService.getTasks(this.currentPage, this.pageSize, params)
        
        console.log('任务列表加载响应:', response)
        
        // 优化响应处理逻辑，适应TaskService的返回格式
        if (response) {
          // 直接从response获取数据，因为TaskService已经内部处理了多种格式
          this.tasks = Array.isArray(response.data) ? response.data : []
          this.total = response.total || 0
          
          // 记录数据加载情况
          console.log(`成功加载 ${this.tasks.length} 个任务，总共 ${this.total} 个任务`)
          
          // 如果没有数据但响应成功，可以给用户一个提示
          if (this.tasks.length === 0 && this.total === 0) {
            console.log('当前条件下没有找到任务')
          }
        } else {
          ElMessage.error('获取任务列表失败：无响应数据')
          this.tasks = []
          this.total = 0
        }
      } catch (error) {
        console.error('获取任务列表错误:', error)
        console.error('错误详情:', error.response ? JSON.stringify(error.response.data) : error.message)
        ElMessage.error('获取任务列表失败，请检查网络或联系管理员')
        this.tasks = []
        this.total = 0
      }
    },
    
    // 搜索任务
    searchTasks() {
      this.currentPage = 1
      this.loadTasks()
    },
    
    // 重置搜索条件
    resetSearch() {
      this.searchForm = {
        taskName: '',
        assignee: '',
        status: ''
      }
      this.loadTasks()
    },
    
    handleSizeChange(size) {
      this.pageSize = size
      this.loadTasks()
    },
    
    handleCurrentChange(current) {
      this.currentPage = current
      this.loadTasks()
    },
    
    handleTaskDetail(taskId) {
      this.$router.push({ name: 'taskDetail', params: { id: taskId } })
    },
    
    formatDate(row, column, cellValue) {
      // 处理不同调用方式：1. 作为formatter: formatDate(row, column, cellValue)
      // 2. 直接调用: formatDate(null, null, dateValue)
      const dateValue = cellValue || column || row
      if (!dateValue) return ''
      const date = new Date(dateValue)
      return date.toLocaleString('zh-CN')
    },
    
    // 获取优先级样式
    getPriorityType(priority) {
      priority = priority || 50
      if (priority >= 80) return 'danger'
      if (priority >= 50) return 'warning'
      return 'success'
    },
    
    // 判断是否逾期
    isOverdue(dueDate) {
      if (!dueDate) return false
      return new Date(dueDate) < new Date()
    },
    
    // 选择任务处理
    handleSelectionChange(selection) {
      this.selectedTasks = selection
    },
    
    // 认领任务
    async claimTask(taskId) {
      try {
        const response = await TaskService.claimTask(taskId)
        if (response && response.success !== false) {
          ElMessage.success('任务认领成功')
          this.loadTasks()
        } else {
          ElMessage.error(response.message || '任务认领失败')
        }
      } catch (error) {
        console.error('认领任务错误:', error)
        ElMessage.error('任务认领失败: ' + (error.response?.data?.message || error.message))
      }
    },
    
    // 取消认领任务
    unclaimTask(taskId) {
      this.$confirm('确定要取消认领该任务吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await TaskService.unclaimTask(taskId)
          if (response && response.success !== false) {
            ElMessage.success('取消认领成功')
            this.loadTasks()
          } else {
            ElMessage.error(response.message || '取消认领失败')
          }
        } catch (error) {
          console.error('取消认领任务错误:', error)
          ElMessage.error('取消认领失败: ' + (error.response?.data?.message || error.message))
        }
      }).catch(() => {
        // 取消操作
      })
    },
    
    // 打开委派任务对话框
    delegateTask(task) {
      this.delegateForm.taskId = task.id
      this.delegateForm.taskName = task.name
      this.delegateForm.assignee = ''
      this.delegateDialogVisible = true
    },
    
    // 确认委派任务
    async confirmDelegate() {
      if (!this.delegateForm.assignee) {
        ElMessage.warning('请输入委派的用户名')
        return
      }
      
      try {
        const response = await TaskService.delegateTask(this.delegateForm.taskId, this.delegateForm.assignee)
        if (response && response.success !== false) {
          ElMessage.success('任务委派成功')
          this.delegateDialogVisible = false
          this.loadTasks()
        } else {
          ElMessage.error(response.message || '任务委派失败')
        }
      } catch (error) {
        console.error('委派任务错误:', error)
        ElMessage.error('任务委派失败: ' + (error.response?.data?.message || error.message))
      }
    },
    
    // 批量认领任务
    batchClaim() {
      this.$confirm(`确定要认领选中的 ${this.selectedTasks.length} 个任务吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const taskIds = this.selectedTasks.map(task => task.id)
          const response = await TaskService.batchClaim(taskIds)
          if (response && response.success !== false) {
            ElMessage.success('批量认领成功')
            this.selectedTasks = []
            this.loadTasks()
          } else {
            ElMessage.error(response.message || '批量认领失败')
          }
        } catch (error) {
          console.error('批量认领任务错误:', error)
          ElMessage.error('批量认领失败: ' + (error.response?.data?.message || error.message))
        }
      }).catch(() => {
        // 取消操作
      })
    },
    
    // 批量取消认领任务
    batchUnclaim() {
      this.$confirm(`确定要取消认领选中的 ${this.selectedTasks.length} 个任务吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const taskIds = this.selectedTasks.map(task => task.id)
          const response = await TaskService.batchUnclaim(taskIds)
          if (response && response.success !== false) {
            ElMessage.success('批量取消认领成功')
            this.selectedTasks = []
            this.loadTasks()
          } else {
            ElMessage.error(response.message || '批量取消认领失败')
          }
        } catch (error) {
          console.error('批量取消认领任务错误:', error)
          ElMessage.error('批量取消认领失败: ' + (error.response?.data?.message || error.message))
        }
      }).catch(() => {
        // 取消操作
      })
    },
    
    // 打开批量委派对话框
    batchDelegate() {
      this.batchDelegateForm.assignee = ''
      this.batchDelegateDialogVisible = true
    },
    
    // 确认批量委派
    confirmBatchDelegate() {
      if (!this.batchDelegateForm.assignee) {
        ElMessage.warning('请输入委派的用户名')
        return
      }
      
      this.$confirm(`确定要将选中的 ${this.selectedTasks.length} 个任务委派给 ${this.batchDelegateForm.assignee} 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const taskIds = this.selectedTasks.map(task => task.id)
          const response = await TaskService.batchDelegate(taskIds, this.batchDelegateForm.assignee)
          if (response && response.success !== false) {
            ElMessage.success('批量委派成功')
            this.batchDelegateDialogVisible = false
            this.selectedTasks = []
            this.loadTasks()
          } else {
            ElMessage.error(response.message || '批量委派失败')
          }
        } catch (error) {
          console.error('批量委派任务错误:', error)
          ElMessage.error('批量委派失败: ' + (error.response?.data?.message || error.message))
        }
      }).catch(() => {
        // 取消操作
      })
    }
  }
}
</script>

<style scoped>
.task-list-container {
  padding: 20px;
}

.task-card {
  margin-top: 20px;
}

.search-filter-container {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.batch-operations {
  margin-top: 15px;
}

.batch-operations .el-button {
  margin-right: 10px;
}

.overdue {
  color: #f56c6c;
  font-weight: bold;
}
</style>