<template>
  <div class="not-found-container">
    <el-card shadow="hover">
      <div class="not-found-content">
        <el-empty description="404 页面不存在" :image-size="200" />
        <p class="not-found-text">抱歉，您访问的页面不存在或已被移除</p>
        <el-button type="primary" @click="goHome">返回首页</el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'NotFoundView',
  methods: {
    goHome() {
      this.$router.push({ name: 'login' })
    }
  }
}
</script>

<style scoped>
.not-found-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.not-found-content {
  text-align: center;
  padding: 40px;
}

.not-found-text {
  margin-top: 20px;
  margin-bottom: 30px;
  color: #606266;
  font-size: 16px;
}
</style>