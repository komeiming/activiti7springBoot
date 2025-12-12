<template>
  <div class="access-denied-container">
    <div class="access-denied-content">
      <el-icon class="error-icon">
        <WarningFilled />
      </el-icon>
      <h1 class="error-code">403</h1>
      <h2 class="error-title">无权限访问</h2>
      <p class="error-description">
        您没有权限访问此页面或执行此操作，请检查您的权限配置
      </p>
      <div class="error-actions">
        <el-button type="primary" @click="handleBack">返回上一页</el-button>
        <el-button @click="handleHome">返回首页</el-button>
        <el-button @click="handleApplyPermission">申请权限</el-button>
      </div>
      <div class="help-section">
        <h3>需要帮助？</h3>
        <p>请联系管理员或查看<a href="#" class="help-link">权限管理手册</a></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'
import { WarningFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

// 返回上一页
const handleBack = () => {
  if (router.history.state.back) {
    router.back()
  } else {
    router.push('/dashboard')
  }
}

// 返回首页
const handleHome = () => {
  router.push('/dashboard')
}

// 申请权限
const handleApplyPermission = () => {
  // 这里可以跳转到权限申请页面
  ElMessage.info('权限申请功能开发中，敬请期待')
  // router.push('/tenant/permission/custom')
}
</script>

<style scoped>
.access-denied-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 20px;
}

.access-denied-content {
  text-align: center;
  max-width: 600px;
  padding: 40px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.error-icon {
  font-size: 80px;
  color: #faad14;
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

.error-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-bottom: 30px;
  flex-wrap: wrap;
}

.help-section {
  padding-top: 20px;
  border-top: 1px solid #e8e8e8;
}

.help-section h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1f2329;
  margin-bottom: 10px;
}

.help-section p {
  font-size: 14px;
  color: #86909c;
}

.help-link {
  color: #1890ff;
  text-decoration: none;
}

.help-link:hover {
  text-decoration: underline;
}
</style>