<template>
  <div class="task-list-container">
    <el-card shadow="never" class="main-card">
      <template #header>
        <div class="card-header">
          <h2>待办任务</h2>
        </div>
      </template>
      
      <!-- 任务统计卡片 -->
      <div class="task-stats">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-number">{{ totalTasks }}</div>
            <div class="stat-label">全部任务</div>
          </div>
        </el-card>
        <el-card class="stat-card" shadow="hover" type="primary">
          <div class="stat-content">
            <div class="stat-number">{{ urgentTasks }}</div>
            <div class="stat-label">紧急任务</div>
          </div>
        </el-card>
        <el-card class="stat-card" shadow="hover" type="warning">
          <div class="stat-content">
            <div class="stat-number">{{ overdueTasks }}</div>
            <div class="stat-label">逾期任务</div>
          </div>
        </el-card>
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-number">{{ unclaimedTasks }}</div>
            <div class="stat-label">待认领任务</div>
          </div>
        </el-card>
      </div>

      <!-- 过滤和搜索区域 -->
      <div class="filter-section">
        <el-input v-model="searchQuery" placeholder="搜索任务名称" clearable class="search-input"></el-input>
        
        <el-select v-model="processTypeFilter" placeholder="流程类型" clearable class="filter-select">
          <el-option label="全部" value=""></el-option>
          <el-option label="请假申请" value="leave"></el-option>
          <el-option label="入职流程" value="onboarding"></el-option>
          <el-option label="通用审批" value="approval"></el-option>
        </el-select>
        
        <el-select v-model="priorityFilter" placeholder="优先级" clearable class="filter-select">
          <el-option label="全部" value=""></el-option>
          <el-option label="高" value="50"></el-option>
          <el-option label="中" value="30"></el-option>
          <el-option label="低" value="10"></el-option>
        </el-select>
        
        <el-button type="primary" @click="refreshTasks" class="refresh-btn">刷新</el-button>
      </div>
      
      <!-- 任务表格 -->
      <el-table 
        :data="filteredTasks" 
        style="width: 100%" 
        @row-click="handleRowClick"
        row-class-name="task-row"
        :cell-style="{ padding: '12px 0' }"
      >
        <el-table-column prop="name" label="任务名称" min-width="240">
          <template #default="scope">
            <div class="task-name">
              <el-tag 
                v-if="getTaskType(scope.row.name)" 
                size="small" 
                :type="getTaskTypeColor(scope.row.name)"
                class="task-type-tag"
              >
                {{ getTaskType(scope.row.name) }}
              </el-tag>
              <span class="task-title">{{ scope.row.name }}</span>
            </div>
            <div class="task-description" v-if="getTaskDescription(scope.row)">
              {{ getTaskDescription(scope.row) }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="assignee" label="负责人" min-width="120">
          <template #default="scope">
            <span v-if="scope.row.assignee" class="assignee-text">{{ scope.row.assignee }}</span>
            <el-tag v-else size="small" type="info">待认领</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="priority" label="优先级" min-width="100">
          <template #default="scope">
            <el-tag :type="getPriorityTagType(scope.row.priority)">
              {{ getPriorityText(scope.row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="created" label="创建时间" min-width="160">
          <template #default="scope">
            {{ formatDate(scope.row.created) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="dueDate" label="截止日期" min-width="160">
          <template #default="scope">
            <span :class="{ 'overdue': isOverdue(scope.row.dueDate) }">
              {{ scope.row.dueDate ? formatDate(scope.row.dueDate) : '无' }}
            </span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" min-width="140" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click.stop="viewTask(scope.row.id)">查看详情</el-button>
            <el-button 
              v-if="!scope.row.assignee" 
              type="success" 
              size="small" 
              @click.stop="claimTask(scope.row.id)"
            >
              认领
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页控件 -->
      <div class="pagination" v-if="total > 0">
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
      
      <!-- 错误状态 -->
      <div v-if="error" class="error-state">
        <el-alert
          :title="error"
          type="error"
          show-icon
          :closable="false"
        >
          <template #desc>
            <div>
              <span>请检查网络连接或稍后重试</span>
              <el-button type="text" @click="refreshTasks" class="retry-btn">重试</el-button>
            </div>
          </template>
        </el-alert>
      </div>
      
      <!-- 空状态 -->
      <div v-else-if="total === 0" class="empty-state">
        <el-empty description="暂无待办任务" />
      </div>
    </el-card>
  </div>
</template>

<script>
import TaskService from '../services/TaskService'

export default {
  name: 'TaskList',
  data() {
    return {
      tasks: [],
      searchQuery: '',
      processTypeFilter: '',
      priorityFilter: '',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      loading: false,
      error: null
    }
  },
  created() {
    this.refreshTasks()
  },
  inject: ['showNotification', 'showLoading', 'hideLoading'],
  watch: {
    // 监听筛选条件变化，重置页码
    searchQuery() {
      this.currentPage = 1
    },
    processTypeFilter() {
      this.currentPage = 1
    },
    priorityFilter() {
      this.currentPage = 1
    }
  },
  computed: {
    filteredTasks() {
      let filtered = [...this.tasks]
      
      // 搜索过滤
      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase()
        filtered = filtered.filter(task => 
          task.name.toLowerCase().includes(query)
        )
      }
      
      // 流程类型过滤
      if (this.processTypeFilter) {
        if (this.processTypeFilter === 'leave') {
          filtered = filtered.filter(task => 
            task.name && task.name.includes('请假')
          )
        } else if (this.processTypeFilter === 'onboarding') {
          filtered = filtered.filter(task => 
            task.name && (task.name.includes('入职') || task.name.includes('新员工'))
          )
        } else if (this.processTypeFilter === 'approval') {
          filtered = filtered.filter(task => 
            task.name && (task.name.includes('审批') || task.name.includes('申请')) &&
            !task.name.includes('请假') && !task.name.includes('入职')
          )
        }
      }
      
      // 优先级过滤
      if (this.priorityFilter) {
        filtered = filtered.filter(task => task.priority === parseInt(this.priorityFilter))
      }
      
      return filtered
    },
    
    // 任务统计
    totalTasks() {
      return this.tasks.length
    },
    
    urgentTasks() {
      return this.tasks.filter(task => task.priority === 50 || task.priority === 100).length
    },
    
    overdueTasks() {
      return this.tasks.filter(task => this.isOverdue(task.dueDate)).length
    },
    
    unclaimedTasks() {
      return this.tasks.filter(task => !task.assignee).length
    }
  },
  methods: {
    async refreshTasks() {
      this.loading = true
      this.error = null
      
      // 使用全局加载状态
      if (this.showLoading) {
        this.showLoading()
      }
      
      try {
        // 构建查询参数
        const params = {
          page: this.currentPage,
          size: this.pageSize
        }
        
        // 添加筛选条件
        if (this.processTypeFilter) {
          params.processType = this.processTypeFilter
        }
        
        if (this.priorityFilter) {
          params.priority = this.priorityFilter
        }
        
        if (this.searchQuery) {
          params.nameLike = this.searchQuery
        }
        
        const result = await TaskService.getTasks(params)
        
        this.tasks = result.data || []
        this.total = result.total || 0
        
        // 显示成功通知
        if (this.showNotification) {
          this.showNotification('刷新成功', '任务列表已更新', 'success')
        }
      } catch (error) {
        console.error('获取任务列表失败:', error)
        this.error = error.message || '获取任务列表失败，请重试'
        
        // 使用全局通知或组件内通知
        if (this.showNotification) {
          this.showNotification('获取任务失败', this.error, 'error')
        } else if (this.$message) {
          this.$message.error(this.error)
        }
      } finally {
        this.loading = false
        if (this.hideLoading) {
          this.hideLoading()
        }
      }
    },
    
    viewTask(taskId) {
      this.$router.push(`/tasks/${taskId}`)
    },
    
    handleRowClick(row) {
      this.viewTask(row.id)
    },
    
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
      this.refreshTasks()
    },
    
    handleCurrentChange(current) {
      this.currentPage = current
      this.refreshTasks()
    },
    
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },
    
    isOverdue(dueDate) {
      if (!dueDate) return false
      const now = new Date()
      const due = new Date(dueDate)
      return now > due
    },
    
    getPriorityTagType(priority) {
      switch (priority) {
        case 100:
          return 'danger'
        case 50:
          return 'warning'
        case 30:
          return 'info'
        default:
          return 'default'
      }
    },
    
    getPriorityText(priority) {
      switch (priority) {
        case 100:
          return '最高'
        case 50:
          return '高'
        case 30:
          return '中'
        case 10:
          return '低'
        default:
          return '普通'
      }
    },
    
    // 获取任务类型 - 添加安全检查
    getTaskType(taskName) {
      // 确保taskName存在且为字符串
      if (!taskName || typeof taskName !== 'string') {
        return null
      }
      
      if (taskName.includes('请假')) {
        return '请假申请'
      } else if (taskName.includes('入职') || taskName.includes('新员工')) {
        return '入职流程'
      } else if (taskName.includes('审批') || taskName.includes('申请')) {
        return '通用审批'
      }
      return null
    },
    
    // 获取任务类型对应的颜色 - 添加安全检查
    getTaskTypeColor(taskName) {
      // 确保taskName存在且为字符串
      if (!taskName || typeof taskName !== 'string') {
        return 'info'
      }
      
      if (taskName.includes('请假')) {
        return 'primary'
      } else if (taskName.includes('入职') || taskName.includes('新员工')) {
        return 'success'
      } else if (taskName.includes('审批') || taskName.includes('申请')) {
        return 'warning'
      }
      return 'info'
    },
    
    // 获取任务描述信息 - 添加安全检查
    getTaskDescription(task) {
      // 确保task存在且有name属性
      if (!task || !task.name || typeof task.name !== 'string') {
        return ''
      }
      
      // 根据任务名称返回描述信息
      if (task.name.includes('请假')) {
        return '请假相关任务，需要及时处理'
      } else if (task.name.includes('入职') || task.name.includes('新员工')) {
        return '新员工入职流程，请尽快审批'
      }
      return ''
    },
    
    // 认领任务
    async claimTask(taskId) {
      try {
        // 显示加载状态
        if (this.showLoading) {
          this.showLoading()
        }
        
        const success = await TaskService.claimTask(taskId)
        if (success) {
          // 显示成功通知
          if (this.showNotification) {
            this.showNotification('认领成功', '任务已成功认领', 'success')
          } else if (this.$message) {
            this.$message.success('任务已成功认领')
          }
          this.refreshTasks()
        }
      } catch (error) {
        console.error('认领任务失败:', error)
        const errorMsg = error.message || '认领任务失败，请重试'
        
        // 显示错误通知
        if (this.showNotification) {
          this.showNotification('认领失败', errorMsg, 'error')
        } else if (this.$message) {
          this.$message.error(errorMsg)
        }
      } finally {
        if (this.hideLoading) {
          this.hideLoading()
        }
      }
    }
  }
}
</script>

<style scoped>
.task-list-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.main-card {
  border-radius: 8px;
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.task-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  flex: 1;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12) !important;
}

.stat-content {
  text-align: center;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  margin-top: 4px;
}

.filter-section {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.search-input {
  width: 300px;
}

.filter-select {
  width: 180px;
}

.task-row:hover {
  background-color: #f5f7fa;
}

.task-name {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.task-title {
  font-weight: 500;
  color: #303133;
}

.task-type-tag {
  margin-bottom: 0;
}

.task-description {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.assignee-text {
  color: #606266;
}

.overdue {
  color: #f56c6c;
  font-weight: 500;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

.error-state {
  margin-top: 20px;
}

.retry-btn {
  margin-left: 10px;
  color: #409eff;
}

/* 加载状态覆盖 */
.task-list-container .el-loading-mask {
  background-color: rgba(255, 255, 255, 0.8);
}

/* 表格加载状态 */
.el-table__empty-block,
.el-table__placeholder {
  min-height: 200px;
}

@media (max-width: 1200px) {
  .task-stats {
    flex-wrap: wrap;
  }
  
  .stat-card {
    flex-basis: calc(50% - 8px);
  }
}

@media (max-width: 768px) {
  .task-list-container {
    padding: 16px;
  }
  
  .stat-card {
    flex-basis: 100%;
  }
  
  .filter-section {
    flex-direction: column;
  }
  
  .search-input,
  .filter-select {
    width: 100%;
  }
}
</style>