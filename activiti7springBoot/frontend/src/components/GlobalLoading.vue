<template>
  <transition name="fade">
    <div class="global-loading" v-if="visible">
      <div class="loading-content">
        <el-loading :text="text" :fullscreen="false" :visible="true" :spinner="'el-icon-loading'" />
      </div>
    </div>
  </transition>
</template>

<script>
import { defineComponent } from 'vue'
import { ElLoading } from 'element-plus'

export default defineComponent({
  name: 'GlobalLoading',
  components: {
    ElLoading
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    text: {
      type: String,
      default: '加载中...'
    }
  }
})
</script>

<style scoped>
.global-loading {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.loading-content {
  background-color: white;
  border-radius: 8px;
  padding: 30px;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.loading-text {
  margin-top: 15px;
  color: #606266;
  font-size: 14px;
}

/* 淡入淡出动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 内容缩放动画 */
.fade-enter-active .loading-content,
.fade-leave-active .loading-content {
  transition: transform 0.3s ease;
}

.fade-enter-from .loading-content {
  transform: scale(0.9);
}

.fade-leave-to .loading-content {
  transform: scale(0.9);
}
</style>