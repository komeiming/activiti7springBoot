<template>
  <div class="server-error-container">
    <div class="server-error-content">
      <el-icon class="error-icon">
        <CircleCloseFilled />
      </el-icon>
      <h1 class="error-code">500</h1>
      <h2 class="error-title">服务器错误</h2>
      <p class="error-description">
        服务器内部发生了错误，请稍后重试。如果问题持续存在，请联系技术支持。
      </p>
      
      <el-card class="feedback-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>报告问题</span>
          </div>
        </template>
        <el-form ref="feedbackForm" :model="feedbackForm" label-width="80px">
          <el-form-item label="问题描述">
            <el-input
              v-model="feedbackForm.description"
              type="textarea"
              placeholder="请描述您遇到的问题..."
              :rows="3"
            ></el-input>
          </el-form-item>
          <el-form-item label="联系方式">
            <el-input
              v-model="feedbackForm.contact"
              placeholder="请输入您的手机号或邮箱"
              clearable
            ></el-input>
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              :loading="submittingFeedback"
              @click="handleSubmitFeedback"
              class="submit-feedback-btn"
            >
              提交反馈
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
      
      <div class="error-actions">
        <el-button type="primary" @click="handleRefresh">刷新页面</el-button>
        <el-button @click="handleBack">返回上一页</el-button>
        <el-button @click="handleHome">返回首页</el-button>
      </div>
      
      <div class="help-section">
        <h3>技术支持</h3>
        <p>联系邮箱：<a href="mailto:support@example.com" class="help-link">support@example.com</a></p>
        <p>联系电话：<a href="tel:400-123-4567" class="help-link">400-123-4567</a></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { CircleCloseFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 反馈表单数据
const feedbackForm = ref({
  description: '',
  contact: ''
})

// 加载状态
const submittingFeedback = ref(false)

// 刷新页面
const handleRefresh = () => {
  window.location.reload()
}

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

// 提交反馈
const handleSubmitFeedback = async () => {
  if (!feedbackForm.value.description) {
    ElMessage.warning('请描述您遇到的问题')
    return
  }
  
  submittingFeedback.value = true
  try {
    // 调用提交反馈API
    // await axios.post('/api/v1/public/feedback', feedbackForm.value)
    ElMessage.success('反馈提交成功，我们将尽快处理')
    feedbackForm.value = {
      description: '',
      contact: ''
    }
  } catch (error) {
    ElMessage.error('反馈提交失败，请稍后重试')
  } finally {
    submittingFeedback.value = false
  }
}
</script>

<style scoped>
.server-error-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 20px;
}

.server-error-content {
  text-align: center;
  max-width: 600px;
  padding: 40px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.error-icon {
  font-size: 80px;
  color: #ff4d4f;
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

.feedback-card {
  margin: 20px 0;
  border-radius: 8px;
  text-align: left;
}

.card-header {
  font-weight: 600;
  color: #1f2329;
}

.submit-feedback-btn {
  width: 100%;
}

.error-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin: 30px 0;
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
  margin: 5px 0;
}

.help-link {
  color: #1890ff;
  text-decoration: none;
}

.help-link:hover {
  text-decoration: underline;
}
</style>