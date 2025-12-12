<template>
  <div class="layout-container">
    <!-- 侧边栏导航 -->
  <aside class="sidebar" :class="{ 'sidebar-collapsed': isSidebarCollapsed }">
    <!-- 系统标题 -->
    <div class="sidebar-header">
      <div class="logo-container">
          <img src="/bayer-logo.png" alt="系统图标" class="sidebar-logo">
          <h1 class="sidebar-title">{{ systemName }}</h1>
        </div>
      <button class="sidebar-toggle" @click="toggleSidebar">
        {{ isSidebarCollapsed ? '展开' : '收起' }}
      </button>
    </div>
      
      <!-- 导航菜单 -->
      <nav class="sidebar-menu">
        <!-- 动态渲染菜单 -->
        <div 
          v-for="menu in menus" 
          :key="menu.id"
          class="menu-group"
        >
          <!-- 目录类型菜单 -->
          <template v-if="menu.menuType === 'directory'">
            <div class="menu-group-title">{{ menu.localizedName || menu.name }}</div>
            <!-- 渲染子菜单 -->
            <div 
              v-for="subMenu in menu.children" 
              :key="subMenu.id"
              class="menu-item"
              :class="{ active: currentRoute === subMenu.path }"
              @click="() => {
                console.log('点击菜单项:', subMenu.name, 'path:', subMenu.path);
                navigateTo(subMenu.path);
              }"
            >
              <el-icon class="menu-icon">
                <component :is="iconMap[subMenu.icon] || Menu" />
              </el-icon>
              <span class="menu-text">{{ subMenu.localizedName || subMenu.name }}</span>
            </div>
          </template>
          <!-- 菜单项类型菜单 -->
          <template v-else-if="menu.menuType === 'menu'">
            <div 
              class="menu-item"
              :class="{ active: currentRoute === menu.path }"
              @click="() => {
                console.log('点击菜单项:', menu.name, 'path:', menu.path);
                navigateTo(menu.path);
              }"
            >
              <el-icon class="menu-icon">
                <component :is="iconMap[menu.icon] || Menu" />
              </el-icon>
              <span class="menu-text">{{ menu.localizedName || menu.name }}</span>
            </div>
          </template>
        </div>
      </nav>
      
      <!-- 退出登录按钮 -->
      <div class="sidebar-footer">
        <button class="logout-btn" @click="logout">退出登录</button>
      </div>
    </aside>
    
    <!-- 主内容区域 -->
    <main class="main-content">
      <!-- 顶部栏 -->
      <header class="topbar">
        <div class="topbar-left">
          <button class="sidebar-toggle-btn" @click="toggleSidebar">
            <el-icon><Menu /></el-icon>
          </button>
          <!-- 主题切换 -->
          <ThemeSwitcher />
        </div>
        
        <div class="topbar-center">
          <!-- 可以放面包屑导航或其他内容 -->
        </div>
        
        <div class="topbar-right">
          <!-- 语言切换 -->
          <div class="language-switcher">
            <el-select 
              v-model="languageCode" 
              size="small"
              @change="switchLanguage"
              class="custom-language-select"
            >
              <el-option
                v-for="lang in languages"
                :key="lang.code"
                :label="lang.name"
                :value="lang.code"
              >
                <div class="language-option">
                  <span class="language-name">{{ lang.name }}</span>
                </div>
              </el-option>
            </el-select>
          </div>
          <!-- 通知图标 -->
          <div class="notification-icon">
            <el-icon><Bell /></el-icon>
          </div>
          <!-- 用户信息 -->
          <div class="user-info">
            <span class="user-name">{{ currentUser?.username || '用户' }}</span>
            <div class="user-avatar">
              <!-- 根据用户角色显示不同的头像 -->
              <img 
                :src="getUserAvatar(currentUser?.role)" 
                alt="用户头像" 
                class="avatar-img"
                />
            </div>
          </div>
        </div>
      </header>
      
      <!-- 页面内容 -->
      <div class="page-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  DataLine,
  DocumentCopy,
  Check,
  Document,
  User,
  UserFilled,
  Avatar,
  List,
  View,
  Menu,
  Setting,
  Message,
  ChatRound,
  Clock,
  Bell,
  Grid
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import ThemeSwitcher from './ThemeSwitcher.vue'
import PermissionService from '../services/PermissionService'
import eventBus from '../utils/eventBus'
import { getSystemName, getPageTitle } from '../utils/languageConfig'

const router = useRouter()
const route = useRoute()

// 侧边栏折叠状态
const isSidebarCollapsed = ref(false)

// 当前路由
const currentRoute = computed(() => route.path)

// 当前用户信息
const currentUser = ref(null)

// 菜单数据
const menus = ref([])

// 系统名称 - 响应式变量
const systemName = ref('零售门店业务管理平台')

// 图标映射表
const iconMap = {
  DataLine,
  DocumentCopy,
  Check,
  Document,
  User,
  UserFilled,
  Avatar,
  List,
  View,
  Menu,
  Setting,
  Message,
  ChatRound,
  Clock,
  Bell,
  Grid
}

