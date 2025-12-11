<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <el-icon class="error-icon">
        <DocumentDelete />
      </el-icon>
      <h1 class="error-code">404</h1>
      <h2 class="error-title">页面不存在</h2>
      <p class="error-description">
        您访问的页面不存在或已被删除。请检查您输入的URL是否正确，或返回首页重新开始。
      </p>
      
      <div class="search-section">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索您想访问的页面..."
          :prefix-icon="Search"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>
      
      <div class="quick-links">
        <h3>快速链接</h3>
        <div class="link-list">
          <el-button type="text" @click="handleHome" class="quick-link-btn">
            <el-icon class="link-icon"><HomeFilled /></el-icon>
            <span>首页</span>
          </el-button>
          <el-button type="text" @click="handleDashboard" class="quick-link-btn">
            <el-icon class="link-icon"><DataAnalysis /></el-icon>
            <span>控制台</span>
          </el-button>
          <el-button type="text" @click="handleTasks" class="quick-link-btn">
            <el-icon class="link-icon"><List /></el-icon>
            <span>待办任务</span>
          </el-button>
          <el-button type="text" @click="handleProcessHistory" class="quick-link-btn">
            <el-icon class="link-icon"><Clock /></el-icon>
            <span>流程历史</span>
          </el-button>
        </div>
      </div>
      
      <div class="error-actions">
        <el-button type="primary" @click="handleHome">返回首页</el-button>
        <el-button @click="handleBack">返回上一页</el-button>
        <el-button @click="handleContactSupport">联系客服</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  DocumentDelete, 
  Search, 
  HomeFilled, 
  DataAnalysis, 
  List, 
  Clock 
} from '@element-plus/icons-vue'

const router = useRouter()

// 搜索关键词
const searchKeyword = ref('')

// 返回首页
const handleHome = () => {
  router.push('/')
}

// 返回上一页
const handleBack = () => {
  if (router.history.state.back) {
    router.back()
  } else {
    router.push('/')
  }
}

// 跳转到控制台
const handleDashboard = () => {
  router.push('/dashboard')
}

// 跳转到待办任务
const handleTasks = () => {
  router.push('/tasks')
}

// 跳转到流程历史
const handleProcessHistory = () => {
  router.push('/process-history')
}

// 联系客服
const handleContactSupport = () => {
  ElMessage.info('客服功能开发中，敬请期待')
}

// 搜索功能
const handleSearch = () => {
  if (!searchKeyword.value) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  
  // 这里可以实现搜索功能，目前仅做提示
  ElMessage.info(`搜索 "${searchKeyword.value}" 功能开发中，敬请期待`)
}
</script>

<style scoped>
.not-found-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 20px;
}

.not-found-content {
  text-align: center;
  max-width: 600px;
  padding: 40px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.error-icon {
  font-size: 80px;
  color: #1890ff;
  margin-bottom: 20px;
}

.error-code {
  font-size: 64px;
  font-weight: 700;
  color: #1f2329;
  margin-bottom: 10px;
}

.error-title {
  font-size: 28px;
  font-weight: 600;
  color: #1f2329;
  margin-bottom: 15px;
}

.error-description {
  font-size: 16px;
  color: #86909c;
  margin-bottom: 30px;
  line-height: 1.6;
}

.search-section {
  margin-bottom: 30px;
}

.search-section .el-input {
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
}

.quick-links {
  margin-bottom: 30px;
}

.quick-links h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1f2329;
  margin-bottom: 15px;
}

.link-list {
  display: flex;
  gap: 20px;
  justify-content: center;
  flex-wrap: wrap;
}

.quick-link-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  color: #86909c;
}

.quick-link-btn:hover {
  color: #1890ff;
}

.link-icon {
  font-size: 24px;
  margin-bottom: 5px;
}

.error-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  flex-wrap: wrap;
}
</style>