<template>
  <div class="sms-editor">
    <div class="editor-header">
      <div class="char-count">
        <span :class="{ 'text-danger': charCount > 1000 }">
          {{ charCount }}/{{ maxLength }}
        </span>
        <span class="sms-count">
          ({{ smsCount }}条短信)
        </span>
      </div>
      <el-button size="small" @click="insertVariable">
        # 插入变量
      </el-button>
    </div>
    
    <div class="editor-container">
      <el-input
        v-model="internalValue"
        type="textarea"
        :rows="8"
        placeholder="请输入短信内容，支持${变量名}格式的占位符"
        @input="handleInput"
        @change="handleChange"
        @keydown="handleKeydown"
      />
    </div>
    
    <div class="editor-footer">
      <el-alert
        title="短信内容注意事项"
        type="info"
        :closable="false"
        :description="[`1. 每条短信最多支持70个汉字或160个英文字符`, `2. 超过限制将分多条发送`, `3. 变量将被实际值替换，请注意预留足够空间`]"
      />
    </div>
    
    <!-- 插入变量对话框 -->
    <el-dialog
      v-model="variableDialogVisible"
      title="插入变量"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form :model="variableForm" label-width="80px">
        <el-form-item label="变量名">
          <el-input v-model="variableForm.name" placeholder="请输入变量名（无需${}）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="variableDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmInsertVariable">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, watch } from 'vue'

export default {
  name: 'SmsEditor',
  components: {
    // 无需Hash组件，按钮已使用文本替代图标
  },
  props: {
    modelValue: {
      type: String,
      default: ''
    },
    maxLength: {
      type: Number,
      default: 1000
    },
    placeholder: {
      type: String,
      default: '请输入短信内容，支持${变量名}格式的占位符'
    }
  },
  emits: ['update:modelValue', 'change'],
  setup(props, { emit }) {
    const internalValue = ref(props.modelValue)
    const variableDialogVisible = ref(false)
    const variableForm = ref({
      name: ''
    })
    
    // 监听props变化，更新内部值
    watch(() => props.modelValue, (newVal) => {
      internalValue.value = newVal
    })
    
    // 计算字符数（汉字按2个字符计算）
    const charCount = computed(() => {
      if (!internalValue.value) return 0
      // 汉字按2个字符计算，其他按1个字符计算
      const chineseChars = (internalValue.value.match(/[\u4e00-\u9fa5]/g) || []).length
      const otherChars = internalValue.value.length - chineseChars
      return chineseChars * 2 + otherChars
    })
    
    // 计算短信条数
    const smsCount = computed(() => {
      if (charCount.value === 0) return 0
      // 每条短信支持70个汉字或160个字符
      const smsUnit = 160
      return Math.ceil(charCount.value / smsUnit)
    })
    
    // 处理输入
    const handleInput = () => {
      emit('update:modelValue', internalValue.value)
    }
    
    // 处理change事件
    const handleChange = () => {
      emit('change', internalValue.value)
    }
    
    // 处理键盘事件
    const handleKeydown = (e) => {
      // 可以在这里添加特殊的键盘事件处理
    }
    
    // 插入变量
    const insertVariable = () => {
      variableDialogVisible.value = true
    }
    
    // 确认插入变量
    const confirmInsertVariable = () => {
      if (variableForm.value.name) {
        const variableText = '${' + variableForm.value.name + '}'
        
        // 获取textarea元素
        const textarea = document.querySelector('.sms-editor textarea')
        if (textarea) {
          // 保存当前光标位置
          const startPos = textarea.selectionStart
          const endPos = textarea.selectionEnd
          
          // 在光标位置插入变量
          internalValue.value = internalValue.value.substring(0, startPos) + variableText + internalValue.value.substring(endPos)
          
          // 触发输入事件
          handleInput()
          
          // 关闭对话框并重置表单
          variableDialogVisible.value = false
          variableForm.value.name = ''
          
          // 在下一个事件循环中重新聚焦并设置光标位置
          setTimeout(() => {
            textarea.focus()
            // 将光标设置到插入文本之后
            const newCursorPos = startPos + variableText.length
            textarea.setSelectionRange(newCursorPos, newCursorPos)
          }, 0)
        } else {
          // 降级处理：如果找不到textarea元素，直接追加到末尾
          internalValue.value += variableText
          handleInput()
          variableDialogVisible.value = false
          variableForm.value.name = ''
        }
      }
    }
    
    // 设置内容
    const setContent = (content) => {
      internalValue.value = content
      handleInput()
    }
    
    // 获取内容
    const getContent = () => {
      return internalValue.value
    }
    
    // 验证短信内容
    const validate = () => {
      if (!internalValue.value || !internalValue.value.trim()) {
        return { valid: false, message: '短信内容不能为空' }
      }
      
      if (charCount.value > props.maxLength) {
        return { valid: false, message: `短信内容超过最大长度限制（${props.maxLength}字符）` }
      }
      
      // 检查变量格式是否正确
      const variableRegex = /\$\{([^}]+)\}/g
      const matches = internalValue.value.match(variableRegex)
      if (matches) {
        for (const match of matches) {
          // 检查变量名是否符合规范（只能包含字母、数字、下划线）
          const varName = match.substring(2, match.length - 1)
          if (!/^[a-zA-Z_][a-zA-Z0-9_]*$/.test(varName)) {
            return { valid: false, message: `变量格式不正确：${match}，变量名只能包含字母、数字、下划线，且不能以数字开头` }
          }
        }
      }
      
      return { valid: true }
    }
    
    // 清理无效的变量格式
    const sanitizeContent = () => {
      let sanitized = internalValue.value
      
      // 处理不完整的变量格式
      // 例如：${var 替换为 ${var}，${ 替换为 ${}}
      sanitized = sanitized.replace(/\$\{[^}]*$/g, (match) => {
        if (!match.endsWith('}')) {
          return match + '}'
        }
        return match
      })
      
      // 移除多余的 }
      sanitized = sanitized.replace(/\}(?!\s*\$\{)/g, '}\\')
      
      if (sanitized !== internalValue.value) {
        internalValue.value = sanitized
        handleInput()
      }
      
      return sanitized
    }
    
    return {
      internalValue,
      variableDialogVisible,
      variableForm,
      charCount,
      smsCount,
      handleInput,
      handleChange,
      handleKeydown,
      insertVariable,
      confirmInsertVariable,
      setContent,
      getContent,
      validate,
      sanitizeContent
    }
  }
}
</script>

<style scoped>
.sms-editor {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.editor-header {
  background-color: #f5f7fa;
  padding: 10px 12px;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.char-count {
  font-size: 14px;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 8px;
}

.sms-count {
  font-size: 12px;
  color: #909399;
}

.editor-container {
  padding: 12px;
}

.editor-container :deep(.el-textarea__inner) {
  border: none;
  resize: vertical;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.5;
  color: #303133;
}

.editor-container :deep(.el-textarea__inner:focus) {
  box-shadow: none;
  border-color: transparent;
}

.editor-footer {
  padding: 12px;
  border-top: 1px solid #dcdfe6;
}

.editor-footer :deep(.el-alert__description) {
  font-size: 13px;
  line-height: 1.6;
  margin-top: 8px;
}

.editor-footer :deep(.el-alert__description li) {
  margin: 4px 0;
}
</style>