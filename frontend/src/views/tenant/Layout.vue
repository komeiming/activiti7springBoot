<template>
  <div class="tenant-layout">
    <!-- 顶部导航栏 -->
    <el-header class="tenant-header">
      <div class="header-left">
        <el-button
          type="text"
          @click="toggleSidebar"
          class="sidebar-toggle-btn"
        >
          <el-icon>
            <Menu />
          </el-icon>
        </el-button>
        <div class="logo">
          <span class="logo-text">零售门店业务管理平台</span>
        </div>
      </div>
      <div class="header-right">
        <!-- 通知图标 -->
        <el-dropdown trigger="click" class="notification-dropdown">
          <el-button type="text" class="notification-btn">
            <el-icon>
              <Bell />
            </el-icon>
            <el-badge :value="unreadNotifications" class="notification-badge" />
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-for="item in notifications" :key="item.id">
                <div class="notification-item">
                  <div class="notification-title">{{ item.title }}</div>
                  <div class="notification-content">{{ item.content }}</div>
                  <div class="notification-time">{{ item.time }}</div>
                </div>
              </el-dropdown-item>
              <el-dropdown-item divided>
                <el-button type="text" @click="handleViewAllNotifications" class="view-all-btn">
                  查看全部
                </el-button>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        
        <!-- 帮助中心 -->
        <el-dropdown trigger="click" class="help-dropdown">
          <el-button type="text" class="help-btn">
            <el-icon>
              <QuestionFilled />
            </el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="handleHelpCenter">帮助中心</el-dropdown-item>
              <el-dropdown-item @click="handleApiDoc">API文档</el-dropdown-item>
              <el-dropdown-item @click="handleFaq">FAQ</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        
        <!-- 个人中心 -->
        <el-dropdown trigger="click" class="user-dropdown">
          <div class="user-info">
            <el-avatar :size="32" :src="userInfo.avatar || ''" :icon="UserFilled" />
            <span class="user-name">{{ userInfo.username || '未知用户' }}</span>
            <el-icon class="arrow-down-icon">
              <ArrowDown />
            </el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="handleProfile">个人中心</el-dropdown-item>
              <el-dropdown-item @click="handleChangePassword">修改密码</el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    
    <!-- 主体内容区域 -->
    <div class="tenant-main">
      <!-- 左侧菜单栏 -->
      <el-aside
        :width="sidebarCollapsed ? '64px' : '240px'"
        class="tenant-sidebar"
        :class="{ 'sidebar-collapsed': sidebarCollapsed }"
      >
        <el-scrollbar>
          <el-menu
            :default-active="activeMenu"
            class="tenant-menu"
            router
            :collapse="sidebarCollapsed"
            :collapse-transition="false"
            background-color="#001529"
            text-color="#fff"
            active-text-color="#1890ff"
          >
            <!-- 首页 -->
            <el-menu-item index="/tenant">
              <el-icon>
                <HomeFilled />
              </el-icon>
              <template #title>首页</template>
            </el-menu-item>
            
            <!-- 租户信息管理 -->
            <el-sub-menu index="tenant-info">
              <template #title>
                <el-icon>
                  <Setting />
                </el-icon>
                <span>租户信息管理</span>
              </template>
              <el-menu-item index="/tenant/info">基本信息</el-menu-item>
              <el-menu-item index="/tenant/auth">授权信息</el-menu-item>
              <el-menu-item index="/tenant/service">服务管理</el-menu-item>
            </el-sub-menu>
            
            <!-- 通知模块管理 -->
            <el-sub-menu index="notification" v-if="hasPermission('notification')">
              <template #title>
                <el-icon>
                  <Message />
                </el-icon>
                <span>通知模块</span>
              </template>
              <el-menu-item index="/tenant/notification/templates">模板管理</el-menu-item>
              <el-menu-item index="/tenant/notification/send">通知发送</el-menu-item>
              <el-menu-item index="/tenant/notification/logs">发送日志</el-menu-item>
              <el-menu-item index="/tenant/notification/config">通知配置</el-menu-item>
            </el-sub-menu>
            
            <!-- 工作流模块管理 -->
            <el-sub-menu index="workflow" v-if="hasPermission('workflow')">
              <template #title>
                <el-icon>
                  <Document />
                </el-icon>
                <span>工作流模块</span>
              </template>
              <el-menu-item index="/tenant/workflow/templates">流程模板管理</el-menu-item>
              <el-menu-item index="/tenant/workflow/designer">流程设计</el-menu-item>
              <el-menu-item index="/tenant/workflow/instances">流程实例管理</el-menu-item>
              <el-menu-item index="/tenant/workflow/logs">执行日志</el-menu-item>
              <el-menu-item index="/tenant/workflow/config">工作流配置</el-menu-item>
            </el-sub-menu>
            
            <!-- 监控与日志模块 -->
            <el-sub-menu index="monitor">
              <template #title>
                <el-icon>
                  <DataAnalysis />
                </el-icon>
                <span>监控与日志</span>
              </template>
              <el-menu-item index="/tenant/monitor/stats">调用统计</el-menu-item>
              <el-menu-item index="/tenant/monitor/logs">日志查询</el-menu-item>
              <el-menu-item index="/tenant/monitor/alerts">告警配置</el-menu-item>
            </el-sub-menu>
            

            
          </el-menu>
          
          <!-- 退出登录按钮 -->
          <div class="logout-button-container">
            <el-button type="danger" @click="handleLogout" class="logout-button">
              <el-icon>
                <SwitchButton />
              </el-icon>
              退出登录
            </el-button>
          </div>
        </el-scrollbar>
      </el-aside>
      
      <!-- 右侧内容区域 -->
      <el-main class="tenant-content">
        <div class="content-header">
          <h2 class="page-title">{{ pageTitle }}</h2>
          <div class="breadcrumb">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/tenant' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-for="(item, index) in breadcrumb" :key="index" :to="item.path">{{ item.name }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
        </div>
        
        <div class="content-body">
          <router-view></router-view>
        </div>
        
        <el-footer class="tenant-footer">
        <div class="footer-content">
          <p>© 2025 零售门店业务管理平台 版权所有</p>
          <p>技术支持：591462485@qq.com</p>
        </div>
      </el-footer>
      </el-main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Menu, Bell, QuestionFilled, UserFilled, ArrowDown, 
  HomeFilled, Setting, Message, Document, Lock, DataAnalysis, 
  SwitchButton 
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

