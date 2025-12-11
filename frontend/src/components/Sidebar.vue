<template>
  <el-aside width="240px" class="sidebar-container">
    <el-menu
      :default-active="activePath"
      class="el-menu-vertical"
      router
      text-color="#fff"
      background-color="#2a3447"
      active-text-color="#409eff"
    >
      <!-- 所有用户可见的菜单 -->
      <el-menu-item index="/dashboard">
        <el-icon><Operation /></el-icon>
        <span>仪表盘</span>
      </el-menu-item>
      
      <el-sub-menu index="1">
        <template #title>
          <el-icon><Document /></el-icon>
          <span>任务管理</span>
        </template>
        <el-menu-item index="/tasks">
          <el-icon><List /></el-icon>
          <span>任务列表</span>
        </el-menu-item>
        <el-menu-item index="/completed-tasks">
          <el-icon><Check /></el-icon>
          <span>已办任务</span>
        </el-menu-item>
      </el-sub-menu>
      
      <!-- 流程申请相关菜单 -->
      <el-sub-menu index="4">
        <template #title>
          <el-icon><Upload2 /></el-icon>
          <span>流程申请</span>
        </template>
        <el-menu-item index="/leave-application">
          <el-icon><DocumentAdd /></el-icon>
          <span>发起请假</span>
        </el-menu-item>
        <el-menu-item index="/process-apply">
          <el-icon><Upload /></el-icon>
          <span>申请流程</span>
        </el-menu-item>
        <el-menu-item index="/process-history">
          <el-icon><View /></el-icon>
          <span>流程历史</span>
        </el-menu-item>
      </el-sub-menu>

      <!-- 仅管理员可见的菜单 -->
      <template v-if="isAdmin">
        <el-sub-menu index="2">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/roles">
            <el-icon><Position /></el-icon>
            <span>角色管理</span>
          </el-menu-item>
          <el-menu-item index="/notification-templates">
              <el-icon><Bell /></el-icon>
              <span>通知模板管理</span>
            </el-menu-item>
            <el-menu-item index="/notification-logs">
              <el-icon><Reading /></el-icon>
              <span>通知日志</span>
            </el-menu-item>
            <el-menu-item index="/menu-management">
              <el-icon><Menu /></el-icon>
              <span>菜单管理</span>
            </el-menu-item>
            <el-menu-item index="/tenant-approval">
              <el-icon><User /></el-icon>
              <span>租户审批管理</span>
            </el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="3">
          <template #title>
            <el-icon><Operation /></el-icon>
            <span>流程管理</span>
          </template>
          <el-menu-item index="/process-definitions">
            <el-icon><Ticket /></el-icon>
            <span>流程定义管理</span>
          </el-menu-item>
        </el-sub-menu>
      </template>
    </el-menu>
  </el-aside>
</template>

<script>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Document, List, Setting, User, Position, Operation, Ticket, Check, Upload2, DocumentAdd, Upload, View, Bell, Reading, Menu } from '@element-plus/icons-vue'

export default {
  name: 'Sidebar',
  components: {
    Document,
    List,
    Setting,
    User,
    Position,
    Operation,
    Ticket,
    Check,
    Upload2,
    DocumentAdd,
    Upload,
    View,
    Bell,
    Reading,
    Menu
  },
  setup() {
    const route = useRoute()
    
    // 计算当前激活的路径
    const activePath = computed(() => {
      const path = route.path
      // 如果是详情页，激活对应的列表页
      if (path.startsWith('/tasks/')) {
        return '/tasks'
      }
      return path
    })
    
    // 计算用户是否是管理员
    const isAdmin = computed(() => {
      try {
        const userInfo = JSON.parse(localStorage.getItem('user') || '{}')
        // 兼容两种格式：直接判断role字段或roles数组
        return userInfo.role === 'ROLE_ADMIN' || 
               (userInfo.roles && userInfo.roles.some(role => role.roleCode === 'ROLE_ADMIN'))
      } catch {
        return false
      }
    })
    
    return {
      activePath,
      isAdmin
    }
  }
}
</script>

<style scoped>
.sidebar-container {
  height: calc(100vh - 64px);
  background-color: #2a3447;
  overflow-y: auto;
}

.el-menu-vertical {
  height: 100%;
  border-right: none;
}

.el-menu-item {
  padding-left: 45px !important;
  height: 50px;
  line-height: 50px;
}

.el-sub-menu__title {
  height: 50px;
  line-height: 50px;
}

.el-sub-menu__title:hover {
  background-color: #1f2937;
}

.el-menu-item.is-active {
  background-color: #1f2937;
}

:deep(.el-menu-item__content) {
  font-size: 14px;
}
</style>