// 语言相关
const languageCode = ref(localStorage.getItem('language') || 'zh-CN')
const languages = ref([
  { code: 'zh-CN', name: '中文' },
  { code: 'en-US', name: 'English' }
])

// 获取菜单数据
const loadMenus = async () => {
  try {
    console.log('=== 开始获取菜单数据 ===');
    console.log('语言代码:', languageCode.value);
    // 从后端API获取菜单数据，确保菜单顺序正确
    const response = await PermissionService.getUserMenus(languageCode.value);
    console.log('PermissionService.getUserMenus返回数据:', response);
    console.log('返回数据类型:', typeof response);
    console.log('是否为数组:', Array.isArray(response));
    
    // 由于axiosConfig.js已经处理了响应，直接返回了数据，所以不需要再检查code
    
    // 递归排序子菜单
    const sortMenuChildren = (menuList) => {
      console.log('sortMenuChildren接收参数:', menuList);
      console.log('参数类型:', typeof menuList);
      console.log('是否为数组:', Array.isArray(menuList));
      
      // 确保menuList是数组
      if (!Array.isArray(menuList)) {
        console.warn('menuList不是数组，无法排序:', menuList);
        return [];
      }
      
      // 首先按照menuOrder排序当前菜单列表
      menuList.sort((a, b) => {
        const orderA = a.menuOrder || 0;
        const orderB = b.menuOrder || 0;
        return orderA - orderB;
      });
      
      // 递归排序每个菜单的子菜单
      menuList.forEach(menu => {
        if (menu.children && Array.isArray(menu.children) && menu.children.length > 0) {
          menu.children = sortMenuChildren(menu.children);
        }
      });
      
      return menuList;
    };
    
    // 排序后的菜单数据
    menus.value = sortMenuChildren(response);
    
    console.log('=== 菜单数据 ===')
    console.log('完整菜单数据:', menus.value)
    
  } catch (error) {
    console.error('获取菜单失败:', error);
    console.error('错误详情:', error.response || error.message || error);
    // 不使用模拟数据，保持菜单为空
    menus.value = [];
    // 可以添加错误提示，告知用户菜单加载失败
    ElMessage.error('菜单加载失败，请检查网络连接或联系管理员');
  }
}

// 更新页面标题
const updatePageTitle = () => {
  const routeName = route.name || 'dashboard'
  const pageTitle = getPageTitle(routeName, languageCode.value)
  document.title = pageTitle
}

// 切换语言
const switchLanguage = (langCode) => {
  languageCode.value = langCode
  localStorage.setItem('language', langCode)
  // 更新系统名称
  systemName.value = getSystemName(langCode)
  // 更新页面标题
  updatePageTitle()
  // 重新加载菜单数据
  loadMenus()
}

// 监听语言变化
watch(languageCode, (newLang) => {
  // 可以在这里添加其他需要更新语言的逻辑
  console.log('语言切换为:', newLang)
})

// 监听路由变化，更新页面标题
watch(() => route.path, () => {
  updatePageTitle()
})

// 切换侧边栏展开/收起状态
function toggleSidebar() {
  isSidebarCollapsed.value = !isSidebarCollapsed.value
}

// 导航到指定路径
function navigateTo(path) {
  router.push(path)
}

// 退出登录
function logout() {
  // 清除本地存储的用户信息
  localStorage.removeItem('user')
  // 重定向到登录页面
  router.push('/login')
}

// 根据用户角色获取头像
function getUserAvatar(role) {
  // 这里可以根据不同角色返回不同的头像图片
  // 目前使用默认头像，实际项目中可以替换为真实的头像图片
  return 'https://picsum.photos/200/200?random=' + (role || 'user')
}

// 组件挂载时获取用户信息和菜单
onMounted(async () => {
  // 获取用户信息
  const userInfo = localStorage.getItem('user')
  if (userInfo) {
    try {
      currentUser.value = JSON.parse(userInfo)
    } catch (e) {
      console.error('解析用户信息失败', e)
    }
  }
  
  // 初始化系统名称和页面标题
  systemName.value = getSystemName(languageCode.value)
  updatePageTitle()
  
  // 获取菜单数据
  await loadMenus()
  
  // 监听菜单更新事件
  eventBus.on('menuUpdated', handleMenuUpdated)
})

// 组件卸载时移除事件监听
onUnmounted(() => {
  eventBus.off('menuUpdated', handleMenuUpdated)
})

// 处理菜单更新事件
const handleMenuUpdated = async () => {
  console.log('菜单数据已更新，重新加载菜单...')
  await loadMenus()
}
</script>

<style scoped>
/* 全局布局样式 */
.layout-container {
  display: flex;
  height: 100vh;
  width: 100%;
  overflow: hidden;
}

/* 侧边栏样式 */
.sidebar {
  width: 250px;
  height: 100%;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  box-shadow: var(--shadow-base);
  border-right: 1px solid var(--border-color);
}

.sidebar.sidebar-collapsed {
  width: 60px;
}

/* 侧边栏头部 */
.sidebar-header {
  padding: 0;
  border-bottom: none;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: transparent;
}

