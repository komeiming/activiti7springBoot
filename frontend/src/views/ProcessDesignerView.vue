<template>
  <div class="process-designer-view">
    <h1>流程设计器</h1>
    <BPMNEditor />
  </div>
</template>

<script>
import BPMNEditor from '../components/BPMNEditor.vue'

export default {
  name: 'ProcessDesignerView',
  components: {
    BPMNEditor
  }
}
</script>

<style scoped>
.process-designer-view {
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

h1 {
  margin: 0;
  padding: 20px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
  font-size: 24px;
  color: #303133;
}
</style>