// 侧边栏折叠状态
const sidebarCollapsed = ref(false)

// 当前激活的菜单
const activeMenu = computed(() => {
  return route.path
})

// 页面标题
const pageTitle = computed(() => {
  const titleMap = {
    '/tenant': '控制台',
    '/tenant/info': '基本信息',
    '/tenant/auth': '授权信息',
    '/tenant/service': '服务管理',
    '/tenant/notification/templates': '模板管理',
    '/tenant/notification/send': '通知发送',
    '/tenant/notification/logs': '发送日志',
    '/tenant/notification/config': '通知配置',
    '/tenant/workflow/templates': '流程模板管理',
    '/tenant/workflow/designer': '流程设计',
    '/tenant/workflow/instances': '流程实例管理',
    '/tenant/workflow/logs': '执行日志',
    '/tenant/workflow/config': '工作流配置',
    '/tenant/permission/roles': '角色配置',
    '/tenant/permission/custom': '自定义权限申请',
    '/tenant/monitor/stats': '调用统计',
    '/tenant/monitor/logs': '日志查询',
    '/tenant/monitor/alerts': '告警配置'
  }
  return titleMap[route.path] || '未知页面'
})

// 面包屑导航
const breadcrumb = computed(() => {
  const path = route.path
  const breadcrumbMap = {
    '/tenant/info': [{ name: '租户信息管理', path: '/tenant/info' }],
    '/tenant/auth': [{ name: '租户信息管理', path: '/tenant/info' }, { name: '授权信息', path: '/tenant/auth' }],
    '/tenant/service': [{ name: '租户信息管理', path: '/tenant/info' }, { name: '服务管理', path: '/tenant/service' }],
    '/tenant/notification/templates': [{ name: '通知模块', path: '/tenant/notification/templates' }, { name: '模板管理', path: '/tenant/notification/templates' }],
    '/tenant/notification/send': [{ name: '通知模块', path: '/tenant/notification/templates' }, { name: '通知发送', path: '/tenant/notification/send' }],
    '/tenant/notification/logs': [{ name: '通知模块', path: '/tenant/notification/templates' }, { name: '发送日志', path: '/tenant/notification/logs' }],
    '/tenant/notification/config': [{ name: '通知模块', path: '/tenant/notification/templates' }, { name: '通知配置', path: '/tenant/notification/config' }],
    '/tenant/workflow/templates': [{ name: '工作流模块', path: '/tenant/workflow/templates' }, { name: '流程模板管理', path: '/tenant/workflow/templates' }],
    '/tenant/workflow/designer': [{ name: '工作流模块', path: '/tenant/workflow/templates' }, { name: '流程设计', path: '/tenant/workflow/designer' }],
    '/tenant/workflow/instances': [{ name: '工作流模块', path: '/tenant/workflow/templates' }, { name: '流程实例管理', path: '/tenant/workflow/instances' }],
    '/tenant/workflow/logs': [{ name: '工作流模块', path: '/tenant/workflow/templates' }, { name: '执行日志', path: '/tenant/workflow/logs' }],
    '/tenant/workflow/config': [{ name: '工作流模块', path: '/tenant/workflow/templates' }, { name: '工作流配置', path: '/tenant/workflow/config' }],
    '/tenant/permission/roles': [{ name: '权限管理', path: '/tenant/permission/roles' }, { name: '角色配置', path: '/tenant/permission/roles' }],
    '/tenant/permission/custom': [{ name: '权限管理', path: '/tenant/permission/roles' }, { name: '自定义权限申请', path: '/tenant/permission/custom' }],
    '/tenant/monitor/stats': [{ name: '监控与日志', path: '/tenant/monitor/stats' }, { name: '调用统计', path: '/tenant/monitor/stats' }],
    '/tenant/monitor/logs': [{ name: '监控与日志', path: '/tenant/monitor/stats' }, { name: '日志查询', path: '/tenant/monitor/logs' }],
    '/tenant/monitor/alerts': [{ name: '监控与日志', path: '/tenant/monitor/stats' }, { name: '告警配置', path: '/tenant/monitor/alerts' }]
  }
  return breadcrumbMap[path] || []
})

