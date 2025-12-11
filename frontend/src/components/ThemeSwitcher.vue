<template>
  <div class="theme-switcher">
    <el-dropdown trigger="click" @command="handleThemeChange">
      <el-button size="small" :type="isDarkTheme ? 'default' : 'primary'">
        <template #icon>
          <el-icon v-if="isDarkTheme"><Moon /></el-icon>
          <el-icon v-else><Sunny /></el-icon>
        </template>
        {{ isDarkTheme ? '深色主题' : '浅色主题' }}
        <el-icon class="el-icon--right"><ArrowDown /></el-icon>
      </el-button>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item divided disabled>浅色主题</el-dropdown-item>
          <el-dropdown-item command="default">默认蓝色</el-dropdown-item>
          <el-dropdown-item command="green">清新绿色</el-dropdown-item>
          <el-dropdown-item command="purple">优雅紫色</el-dropdown-item>
          <el-dropdown-item command="orange">活力橙色</el-dropdown-item>
          <el-dropdown-item command="red">热情红色</el-dropdown-item>
          <el-dropdown-item divided disabled>深色主题</el-dropdown-item>
          <el-dropdown-item command="dark">经典深色</el-dropdown-item>
          <el-dropdown-item command="dark-blue">深蓝深色</el-dropdown-item>
          <el-dropdown-item command="dark-green">深绿深色</el-dropdown-item>
          <el-dropdown-item command="dark-purple">深紫深色</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Moon, Sunny, ArrowDown } from '@element-plus/icons-vue'

const currentTheme = ref('default')

const isDarkTheme = computed(() => {
  // 检查当前主题是否为深色主题
  return currentTheme.value.startsWith('dark')
})

const applyTheme = (theme) => {
  try {
    // 移除所有主题类
    document.documentElement.removeAttribute('data-theme')
    // 如果不是默认主题，则添加data-theme属性
    if (theme !== 'default') {
      document.documentElement.setAttribute('data-theme', theme)
    }
  } catch (error) {
    console.error('主题切换失败:', error)
  }
}

const handleThemeChange = (theme) => {
  try {
    currentTheme.value = theme
    applyTheme(theme)
    // 保存主题到localStorage
    localStorage.setItem('theme', theme)
  } catch (error) {
    console.error('处理主题变更失败:', error)
  }
}

// 从localStorage中获取保存的主题，如果没有则使用默认主题
onMounted(() => {
  try {
    const savedTheme = localStorage.getItem('theme')
    if (savedTheme) {
      currentTheme.value = savedTheme
      applyTheme(savedTheme)
    }
  } catch (error) {
    console.error('初始化主题失败:', error)
  }
})
</script>

<style scoped>
.theme-switcher {
  margin-right: 20px;
}
</style>