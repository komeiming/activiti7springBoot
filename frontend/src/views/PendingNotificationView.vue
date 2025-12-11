<template>
  <div class="pending-notification-view">
    <div class="page-header">
      <h1>待发送通知管理</h1>
    </div>

    <div class="content-container">
      <!-- 查询条件 -->
      <el-card class="search-card">
        <el-form :model="searchForm" :inline="true" label-width="80px">
          <el-form-item label="通知类型">
            <el-select v-model="searchForm.type" placeholder="请选择通知类型">
              <el-option label="全部" value=""></el-option>
              <el-option label="邮件" value="EMAIL"></el-option>
              <el-option label="短信" value="SMS"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态">
              <el-option label="全部" value=""></el-option>
              <el-option label="待发送" value="pending"></el-option>
              <el-option label="发送中" value="sending"></el-option>
              <el-option label="已发送" value="sent"></el-option>
              <el-option label="发送失败" value="failed"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="接收者">
            <el-input v-model="searchForm.receiver" placeholder="请输入接收者" clearable></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button type="primary" @click="handleBatchProcess">批量处理</el-button>
        <el-button type="danger" @click="handleBatchDelete" :disabled="selectedIds.length === 0">批量删除</el-button>
      </div>

      <!-- 待发送通知列表 -->
      <el-card class="table-card">
        <el-table
          v-loading="loading"
          :data="pendingNotifications"
          style="width: 100%"
          @selection-change="handleSelectionChange"
          row-key="id"
        >
          <el-table-column type="selection" width="55"></el-table-column>
          <el-table-column prop="id" label="ID" width="180"></el-table-column>
          <el-table-column prop="templateName" label="模板名称"></el-table-column>
          <el-table-column prop="type" label="通知类型" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.type === 'EMAIL' ? 'success' : 'warning'">
                {{ scope.row.type === 'EMAIL' ? '邮件' : '短信' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="receiver" label="接收者"></el-table-column>
          <el-table-column prop="subject" label="邮件主题" v-if="searchForm.type === 'EMAIL' || !searchForm.type"></el-table-column>
          <el-table-column prop="priority" label="优先级" width="80">
            <template #default="scope">
              <el-tag :type="getPriorityType(scope.row.priority)">
                {{ getPriorityText(scope.row.priority) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="retryCount" label="重试次数" width="80"></el-table-column>
          <el-table-column prop="maxRetries" label="最大重试" width="80"></el-table-column>
          <el-table-column prop="nextRetryTime" label="下次重试时间" width="180"></el-table-column>
          <el-table-column prop="createdTime" label="创建时间" width="180"></el-table-column>
          <el-table-column prop="updatedTime" label="更新时间" width="180"></el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button type="primary" size="small" @click="handleProcess(scope.row.id)" :disabled="scope.row.status !== 'pending'">处理</el-button>
              <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
              <el-button type="info" size="small" @click="handleView(scope.row)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="pagination.currentPage"
            :page-sizes="[10, 20, 50, 100]"
            :page-size="pagination.pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="pagination.total"
          ></el-pagination>
        </div>
      </el-card>
    </div>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="待发送通知详情"
      width="60%"
    >
      <el-descriptions v-if="selectedNotification" :column="1" border>
        <el-descriptions-item label="ID">{{ selectedNotification.id }}</el-descriptions-item>
        <el-descriptions-item label="模板ID">{{ selectedNotification.templateId || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="模板名称">{{ selectedNotification.templateName || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="通知类型">{{ selectedNotification.type === 'EMAIL' ? '邮件' : (selectedNotification.type === 'SMS' ? '短信' : '未知') }}</el-descriptions-item>
        <el-descriptions-item label="接收者">{{ selectedNotification.receiver || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="邮件主题" v-if="selectedNotification.type === 'EMAIL'">
          {{ selectedNotification.subject || '无' }}
        </el-descriptions-item>
        <el-descriptions-item label="内容" v-if="selectedNotification.type === 'EMAIL'">
          <div v-html="selectedNotification.content || ''"></div>
        </el-descriptions-item>
        <el-descriptions-item label="内容" v-else>
          {{ selectedNotification.content || '无' }}
        </el-descriptions-item>
        <el-descriptions-item label="参数">
          <pre>{{ JSON.stringify(selectedNotification.params || {}, null, 2) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="调用系统">{{ selectedNotification.senderSystem || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="请求ID">{{ selectedNotification.requestId || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="优先级">{{ getPriorityText(selectedNotification.priority) }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ getStatusText(selectedNotification.status) }}</el-descriptions-item>
        <el-descriptions-item label="重试次数">{{ selectedNotification.retryCount || 0 }}/{{ selectedNotification.maxRetries || 0 }}</el-descriptions-item>
        <el-descriptions-item label="下次重试时间">{{ selectedNotification.nextRetryTime || '无' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ selectedNotification.createdTime || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ selectedNotification.updatedTime || '未知' }}</el-descriptions-item>
      </el-descriptions>
      <div v-else>
        <el-empty description="没有找到通知详情"></el-empty>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import PendingNotificationService from '../services/PendingNotificationService';

export default {
  name: 'PendingNotificationView',
  data() {
    return {
      loading: false,
      pendingNotifications: [],
      searchForm: {
        type: '',
        status: '',
        receiver: ''
      },
      pagination: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
      selectedIds: [],
      dialogVisible: false,
      selectedNotification: null
    };
  },
  mounted() {
    this.loadPendingNotifications();
  },
  methods: {
    // 加载待发送通知列表
    async loadPendingNotifications() {
      this.loading = true;
      try {
        const params = {
          ...this.searchForm,
          page: this.pagination.currentPage,
          pageSize: this.pagination.pageSize
        };
        const response = await PendingNotificationService.getPendingNotifications(params);
        
        // 兼容不同的响应格式 - 避免访问undefined的list属性
        let notifications = [];
        let total = 0;
        
        if (typeof response === 'object' && response) {
          if (Array.isArray(response.list)) {
            // 标准分页格式: { list: [...], total: ... }
            notifications = response.list;
            total = response.total || 0;
          } else if (Array.isArray(response.data)) {
            // 直接返回数据数组
            notifications = response.data;
            total = response.total || response.data.length;
          } else if (response.data && typeof response.data === 'object') {
            // 嵌套格式: { data: { list: [...], total: ... } }
            if (Array.isArray(response.data.list)) {
              notifications = response.data.list;
              total = response.data.total || 0;
            } else if (Array.isArray(response.data)) {
              // 另一种嵌套格式: { data: [...] }
              notifications = response.data;
              total = response.data.length;
            }
          } else if (Array.isArray(response)) {
            // 直接返回数组
            notifications = response;
            total = response.length;
          }
        } else if (Array.isArray(response)) {
          // 直接返回数组
          notifications = response;
          total = response.length;
        }
        
        this.pendingNotifications = notifications;
        this.pagination.total = total;
      } catch (error) {
        this.$message.error('加载待发送通知列表失败');
        console.error('加载待发送通知列表失败:', error);
      } finally {
        this.loading = false;
      }
    },

    // 查询
    handleSearch() {
      this.pagination.currentPage = 1;
      this.loadPendingNotifications();
    },

    // 重置
    handleReset() {
      this.searchForm = {
        type: '',
        status: '',
        receiver: ''
      };
      this.pagination.currentPage = 1;
      this.loadPendingNotifications();
    },

    // 分页大小变化
    handleSizeChange(size) {
      this.pagination.pageSize = size;
      this.loadPendingNotifications();
    },

    // 当前页变化
    handleCurrentChange(current) {
      this.pagination.currentPage = current;
      this.loadPendingNotifications();
    },

    // 选择变化
    handleSelectionChange(selection) {
      this.selectedIds = selection.map(item => item.id);
    },

    // 批量处理
    async handleBatchProcess() {
      try {
        await PendingNotificationService.processPendingNotifications();
        this.$message.success('批量处理成功');
        this.loadPendingNotifications();
      } catch (error) {
        this.$message.error('批量处理失败');
      }
    },

    // 批量删除
    async handleBatchDelete() {
      this.$confirm('确定要删除选中的待发送通知吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await PendingNotificationService.deletePendingNotifications(this.selectedIds);
          this.$message.success('批量删除成功');
          this.loadPendingNotifications();
          this.selectedIds = [];
        } catch (error) {
          this.$message.error('批量删除失败');
        }
      }).catch(() => {
        // 取消删除
      });
    },

    // 处理待发送通知
    async handleProcess(id) {
      try {
        await PendingNotificationService.processPendingNotification(id);
        this.$message.success('处理成功');
        this.loadPendingNotifications();
      } catch (error) {
        this.$message.error('处理失败');
      }
    },

    // 删除待发送通知
    async handleDelete(id) {
      this.$confirm('确定要删除这条待发送通知吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await PendingNotificationService.deletePendingNotification(id);
          this.$message.success('删除成功');
          this.loadPendingNotifications();
        } catch (error) {
          this.$message.error('删除失败');
        }
      }).catch(() => {
        // 取消删除
      });
    },

    // 查看详情
    handleView(notification) {
      if (notification) {
        this.selectedNotification = {...notification};
        this.dialogVisible = true;
      }
    },

    // 获取优先级类型
    getPriorityType(priority) {
      switch (priority) {
        case 2:
          return 'danger';
        case 1:
          return 'warning';
        default:
          return 'info';
      }
    },

    // 获取优先级文本
    getPriorityText(priority) {
      switch (priority) {
        case 2:
          return '高';
        case 1:
          return '中';
        default:
          return '低';
      }
    },

    // 获取状态类型
    getStatusType(status) {
      switch (status) {
        case 'pending':
          return 'warning';
        case 'sending':
          return 'info';
        case 'sent':
          return 'success';
        case 'failed':
          return 'danger';
        default:
          return 'info';
      }
    },

    // 获取状态文本
    getStatusText(status) {
      switch (status) {
        case 'pending':
          return '待发送';
        case 'sending':
          return '发送中';
        case 'sent':
          return '已发送';
        case 'failed':
          return '发送失败';
        default:
          return status;
      }
    }
  }
};
</script>

<style scoped>
.pending-notification-view {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.content-container {
  background: white;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.search-card {
  margin-bottom: 20px;
}

.action-buttons {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-dialog__body) {
  max-height: 60vh;
  overflow-y: auto;
}

:deep(.el-descriptions__content) {
  word-break: break-all;
}

pre {
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
  margin: 0;
}
</style>