// 用户信息
const userInfo = ref({
  username: '测试租户',
  avatar: ''
})

// 通知数据
const notifications = ref([
  { id: 1, title: '审核结果通知', content: '您的服务开通申请已通过审核', time: '2025-12-08 10:30' },
  { id: 2, title: '服务告警', content: '今日调用量已超过阈值', time: '2025-12-08 09:15' },
  { id: 3, title: '系统公告', content: '平台将于12月10日进行维护升级', time: '2025-12-07 18:00' }
])

// 未读通知数量
const unreadNotifications = ref(2)

// 切换侧边栏折叠状态
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

// 查看所有通知
const handleViewAllNotifications = () => {
  ElMessage.info('查看所有通知功能开发中')
}

// 帮助中心
const handleHelpCenter = () => {
  ElMessage.info('帮助中心功能开发中')
}

// API文档
const handleApiDoc = () => {
  ElMessage.info('API文档功能开发中')
}

// FAQ
const handleFaq = () => {
  ElMessage.info('FAQ功能开发中')
}

// 个人中心
const handleProfile = () => {
  ElMessage.info('个人中心功能开发中')
}

// 修改密码
const handleChangePassword = () => {
  ElMessage.info('修改密码功能开发中')
}

// 退出登录
const handleLogout = () => {
  // 清除本地存储的用户信息
  localStorage.removeItem('user')
  localStorage.removeItem('tenantInfo')
  // 跳转到登录页面
  router.push('/login')
  ElMessage.success('已退出登录')
}

// 权限检查
const hasPermission = (module) => {
  // 从本地存储获取租户权限信息
  const tenantInfoStr = localStorage.getItem('tenantInfo')
  if (tenantInfoStr) {
    try {
      const tenantInfo = JSON.parse(tenantInfoStr)
      // 根据serviceModules判断权限，serviceModules格式为逗号分隔的模块列表
      const serviceModules = tenantInfo.serviceModules || ''
      const modules = serviceModules.split(',').map(m => m.trim())
      return modules.includes(module)
    } catch (error) {
      console.error('解析租户信息失败:', error)
      return false
    }
  }
  return false
}
</script>

<style scoped>
.tenant-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.tenant-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.sidebar-toggle-btn {
  font-size: 20px;
  color: #606266;
}

.logo {
  font-size: 18px;
  font-weight: 600;
  color: #1890ff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.notification-btn, .help-btn {
  font-size: 20px;
  color: #606266;
}

.notification-badge {
  position: absolute;
  top: 5px;
  right: 5px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 4px;
  transition: all 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.user-name {
  font-size: 14px;
  color: #303133;
}

.arrow-down-icon {
  font-size: 12px;
  color: #909399;
}

.tenant-main {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.tenant-sidebar {
  background-color: #001529;
  overflow: hidden;
  transition: width 0.3s;
  display: flex;
  flex-direction: column;
}

.tenant-sidebar.sidebar-collapsed {
  width: 64px;
}

.tenant-menu {
  border-right: none;
  flex: 1;
  overflow: auto;
}

.tenant-sidebar :deep(.el-scrollbar) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.tenant-sidebar :deep(.el-scrollbar__wrap) {
  flex: 1;
}

.tenant-sidebar :deep(.el-scrollbar__view) {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.tenant-content {
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: hidden;
  background-color: #f5f7fa;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background-color: #fff;
  border-bottom: 1px solid #e8e8e8;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.breadcrumb {
  font-size: 14px;
}

.content-body {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background-color: #f5f7fa;
}

.tenant-footer {
  padding: 12px 24px;
  text-align: center;
  font-size: 14px;
  color: #909399;
  background-color: #fff;
  border-top: 1px solid #e8e8e8;
}

.footer-content {
  display: flex;
  justify-content: center;
  gap: 40px;
}

.footer-content p {
  margin: 0;
}

/* 退出登录按钮容器样式 */
.logout-button-container {
  padding: 20px;
  margin-top: auto;
  text-align: center;
}

/* 退出登录按钮样式 */
.logout-button {
  width: 100%;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
</style>