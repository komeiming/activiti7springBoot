<template>
  <div id="app">
    <!-- 根据是否登录和路由类型显示不同的布局 -->
    <template v-if="requiresAuth && !isTenantRoute">
      <!-- 已登录状态且非租户路由：使用带导航的系统布局 -->
      <Layout />
    </template>
    <template v-else>
      <!-- 未登录状态或租户路由：直接显示路由内容 -->
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <!-- 添加安全检查，确保Component存在且有效 -->
          <component v-if="Component" :is="Component" />
        </transition>
      </router-view>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import Layout from './components/Layout.vue'

const router = useRouter()
const route = useRoute()
const requiresAuth = ref(false)

// 计算当前路由是否为租户路由
const isTenantRoute = computed(() => {
  return route.path.startsWith('/tenant')
})

const updateAuthStatus = () => {
  // 根据当前路由的meta信息判断是否需要认证
  requiresAuth.value = route.meta.requiresAuth || false
  console.log('更新认证状态:', requiresAuth.value)
}

// 初始化时检查路由是否需要认证
onMounted(() => {
  console.log('App组件已挂载')
  updateAuthStatus()
})

// 监听路由变化
watch(
  () => route.path,
  () => {
    updateAuthStatus()
  }
)
</script>

<style>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  width: 100%;
  font-family: Arial, sans-serif;
  background-color: #f5f5f5;
}

#app {
  height: 100%;
  width: 100%;
}

/* 过渡动画 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>