.logo-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  background: linear-gradient(135deg, #00b42a 0%, #1296db 100%);
  padding: 15px 10px;
  text-align: center;
}

.sidebar-logo {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  margin-right: 5px;
}

.sidebar-title {
  font-size: 14px;
  font-weight: 600;
  margin: 0;
  color: white;
  white-space: normal;
  line-height: 1.4;
  overflow-wrap: break-word;
  word-break: break-word;
  max-width: 100%;
  text-align: center;
}

.sidebar.sidebar-collapsed .sidebar-title {
  display: none;
}

.sidebar-toggle {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  font-size: 12px;
  margin-right: 15px;
  z-index: 10;
}

.sidebar.sidebar-collapsed .sidebar-toggle {
  transform: rotate(180deg);
}

/* 侧边栏菜单 */
.sidebar-menu {
  flex: 1;
  padding: 20px 0;
  overflow-y: auto;
  background-color: var(--bg-primary);
}

.menu-group {
  margin-bottom: 10px;
}

.menu-group-title {
  padding: 10px 20px;
  font-size: 12px;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-weight: 600;
}

.sidebar.sidebar-collapsed .menu-group-title {
  display: none;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  cursor: pointer;
  transition: all var(--transition-fast);
  border-left: 8px solid transparent;
  color: var(--text-primary);
}

.menu-item:hover {
  background-color: var(--bg-hover);
  color: var(--primary-color);
}

.menu-item.active {
  background-color: var(--bg-hover);
  border-left-color: var(--primary-color);
  color: var(--primary-color);
  font-weight: bold;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.menu-icon {
  font-size: 18px;
  margin-right: 10px;
}

.menu-text {
  font-size: 14px;
  font-weight: bold;
}

.sidebar.sidebar-collapsed .menu-text {
  display: none;
}

.sidebar.sidebar-collapsed .menu-item {
  justify-content: center;
  padding: 12px 0;
}

.sidebar.sidebar-collapsed .menu-icon {
  margin-right: 0;
}

/* 侧边栏底部 */
.sidebar-footer {
  padding: 20px;
  border-top: 1px solid var(--border-color);
  background-color: var(--bg-primary);
}

.logout-btn {
  width: 100%;
  padding: 10px;
  background-color: var(--danger-color);
  color: white;
  border: none;
  border-radius: var(--border-radius-base);
  cursor: pointer;
  font-size: 14px;
  transition: background-color var(--transition-fast);
}

.logout-btn:hover {
  background-color: #dc2626;
}

.sidebar.sidebar-collapsed .logout-btn {
  font-size: 0;
  height: 40px;
  padding: 0;
}

/* 主内容区域 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: var(--bg-regular);
}

/* 顶部栏 */
.topbar {
  height: 60px;
  background-color: var(--bg-primary);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: var(--shadow-base);
  border-bottom: 1px solid var(--border-color);
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sidebar-toggle-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: var(--text-secondary);
  padding: 5px;
  transition: color var(--transition-fast);
}

.sidebar-toggle-btn:hover {
  color: var(--primary-color);
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.language-switcher {
  display: flex;
  align-items: center;
}

.custom-language-select {
  width: auto;
  min-width: 120px;
  background-color: var(--bg-primary);
  border-color: var(--border-color);
  color: var(--text-primary);
  transition: all var(--transition-fast);
}

.custom-language-select:hover {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px var(--primary-color-light);
}

.custom-language-select .el-select__wrapper {
  border-radius: var(--border-radius-base);
  background-color: transparent;
}

.custom-language-select .el-select__input {
  color: var(--text-primary);
}

.custom-language-select .el-select__caret {
  color: var(--text-secondary);
  transition: transform var(--transition-fast);
}

.custom-language-select:hover .el-select__caret {
  color: var(--primary-color);
}

.custom-language-select .el-select-dropdown {
  background-color: var(--bg-primary);
  border-color: var(--border-color);
  box-shadow: var(--shadow-base);
}

.custom-language-select .el-select-dropdown__item {
  color: var(--text-primary);
  transition: all var(--transition-fast);
}

.custom-language-select .el-select-dropdown__item:hover {
  background-color: var(--bg-hover);
  color: var(--primary-color);
}

.custom-language-select .el-select-dropdown__item.selected {
  background-color: var(--bg-hover);
  color: var(--primary-color);
}

.language-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.language-name {
  font-weight: 500;
}

.notification-icon {
  font-size: 20px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: color var(--transition-fast);
}

.notification-icon:hover {
  color: var(--primary-color);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: var(--border-light);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 页面内容 */
.page-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: var(--bg-regular);
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    z-index: 1000;
    transform: translateX(0);
  }
  
  .sidebar.sidebar-collapsed {
    transform: translateX(-100%);
    width: 250px;
  }
  
  .sidebar.sidebar-collapsed .sidebar-title,
  .sidebar.sidebar-collapsed .menu-text,
  .sidebar.sidebar-collapsed .menu-group-title,
  .sidebar.sidebar-collapsed .logout-btn {
    display: block;
    font-size: 14px;
  }
  
  .main-content {
    width: 100%;
  }
}
</style>