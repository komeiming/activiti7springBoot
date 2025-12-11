<template>
  <div class="rich-text-editor">
    <!-- 工具栏 -->
    <div class="editor-toolbar">
      <!-- 标题级别 -->
      <el-button-group>
        <el-button size="small" @click="execCommand('formatBlock', '<h1>')" title="标题1">H1</el-button>
        <el-button size="small" @click="execCommand('formatBlock', '<h2>')" title="标题2">H2</el-button>
        <el-button size="small" @click="execCommand('formatBlock', '<h3>')" title="标题3">H3</el-button>
        <el-button size="small" @click="execCommand('formatBlock', '<h4>')" title="标题4">H4</el-button>
        <el-button size="small" @click="execCommand('formatBlock', '<h5>')" title="标题5">H5</el-button>
        <el-button size="small" @click="execCommand('formatBlock', '<h6>')" title="标题6">H6</el-button>
        <el-button size="small" @click="execCommand('formatBlock', '<p>')" title="段落">P</el-button>
      </el-button-group>
      
      <el-divider direction="vertical" />
      
      <!-- 文本格式化 -->
      <el-button-group>
        <el-button size="small" @click="execCommand('bold')" title="加粗">
          B
        </el-button>
        <el-button size="small" @click="execCommand('italic')" title="斜体">
          I
        </el-button>
        <el-button size="small" @click="execCommand('underline')" title="下划线">
          U
        </el-button>
        <el-button size="small" @click="execCommand('strikeThrough')" title="删除线">
          S
        </el-button>
        <el-button size="small" @click="execCommand('superscript')" title="上标">
          X²
        </el-button>
        <el-button size="small" @click="execCommand('subscript')" title="下标">
          X₂
        </el-button>
      </el-button-group>
      
      <el-divider direction="vertical" />
      
      <!-- 字体样式 -->
      <el-button-group>
        <el-button size="small" @click="showFontFamilyPicker" title="字体">
          字体
        </el-button>
        <el-button size="small" @click="showFontSizePicker" title="字体大小">
          大小
        </el-button>
        <el-button size="small" @click="showLineHeightPicker" title="行高">
          行高
        </el-button>
        <el-button size="small" @click="showLetterSpacingPicker" title="字间距">
          字距
        </el-button>
      </el-button-group>
      
      <el-divider direction="vertical" />
      
      <!-- 字体颜色和背景色 -->
      <el-button-group>
        <el-button size="small" @click="showColorPicker('foreColor')" title="字体颜色">
          文字色
        </el-button>
        <el-button size="small" @click="showColorPicker('backColor')" title="背景颜色">
          背景色
        </el-button>
      </el-button-group>
      
      <el-divider direction="vertical" />
      
      <!-- 对齐方式 -->
      <el-button-group>
        <el-button size="small" @click="execCommand('justifyLeft')" title="左对齐">
          左
        </el-button>
        <el-button size="small" @click="execCommand('justifyCenter')" title="居中对齐">
          中
        </el-button>
        <el-button size="small" @click="execCommand('justifyRight')" title="右对齐">
          右
        </el-button>
        <el-button size="small" @click="execCommand('justifyFull')" title="两端对齐">
          均
        </el-button>
      </el-button-group>
      
      <el-divider direction="vertical" />
      
      <!-- 列表和缩进 -->
      <el-button-group>
        <el-button size="small" @click="insertUnorderedList" title="无序列表">
          •
        </el-button>
        <el-button size="small" @click="insertOrderedList" title="有序列表">
          1.
        </el-button>
        <el-button size="small" @click="insertDefinitionList" title="定义列表">
          定义
        </el-button>
        <el-button size="small" @click="execCommand('indent')" title="增加缩进">
          →
        </el-button>
        <el-button size="small" @click="execCommand('outdent')" title="减少缩进">
          ←
        </el-button>
      </el-button-group>
      
      <el-divider direction="vertical" />
      
      <!-- 块级元素 -->
      <el-button-group>
        <el-button size="small" @click="execCommand('formatBlock', '<blockquote>')" title="引用块">
          引用
        </el-button>
        <el-button size="small" @click="execCommand('formatBlock', '<pre>')" title="代码块">
          代码
        </el-button>
        <el-button size="small" @click="insertTable" title="插入表格">
          表格
        </el-button>
      </el-button-group>
      
      <el-divider direction="vertical" />
      
      <!-- 插入功能 -->
      <el-button-group>
        <el-button size="small" @click="insertLink" title="插入链接">
          🔗
        </el-button>
        <el-button size="small" @click="insertImage" title="插入图片">
          📷
        </el-button>
        <el-button size="small" @click="insertAttachment" title="上传附件">
          📎
        </el-button>
        <el-button size="small" @click="insertSpecialChar" title="插入特殊字符">
          特殊字符
        </el-button>
        <el-button size="small" @click="execCommand('insertHorizontalRule')" title="插入水平线">
          —
        </el-button>
      </el-button-group>
      
      <el-divider direction="vertical" />
      
      <!-- 编辑操作 -->
      <el-button-group>
        <el-button size="small" @click="execCommand('undo')" title="撤销">
          ↩️
        </el-button>
        <el-button size="small" @click="execCommand('redo')" title="重做">
          ↪️
        </el-button>
        <el-button size="small" @click="removeLink" title="移除链接">
          移除链接
        </el-button>
        <el-button size="small" @click="showFindReplaceDialog" title="查找替换">
          查找
        </el-button>
        <el-button size="small" @click="showWordCount" title="字数统计">
          字数
        </el-button>
      </el-button-group>
      
      <el-divider direction="vertical" />
      
      <!-- 变量和清除格式 -->
      <el-button-group>
        <el-button size="small" @click="insertVariable" title="插入变量">
          # 变量
        </el-button>
        <el-button size="small" @click="clearFormat" type="danger" title="清除格式">
          清除
        </el-button>
      </el-button-group>
      
      <el-divider direction="vertical" />
      
      <!-- 视图操作 -->
      <el-button-group>
        <el-button size="small" @click="toggleFullScreen" title="全屏模式">
          全屏
        </el-button>
        <el-button size="small" @click="togglePreview" title="预览">
          预览
        </el-button>
      </el-button-group>
    </div>
    
    <!-- 编辑区域 -->
    <div class="editor-container">
      <div
        ref="editor"
        class="editor-content"
        contenteditable
        @input="handleInput"
        @paste="handlePaste"
        @keydown="handleKeydown"
      ></div>
      <!-- 隐藏的文件输入 -->
      <input
        ref="fileInput"
        type="file"
        style="display: none"
        @change="handleFileUpload"
        multiple
      >
    </div>
    
    <!-- 插入链接对话框 -->
    <el-dialog
      v-model="linkDialogVisible"
      title="插入链接"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form :model="linkForm" label-width="80px">
        <el-form-item label="文本">
          <el-input v-model="linkForm.text" placeholder="请输入链接文本" />
        </el-form-item>
        <el-form-item label="URL">
          <el-input v-model="linkForm.url" placeholder="请输入链接地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="linkDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmInsertLink">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 插入图片对话框 -->
    <el-dialog
      v-model="imageDialogVisible"
      title="插入图片"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form :model="imageForm" label-width="80px">
        <el-form-item label="图片URL">
          <el-input v-model="imageForm.url" placeholder="请输入图片URL" />
        </el-form-item>
        <el-form-item label="图片描述">
          <el-input v-model="imageForm.alt" placeholder="请输入图片描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="imageDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmInsertImage">确定</el-button>
      </template>
    </el-dialog>
    
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
    
    <!-- 颜色选择器对话框 -->
    <el-dialog
      v-model="colorPickerVisible"
      :title="colorPickerForm.type === 'foreColor' ? '选择字体颜色' : '选择背景颜色'"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form :model="colorPickerForm" label-width="80px">
        <el-form-item label="颜色">
          <el-color-picker v-model="colorPickerForm.color" show-alpha />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="colorPickerVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmColorPicker">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 特殊字符对话框 -->
    <el-dialog
      v-model="specialCharDialogVisible"
      title="插入特殊字符"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="special-chars-container">
        <div 
          v-for="(char, index) in specialChars" 
          :key="index"
          class="special-char-item"
          @click="confirmInsertSpecialChar(char.char)"
        >
          <span class="char-display">{{ char.char }}</span>
          <span class="char-name">{{ char.name }}</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="specialCharDialogVisible = false">取消</el-button>
      </template>
    </el-dialog>
    
    <!-- 预览模式 -->
    <div v-if="previewMode" class="preview-container">
      <div class="preview-header">
        <h3>预览模式</h3>
        <el-button type="primary" size="small" @click="togglePreview">退出预览</el-button>
      </div>
      <div class="preview-content" v-html="props.modelValue"></div>
    </div>
    
    <!-- 字体选择对话框 -->
    <el-dialog
      v-model="fontFamilyDialogVisible"
      title="选择字体"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form :model="fontFamilyForm" label-width="80px">
        <el-form-item label="字体">
          <el-select v-model="fontFamilyForm.fontFamily" placeholder="请选择字体">
            <el-option v-for="font in fontFamilies" :key="font.value" :label="font.name" :value="font.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="fontFamilyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmFontFamilyPicker">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 字体大小选择对话框 -->
    <el-dialog
      v-model="fontSizeDialogVisible"
      title="选择字体大小"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form :model="fontSizeForm" label-width="80px">
        <el-form-item label="字体大小">
          <el-select v-model="fontSizeForm.fontSize" placeholder="请选择字体大小">
            <el-option v-for="size in fontSizes" :key="size.value" :label="size.name" :value="size.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="fontSizeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmFontSizePicker">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 行高选择对话框 -->
    <el-dialog
      v-model="lineHeightDialogVisible"
      title="选择行高"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form :model="lineHeightForm" label-width="80px">
        <el-form-item label="行高">
          <el-select v-model="lineHeightForm.lineHeight" placeholder="请选择行高">
            <el-option v-for="height in lineHeights" :key="height.value" :label="height.name" :value="height.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="lineHeightDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmLineHeightPicker">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 字间距选择对话框 -->
    <el-dialog
      v-model="letterSpacingDialogVisible"
      title="选择字间距"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form :model="letterSpacingForm" label-width="80px">
        <el-form-item label="字间距">
          <el-select v-model="letterSpacingForm.letterSpacing" placeholder="请选择字间距">
            <el-option v-for="spacing in letterSpacings" :key="spacing.value" :label="spacing.name" :value="spacing.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="letterSpacingDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmLetterSpacingPicker">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 查找替换对话框 -->
    <el-dialog
      v-model="findReplaceDialogVisible"
      title="查找替换"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="findReplaceForm" label-width="80px">
        <el-form-item label="查找">
          <el-input v-model="findReplaceForm.findText" placeholder="请输入要查找的文本" />
        </el-form-item>
        <el-form-item label="替换为">
          <el-input v-model="findReplaceForm.replaceText" placeholder="请输入替换文本" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="findReplaceForm.matchCase">区分大小写</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="findReplaceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="findText">查找</el-button>
        <el-button type="primary" @click="replaceText">替换</el-button>
        <el-button type="primary" @click="replaceAllText">替换全部</el-button>
      </template>
    </el-dialog>
    
    <!-- 字数统计对话框 -->
    <el-dialog
      v-model="wordCountDialogVisible"
      title="字数统计"
      width="400px"
      :close-on-click-modal="false"
      :show-close="false"
    >
      <div class="word-count-content">
        <div class="word-count-item">
          <span class="word-count-label">字符数（含空格）：</span>
          <span class="word-count-value">{{ wordCountResult.characters }}</span>
        </div>
        <div class="word-count-item">
          <span class="word-count-label">单词数：</span>
          <span class="word-count-value">{{ wordCountResult.words }}</span>
        </div>
        <div class="word-count-item">
          <span class="word-count-label">段落数：</span>
          <span class="word-count-value">{{ wordCountResult.paragraphs }}</span>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="wordCountDialogVisible = false">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 表格行列选择对话框 -->
    <el-dialog
      v-model="tableDialogVisible"
      title="插入表格"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form :model="tableForm" label-width="80px">
        <el-form-item label="行数">
          <el-input-number 
            v-model="tableForm.rows" 
            :min="1" 
            :max="10" 
            :step="1" 
            placeholder="请输入行数"
          />
        </el-form-item>
        <el-form-item label="列数">
          <el-input-number 
            v-model="tableForm.columns" 
            :min="1" 
            :max="10" 
            :step="1" 
            placeholder="请输入列数"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tableDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmInsertTable">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
  import { ref, watch, nextTick, onMounted } from 'vue'

export default {
  name: 'RichTextEditor',
  components: {},
  props: {
    modelValue: {
      type: String,
      default: ''
    },
    placeholder: {
      type: String,
      default: '请输入内容...'
    }
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    const editor = ref(null)
    const fileInput = ref(null)
    const linkDialogVisible = ref(false)
    const imageDialogVisible = ref(false)
    const variableDialogVisible = ref(false)
    const colorPickerVisible = ref(false)
    const specialCharDialogVisible = ref(false)
    const previewMode = ref(false)
    const fullScreenMode = ref(false)
    const savedRange = ref(null)
    
    const linkForm = ref({
      text: '',
      url: ''
    })
    
    const imageForm = ref({
      url: '',
      alt: ''
    })
    
    const variableForm = ref({
      name: ''
    })
    
    const colorPickerForm = ref({
      type: 'foreColor',
      color: '#000000'
    })
    
    // 特殊字符列表
    const specialChars = [
      { name: '空格', char: ' ' },
      { name: '版权', char: '©' },
      { name: '注册商标', char: '®' },
      { name: '商标', char: '™' },
      { name: '欧元', char: '€' },
      { name: '英镑', char: '£' },
      { name: '美元', char: '$' },
      { name: '日元', char: '¥' },
      { name: '人民币', char: '¥' },
      { name: '摄氏度', char: '°C' },
      { name: '华氏度', char: '°F' },
      { name: '正负号', char: '±' },
      { name: '乘号', char: '×' },
      { name: '除号', char: '÷' },
      { name: '大于等于', char: '≥' },
      { name: '小于等于', char: '≤' },
      { name: '不等于', char: '≠' },
      { name: '约等于', char: '≈' },
      { name: '无穷大', char: '∞' },
      { name: '圆周率', char: 'π' },
      { name: '平方根', char: '√' },
      { name: '求和', char: '∑' },
      { name: '积分', char: '∫' },
      { name: '箭头', char: '→' },
      { name: '左箭头', char: '←' },
      { name: '上箭头', char: '↑' },
      { name: '下箭头', char: '↓' },
      { name: '版权', char: '©' },
      { name: '注册商标', char: '®' },
      { name: '商标', char: '™' }
    ]
    
    // 字体列表
    const fontFamilies = [
      { name: '默认', value: 'inherit' },
      { name: '宋体', value: 'SimSun, STSong' },
      { name: '黑体', value: 'SimHei, STHeiti' },
      { name: '微软雅黑', value: 'Microsoft YaHei, STXihei' },
      { name: 'Arial', value: 'Arial, sans-serif' },
      { name: 'Verdana', value: 'Verdana, sans-serif' },
      { name: 'Times New Roman', value: 'Times New Roman, serif' },
      { name: 'Courier New', value: 'Courier New, monospace' },
      { name: 'Georgia', value: 'Georgia, serif' },
      { name: 'Trebuchet MS', value: 'Trebuchet MS, sans-serif' }
    ]
    
    // 字体大小列表
    const fontSizes = [
      { name: '10px', value: '10px' },
      { name: '12px', value: '12px' },
      { name: '14px', value: '14px' },
      { name: '16px', value: '16px' },
      { name: '18px', value: '18px' },
      { name: '20px', value: '20px' },
      { name: '24px', value: '24px' },
      { name: '28px', value: '28px' },
      { name: '32px', value: '32px' },
      { name: '36px', value: '36px' },
      { name: '48px', value: '48px' },
      { name: '60px', value: '60px' },
      { name: '72px', value: '72px' }
    ]
    
    // 行高列表
    const lineHeights = [
      { name: '1.0', value: '1.0' },
      { name: '1.2', value: '1.2' },
      { name: '1.5', value: '1.5' },
      { name: '1.8', value: '1.8' },
      { name: '2.0', value: '2.0' },
      { name: '2.5', value: '2.5' },
      { name: '3.0', value: '3.0' }
    ]
    
    // 字间距列表
    const letterSpacings = [
      { name: '0px', value: '0px' },
      { name: '1px', value: '1px' },
      { name: '2px', value: '2px' },
      { name: '3px', value: '3px' },
      { name: '4px', value: '4px' },
      { name: '5px', value: '5px' },
      { name: '10px', value: '10px' }
    ]
    
    // 新的对话框状态
    const fontFamilyDialogVisible = ref(false)
    const fontSizeDialogVisible = ref(false)
    const lineHeightDialogVisible = ref(false)
    const letterSpacingDialogVisible = ref(false)
    const findReplaceDialogVisible = ref(false)
    const wordCountDialogVisible = ref(false)
    // 表格行列选择对话框
    const tableDialogVisible = ref(false)
    
    // 新的表单数据
    const fontFamilyForm = ref({ fontFamily: 'inherit' })
    const fontSizeForm = ref({ fontSize: '16px' })
    const lineHeightForm = ref({ lineHeight: '1.5' })
    const letterSpacingForm = ref({ letterSpacing: '0px' })
    const findReplaceForm = ref({ findText: '', replaceText: '', matchCase: false })
    // 表格行列选择表单
    const tableForm = ref({ rows: 3, columns: 3 })
    
    // 字数统计结果
    const wordCountResult = ref({ characters: 0, words: 0, paragraphs: 0 })
    
    // 监听props变化，更新编辑器内容
    watch(() => props.modelValue, (newVal) => {
      if (editor.value && newVal !== editor.value.innerHTML) {
        editor.value.innerHTML = newVal || ''
      }
    }, { immediate: true })
    
    // 执行命令
    const execCommand = (command, value = null) => {
      try {
        // 检查命令是否支持
        const supportedCommands = ['bold', 'italic', 'underline', 'strikeThrough', 'justifyLeft', 'justifyCenter', 'justifyRight', 'justifyFull', 'insertUnorderedList', 'insertOrderedList', 'indent', 'outdent', 'insertHorizontalRule', 'removeFormat', 'formatBlock', 'superscript', 'subscript', 'undo', 'redo', 'unlink'];
        
        if (supportedCommands.includes(command)) {
          document.execCommand(command, false, value);
        } else {
          // 对于特殊命令，使用自定义实现
          switch (command) {
            case 'createLink':
              // 自定义实现链接创建
              createLinkCustom(value);
              break;
            case 'insertImage':
              // 自定义实现图片插入
              insertImageCustom(value);
              break;
            case 'insertDefinitionList':
              // 手动插入定义列表HTML
              const dlHtml = '<dl><dt contenteditable="true">术语</dt><dd contenteditable="true">定义</dd></dl>';
              insertHtmlAtCursor(dlHtml);
              break;
            case 'foreColor':
              // 自定义实现文字颜色
              applyColor('color', value);
              break;
            case 'backColor':
              // 自定义实现背景颜色
              applyColor('backgroundColor', value);
              break;
          }
        }
        
        editor.value.focus();
        handleInput();
      } catch (error) {
        console.error('执行命令失败:', error);
        // 不抛出错误，避免影响其他功能
      }
    }
    
    // 应用颜色
    const applyColor = (type, color) => {
      const selection = window.getSelection();
      if (selection.rangeCount > 0) {
        const range = selection.getRangeAt(0);
        
        // 如果有选中内容，创建span标签包裹
        if (!range.collapsed) {
          const span = document.createElement('span');
          span.style[type] = color;
          
          // 如果选区是一个文本节点，直接包裹
          if (range.startContainer === range.endContainer && range.startContainer.nodeType === 3) {
            const textNode = range.extractContents();
            span.appendChild(textNode);
            range.insertNode(span);
          } else {
            // 否则使用surroundContents
            try {
              range.surroundContents(span);
            } catch (e) {
              // 如果surroundContents失败，使用extractContents和insertNode
              const fragment = range.extractContents();
              span.appendChild(fragment);
              range.insertNode(span);
            }
          }
        } else {
          // 如果没有选中内容，设置当前选区的样式
          document.execCommand(type === 'color' ? 'foreColor' : 'backColor', false, color);
        }
      }
    }
    
    // 自定义链接创建
    const createLinkCustom = (url) => {
      const selection = window.getSelection();
      if (selection.rangeCount > 0) {
        const range = selection.getRangeAt(0);
        const selectedText = range.toString();
        
        // 创建链接HTML
        const linkHtml = `<a href="${url}" target="_blank">${selectedText || url}</a>`;
        insertHtmlAtCursor(linkHtml);
      }
    }
    
    // 自定义图片插入
    const insertImageCustom = (url) => {
      const imgHtml = `<img src="${url}" alt="" style="max-width: 100%; height: auto;">`;
      insertHtmlAtCursor(imgHtml);
    }
    
    // 插入无序列表
    const insertUnorderedList = () => {
      execCommand('insertUnorderedList')
    }
    
    // 插入有序列表
    const insertOrderedList = () => {
      execCommand('insertOrderedList')
    }
    
    // 插入定义列表
    const insertDefinitionList = () => {
      execCommand('insertDefinitionList')
    }
    
    // 显示颜色选择器
    const showColorPicker = (type) => {
      colorPickerForm.value.type = type
      colorPickerVisible.value = true
    }
    
    // 确认颜色选择
    const confirmColorPicker = () => {
      execCommand(colorPickerForm.value.type, colorPickerForm.value.color)
      colorPickerVisible.value = false
    }
    
    // 插入表格
    const insertTable = () => {
      // 显示表格行列选择对话框
      tableDialogVisible.value = true;
    }
    
    // 确认插入表格
    const confirmInsertTable = () => {
      const { rows, columns } = tableForm.value;
      
      // 生成表格HTML
      let tableHtml = `
        <table border="1" cellpadding="5" cellspacing="0" style="border-collapse: collapse; width: 100%; min-width: 400px;">
      `;
      
      // 生成表格行和列
      for (let i = 0; i < rows; i++) {
        tableHtml += '          <tr>\n';
        for (let j = 0; j < columns; j++) {
          // 为第一行和第一列添加默认内容，方便用户编辑
          const cellContent = i === 0 ? `${j + 1}` : (j === 0 ? String.fromCharCode(97 + i - 1) : '');
          tableHtml += `            <td contenteditable="true">${cellContent}</td>\n`;
        }
        tableHtml += '          </tr>\n';
      }
      
      tableHtml += '        </table>';
      
      editor.value.focus();
      insertHtmlAtCursor(tableHtml);
      
      // 尝试将光标定位到第一个单元格
      setTimeout(() => {
        const firstTd = editor.value.querySelector('table td:first-child');
        if (firstTd) {
          // 创建新的选区并聚焦到第一个单元格
          const range = document.createRange();
          const selection = window.getSelection();
          range.setStart(firstTd, 0);
          range.collapse(true);
          selection.removeAllRanges();
          selection.addRange(range);
          firstTd.focus();
        }
      }, 100);
      
      // 关闭对话框
      tableDialogVisible.value = false;
    }
    
    // 插入HTML
    const insertHtmlAtCursor = (html) => {
      try {
        const selection = window.getSelection()
        if (selection.rangeCount > 0) {
          const range = selection.getRangeAt(0)
          range.deleteContents()
          
          // 创建临时容器
          const tempDiv = document.createElement('div')
          tempDiv.innerHTML = html
          
          // 处理所有子节点
          const nodes = Array.from(tempDiv.childNodes)
          
          if (nodes.length > 0) {
            // 插入第一个节点到选区
            range.insertNode(nodes[0])
            
            // 插入剩余节点
            for (let i = 1; i < nodes.length; i++) {
              range.collapse(false)
              range.insertNode(nodes[i])
            }
            
            // 将光标移动到插入内容的末尾
            range.collapse(false)
            selection.removeAllRanges()
            selection.addRange(range)
          }
          
          // 触发输入事件
          handleInput()
          
          // 聚焦编辑器
          editor.value.focus()
        }
      } catch (error) {
        console.error('插入HTML失败:', error)
        // 不抛出错误，避免影响其他功能
      }
    }
    
    // 插入特殊字符
    const insertSpecialChar = () => {
      // 保存当前选区
      const selection = window.getSelection()
      if (selection.rangeCount > 0) {
        savedRange.value = selection.getRangeAt(0).cloneRange()
      }
      specialCharDialogVisible.value = true
    }
    
    // 确认插入特殊字符
    const confirmInsertSpecialChar = (char) => {
      // 聚焦编辑器
      editor.value.focus()
      
      // 恢复选区或创建新选区
      const selection = window.getSelection()
      selection.removeAllRanges()
      
      if (savedRange.value) {
        selection.addRange(savedRange.value)
      } else {
        // 如果没有保存的选区，创建一个新的选区在编辑器末尾
        const range = document.createRange()
        range.selectNodeContents(editor.value)
        range.collapse(false)
        selection.addRange(range)
      }
      
      insertTextAtCursor(char)
      
      // 重置保存的选区
      savedRange.value = null
      
      specialCharDialogVisible.value = false
    }
    
    // 插入附件
    const insertAttachment = () => {
      // 保存当前选区
      const selection = window.getSelection()
      if (selection.rangeCount > 0) {
        savedRange.value = selection.getRangeAt(0).cloneRange()
      }
      // 触发文件选择
      fileInput.value.click()
    }
    
    // 处理文件上传
    const handleFileUpload = (e) => {
      const files = e.target.files
      if (files.length > 0) {
        // 聚焦编辑器
        editor.value.focus()
        
        // 恢复选区或创建新选区
        const selection = window.getSelection()
        selection.removeAllRanges()
        
        if (savedRange.value) {
          selection.addRange(savedRange.value)
        } else {
          // 如果没有保存的选区，创建一个新的选区在编辑器末尾
          const range = document.createRange()
          range.selectNodeContents(editor.value)
          range.collapse(false)
          selection.addRange(range)
        }
        
        // 处理每个文件
        Array.from(files).forEach(file => {
          // 这里可以添加实际的文件上传逻辑
          // 目前使用DataURL作为示例
          const reader = new FileReader()
          reader.onload = (event) => {
            // 创建附件链接
            const fileUrl = event.target.result
            const attachmentHtml = `<a href="${fileUrl}" target="_blank" style="display: inline-block; padding: 4px 8px; background-color: #f0f0f0; border: 1px solid #dcdfe6; border-radius: 4px; text-decoration: none; color: #606266; margin: 2px;">
              📎 ${file.name}
            </a>&nbsp;`
            insertHtmlAtCursor(attachmentHtml)
          }
          reader.onerror = (error) => {
            console.error('文件读取失败:', error)
          }
          reader.readAsDataURL(file)
        })
        
        // 重置保存的选区
        savedRange.value = null
        
        // 清空文件输入
        e.target.value = ''
      }
    }
    
    // 切换全屏模式
    const toggleFullScreen = () => {
      fullScreenMode.value = !fullScreenMode.value
      if (fullScreenMode.value) {
        document.documentElement.requestFullscreen()
      } else {
        document.exitFullscreen()
      }
    }
    
    // 切换预览模式
    const togglePreview = () => {
      previewMode.value = !previewMode.value
    }
    
    // 显示字体选择器
    const showFontFamilyPicker = () => {
      fontFamilyDialogVisible.value = true
    }
    
    // 确认字体选择
    const confirmFontFamilyPicker = () => {
      execCommand('fontName', fontFamilyForm.value.fontFamily)
      fontFamilyDialogVisible.value = false
    }
    
    // 显示字体大小选择器
    const showFontSizePicker = () => {
      fontSizeDialogVisible.value = true
    }
    
    // 确认字体大小选择
    const confirmFontSizePicker = () => {
      execCommand('fontSize', fontSizeForm.value.fontSize)
      fontSizeDialogVisible.value = false
    }
    
    // 显示行高选择器
    const showLineHeightPicker = () => {
      lineHeightDialogVisible.value = true
    }
    
    // 确认行高选择
    const confirmLineHeightPicker = () => {
      // 行高需要直接设置样式，execCommand不支持
      const selection = window.getSelection()
      if (selection.rangeCount > 0) {
        const range = selection.getRangeAt(0)
        const span = document.createElement('span')
        span.style.lineHeight = lineHeightForm.value.lineHeight
        span.innerHTML = range.toString()
        range.deleteContents()
        range.insertNode(span)
        handleInput()
      }
      lineHeightDialogVisible.value = false
    }
    
    // 显示字间距选择器
    const showLetterSpacingPicker = () => {
      letterSpacingDialogVisible.value = true
    }
    
    // 确认字间距选择
    const confirmLetterSpacingPicker = () => {
      // 字间距需要直接设置样式，execCommand不支持
      const selection = window.getSelection()
      if (selection.rangeCount > 0) {
        const range = selection.getRangeAt(0)
        const span = document.createElement('span')
        span.style.letterSpacing = letterSpacingForm.value.letterSpacing
        span.innerHTML = range.toString()
        range.deleteContents()
        range.insertNode(span)
        handleInput()
      }
      letterSpacingDialogVisible.value = false
    }
    
    // 移除链接
    const removeLink = () => {
      execCommand('unlink')
    }
    
    // 显示查找替换对话框
    const showFindReplaceDialog = () => {
      findReplaceDialogVisible.value = true
    }
    
    // 查找文本
    const findText = () => {
      if (!findReplaceForm.value.findText) return
      
      const content = editor.value.innerHTML
      const text = findReplaceForm.value.findText
      const matchCase = findReplaceForm.value.matchCase
      
      // 简单的查找实现，高亮匹配文本
      const regex = new RegExp(text, matchCase ? 'g' : 'gi')
      const highlightedContent = content.replace(regex, '<mark>$&</mark>')
      editor.value.innerHTML = highlightedContent
      handleInput()
    }
    
    // 替换文本
    const replaceText = () => {
      if (!findReplaceForm.value.findText) return
      
      const selection = window.getSelection()
      if (selection.rangeCount > 0) {
        const range = selection.getRangeAt(0)
        const selectedText = range.toString()
        
        if (selectedText === findReplaceForm.value.findText) {
          range.deleteContents()
          const textNode = document.createTextNode(findReplaceForm.value.replaceText)
          range.insertNode(textNode)
          handleInput()
        }
      }
    }
    
    // 替换全部文本
    const replaceAllText = () => {
      if (!findReplaceForm.value.findText) return
      
      const content = editor.value.innerHTML
      const text = findReplaceForm.value.findText
      const replaceText = findReplaceForm.value.replaceText
      const matchCase = findReplaceForm.value.matchCase
      
      const regex = new RegExp(text, matchCase ? 'g' : 'gi')
      const newContent = content.replace(regex, replaceText)
      editor.value.innerHTML = newContent
      handleInput()
    }
    
    // 显示字数统计
    const showWordCount = () => {
      calculateWordCount()
      wordCountDialogVisible.value = true
    }
    
    // 计算字数统计
    const calculateWordCount = () => {
      if (!editor.value) return
      
      // 获取纯文本内容
      const text = editor.value.innerText || editor.value.textContent || ''
      
      // 字符数（含空格）
      const characters = text.length
      
      // 单词数（简单实现：按空格分割）
      const words = text.trim() ? text.trim().split(/\s+/).length : 0
      
      // 段落数（按换行分割）
      const paragraphs = text.trim() ? text.trim().split(/\n+/).length : 0
      
      wordCountResult.value = { characters, words, paragraphs }
    }
    
    // 监听内容变化，更新字数统计
    watch(() => props.modelValue, () => {
      calculateWordCount()
    })
    
    // 插入链接
    const insertLink = () => {
      const selection = window.getSelection()
      if (selection.rangeCount > 0) {
        // 保存选区
        savedRange.value = selection.getRangeAt(0).cloneRange()
        const selectedText = selection.toString()
        linkForm.value.text = selectedText
      }
      linkDialogVisible.value = true
    }
    
    // 确认插入链接
    const confirmInsertLink = () => {
      if (linkForm.value.url) {
        // 聚焦编辑器
        editor.value.focus()
        
        // 恢复选区或创建新选区
        const selection = window.getSelection()
        selection.removeAllRanges()
        
        let rangeToUse
        if (savedRange.value) {
          rangeToUse = savedRange.value
          selection.addRange(rangeToUse)
        } else {
          // 如果没有保存的选区，创建一个新的选区在编辑器末尾
          rangeToUse = document.createRange()
          rangeToUse.selectNodeContents(editor.value)
          rangeToUse.collapse(false)
          selection.addRange(rangeToUse)
        }
        
        // 获取选区文本
        const selectedText = selection.toString()
        
        // 创建链接HTML，使用表单中的文本或URL作为显示文本
        const linkHtml = `<a href="${linkForm.value.url}" target="_blank">${linkForm.value.text || selectedText || linkForm.value.url}</a>`;
        
        // 直接操作DOM插入链接，避免insertHtmlAtCursor的选区问题
        try {
          // 删除当前选区内容
          rangeToUse.deleteContents()
          
          // 创建临时容器解析HTML
          const tempDiv = document.createElement('div')
          tempDiv.innerHTML = linkHtml
          
          // 获取所有子节点
          const nodes = Array.from(tempDiv.childNodes)
          
          // 插入所有节点
          nodes.forEach((node, index) => {
            if (index === 0) {
              // 第一个节点直接插入到选区
              rangeToUse.insertNode(node)
            } else {
              // 后续节点插入到前一个节点之后
              rangeToUse.setStartAfter(nodes[index - 1])
              rangeToUse.collapse(false)
              rangeToUse.insertNode(node)
            }
          })
          
          // 将光标移动到插入内容的末尾
          rangeToUse.setStartAfter(nodes[nodes.length - 1])
          rangeToUse.collapse(false)
          selection.removeAllRanges()
          selection.addRange(rangeToUse)
        } catch (error) {
          console.error('插入链接失败:', error)
          // 降级使用insertHtmlAtCursor
          insertHtmlAtCursor(linkHtml)
        }
        
        // 重置保存的选区
        savedRange.value = null
        linkDialogVisible.value = false
        
        // 重置表单
        linkForm.value = {
          text: '',
          url: ''
        }
        
        // 触发输入事件
        handleInput()
      }
    }
    
    // 插入图片
    const insertImage = () => {
      // 保存当前选区
      const selection = window.getSelection()
      if (selection.rangeCount > 0) {
        savedRange.value = selection.getRangeAt(0).cloneRange()
      }
      imageDialogVisible.value = true
    }
    
    // 确认插入图片
    const confirmInsertImage = () => {
      if (imageForm.value.url) {
        // 聚焦编辑器
        editor.value.focus()
        
        // 恢复选区或创建新选区
        const selection = window.getSelection()
        selection.removeAllRanges()
        
        if (savedRange.value) {
          selection.addRange(savedRange.value)
        } else {
          // 如果没有保存的选区，创建一个新的选区在编辑器末尾
          const range = document.createRange()
          range.selectNodeContents(editor.value)
          range.collapse(false)
          selection.addRange(range)
        }
        
        // 使用自定义图片插入方式
        const altText = imageForm.value.alt || '';
        const imgHtml = `<img src="${imageForm.value.url}" alt="${altText}" style="max-width: 100%; height: auto;">`;
        insertHtmlAtCursor(imgHtml);
        
        // 重置保存的选区
        savedRange.value = null
        
        imageDialogVisible.value = false
        // 重置表单
        imageForm.value = {
          url: '',
          alt: ''
        }
      }
    }
    
    // 插入变量
    const insertVariable = () => {
      // 保存当前选区
      const selection = window.getSelection()
      if (selection.rangeCount > 0) {
        savedRange.value = selection.getRangeAt(0).cloneRange()
      }
      variableDialogVisible.value = true
    }
    
    // 确认插入变量
    const confirmInsertVariable = () => {
      if (variableDialogVisible.value && variableForm.value && variableForm.value.name) {
        const variableText = '${' + variableForm.value.name + '}'
        
        // 聚焦编辑器
        editor.value.focus()
        
        // 恢复选区或创建新选区
        const selection = window.getSelection()
        selection.removeAllRanges()
        
        if (savedRange.value) {
          selection.addRange(savedRange.value)
        } else {
          // 如果没有保存的选区，创建一个新的选区在编辑器末尾
          const range = document.createRange()
          range.selectNodeContents(editor.value)
          range.collapse(false)
          selection.addRange(range)
        }
        
        insertTextAtCursor(variableText)
        
        // 重置保存的选区
        savedRange.value = null
        
        variableDialogVisible.value = false
        // 重置表单
        variableForm.value = {
          name: ''
        }
      }
    }
    
    // 在光标位置插入文本
    const insertTextAtCursor = (text) => {
      const selection = window.getSelection()
      if (selection.rangeCount > 0) {
        const range = selection.getRangeAt(0)
        range.deleteContents()
        
        const textNode = document.createTextNode(text)
        range.insertNode(textNode)
        
        // 将光标移动到插入文本的末尾
        range.setStartAfter(textNode)
        range.setEndAfter(textNode)
        selection.removeAllRanges()
        selection.addRange(range)
        
        handleInput()
      }
    }
    
    // 清除格式
    const clearFormat = () => {
      execCommand('removeFormat')
      execCommand('formatBlock', '<p>')
    }
    
    // 处理输入
    const handleInput = () => {
      if (editor.value) {
        emit('update:modelValue', editor.value.innerHTML)
      }
    }
    
    // 处理粘贴
    const handlePaste = (e) => {
      e.preventDefault()
      
      const clipboardData = e.clipboardData || window.clipboardData
      
      // 检查是否有文件
      const items = clipboardData.items || []
      let hasFiles = false
      
      for (let i = 0; i < items.length; i++) {
        const item = items[i]
        if (item.kind === 'file') {
          hasFiles = true
          const file = item.getAsFile()
          if (file) {
            // 处理文件（图片或其他附件）
            if (file.type.indexOf('image') !== -1) {
              handleImageFile(file)
            } else {
              // 处理其他类型文件
              handleAttachmentFile(file)
            }
          }
        }
      }
      
      if (!hasFiles) {
        // 尝试获取HTML内容
        const html = clipboardData.getData('text/html')
        
        if (html && html.trim()) {
          // 如果有HTML内容，插入HTML
          insertHtmlAtCursor(html)
        } else {
          // 否则插入纯文本
          const text = clipboardData.getData('text/plain')
          insertTextAtCursor(text)
        }
      }
    }
    
    // 处理图片文件
    const handleImageFile = (file) => {
      // 创建FileReader读取图片
      const reader = new FileReader()
      reader.onload = (event) => {
        // 图片加载完成后插入
        const imgHtml = `<img src="${event.target.result}" alt="" style="max-width: 100%; height: auto;">`
        insertHtmlAtCursor(imgHtml)
      }
      reader.onerror = (error) => {
        console.error('图片读取失败:', error)
      }
      reader.readAsDataURL(file)
    }
    
    // 处理附件文件
    const handleAttachmentFile = (file) => {
      // 创建FileReader读取文件
      const reader = new FileReader()
      reader.onload = (event) => {
        // 文件加载完成后插入
        const fileUrl = event.target.result
        const attachmentHtml = `<a href="${fileUrl}" target="_blank" style="display: inline-block; padding: 4px 8px; background-color: #f0f0f0; border: 1px solid #dcdfe6; border-radius: 4px; text-decoration: none; color: #606266; margin: 2px;">
          📎 ${file.name}
        </a>&nbsp;`
        insertHtmlAtCursor(attachmentHtml)
      }
      reader.onerror = (error) => {
        console.error('文件读取失败:', error)
      }
      reader.readAsDataURL(file)
    }
    
    // 处理键盘事件
    const handleKeydown = (e) => {
      // 支持Tab键缩进
      if (e.key === 'Tab') {
        e.preventDefault()
        insertTextAtCursor('  ')
      }
    }
    
    // 设置内容
    const setContent = (content) => {
      if (editor.value) {
        editor.value.innerHTML = content || ''
        handleInput()
      }
    }
    
    // 获取内容
    const getContent = () => {
      return editor.value ? editor.value.innerHTML : ''
    }
    
    // 聚焦编辑器
    const focus = () => {
      if (editor.value) {
        editor.value.focus()
      }
    }
    
    // 失焦编辑器
    const blur = () => {
      if (editor.value) {
        editor.value.blur()
      }
    }
    
    // 组件挂载后初始化
    onMounted(() => {
      nextTick(() => {
        if (editor.value) {
          editor.value.innerHTML = props.modelValue || ''
          // 设置placeholder样式
          if (!props.modelValue) {
            editor.value.classList.add('placeholder')
            editor.value.innerHTML = props.placeholder
          }
          
          // 处理placeholder逻辑
          editor.value.addEventListener('focus', () => {
            if (editor.value.classList.contains('placeholder')) {
              editor.value.classList.remove('placeholder')
              editor.value.innerHTML = ''
            }
          })
          
          editor.value.addEventListener('blur', () => {
            if (!editor.value.innerHTML.trim()) {
              editor.value.classList.add('placeholder')
              editor.value.innerHTML = props.placeholder
            }
          })
        }
      })
    })
    
    return {
      editor,
      fileInput,
      linkDialogVisible,
      imageDialogVisible,
      variableDialogVisible,
      colorPickerVisible,
      specialCharDialogVisible,
      fontFamilyDialogVisible,
      fontSizeDialogVisible,
      lineHeightDialogVisible,
      letterSpacingDialogVisible,
      findReplaceDialogVisible,
      wordCountDialogVisible,
      // 表格相关
      tableDialogVisible,
      tableForm,
      previewMode,
      fullScreenMode,
      linkForm,
      imageForm,
      variableForm,
      colorPickerForm,
      fontFamilyForm,
      fontSizeForm,
      lineHeightForm,
      letterSpacingForm,
      findReplaceForm,
      specialChars,
      fontFamilies,
      fontSizes,
      lineHeights,
      letterSpacings,
      wordCountResult,
      execCommand,
      insertUnorderedList,
      insertOrderedList,
      insertDefinitionList,
      insertLink,
      confirmInsertLink,
      insertImage,
      confirmInsertImage,
      insertAttachment,
      handleFileUpload,
      insertVariable,
      confirmInsertVariable,
      showColorPicker,
      confirmColorPicker,
      showFontFamilyPicker,
      confirmFontFamilyPicker,
      showFontSizePicker,
      confirmFontSizePicker,
      showLineHeightPicker,
      confirmLineHeightPicker,
      showLetterSpacingPicker,
      confirmLetterSpacingPicker,
      insertTable,
      confirmInsertTable,
      insertSpecialChar,
      confirmInsertSpecialChar,
      removeLink,
      showFindReplaceDialog,
      findText,
      replaceText,
      replaceAllText,
      showWordCount,
      calculateWordCount,
      toggleFullScreen,
      togglePreview,
      clearFormat,
      handleInput,
      handlePaste,
      handleKeydown,
      setContent,
      getContent,
      focus,
      blur
    }
  }
}
</script>

<style scoped>
.rich-text-editor {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.editor-toolbar {
  background-color: #f5f7fa;
  padding: 8px;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}

.editor-container {
  min-height: 300px;
  max-height: 600px;
  overflow-y: auto;
}

.editor-content {
  padding: 12px;
  min-height: 300px;
  outline: none;
  line-height: 1.6;
  font-size: 14px;
}

.editor-content:empty:before {
  content: attr(placeholder);
  color: #c0c4cc;
}

.editor-content.placeholder {
  color: #c0c4cc;
}

/* 内容样式 */
.editor-content h1 {
  font-size: 24px;
  margin: 16px 0;
}

.editor-content h2 {
  font-size: 20px;
  margin: 14px 0;
}

.editor-content h3 {
  font-size: 18px;
  margin: 12px 0;
}

.editor-content p {
  margin: 10px 0;
}

.editor-content ul, .editor-content ol {
  margin: 10px 0;
  padding-left: 24px;
}

.editor-content li {
  margin: 4px 0;
}

.editor-content a {
  color: #409eff;
  text-decoration: none;
}

.editor-content a:hover {
  text-decoration: underline;
}

.editor-content img {
  max-width: 100%;
  height: auto;
}

.editor-content hr {
  margin: 20px 0;
  border: none;
  border-top: 1px solid #dcdfe6;
}

/* 表格样式 */
.editor-content table {
  border-collapse: collapse;
  width: 100%;
  margin: 10px 0;
  background-color: white;
}

.editor-content table td,
.editor-content table th {
  border: 1px solid #dcdfe6;
  padding: 8px 12px;
  min-width: 80px;
  vertical-align: top;
  transition: all 0.2s ease;
}

.editor-content table td:hover,
.editor-content table th:hover {
  background-color: #f5f7fa;
}

.editor-content table tr:hover {
  background-color: #fafafa;
}

/* 确保表格单元格可以正常编辑 */
.editor-content table td[contenteditable="true"]:empty:before {
  content: " ";
  white-space: pre-wrap;
}

/* 确保表格在嵌套时也能正常显示 */
.editor-content table table {
  margin: 0;
  border: none;
  width: auto;
}

/* 确保表格单元格内的内容可以正常换行 */
.editor-content table td,
.editor-content table th {
  word-break: break-word;
  overflow-wrap: break-word;
}

/* 选中样式 */
.editor-content ::selection {
  background-color: #b3d4fc;
  color: #303133;
}

/* 特殊字符对话框样式 */
.special-chars-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  gap: 10px;
  max-height: 300px;
  overflow-y: auto;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.special-char-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 10px;
  background-color: white;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
  min-height: 60px;
}

.special-char-item:hover {
  background-color: #ecf5ff;
  border-color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.char-display {
  font-size: 24px;
  margin-bottom: 4px;
}

.char-name {
  font-size: 12px;
  color: #606266;
  text-align: center;
  line-height: 1.2;
}

/* 预览模式样式 */
.preview-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: white;
  z-index: 9999;
  display: flex;
  flex-direction: column;
}

.preview-header {
  padding: 20px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.preview-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.preview-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  line-height: 1.6;
  font-size: 16px;
}

/* 代码块样式 */
.editor-content pre {
  background-color: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  overflow-x: auto;
  font-family: 'Courier New', Courier, monospace;
  font-size: 14px;
  line-height: 1.5;
  margin: 10px 0;
}

/* 引用块样式 */
.editor-content blockquote {
  border-left: 4px solid #409eff;
  padding: 10px 15px;
  margin: 10px 0;
  background-color: #f0f9ff;
  color: #606266;
  font-style: italic;
}

/* 表格样式 */
.editor-content table {
  border-collapse: collapse;
  width: 100%;
  margin: 10px 0;
}

.editor-content table th,
.editor-content table td {
  border: 1px solid #dcdfe6;
  padding: 8px 12px;
  text-align: left;
}

.editor-content table th {
  background-color: #f5f7fa;
  font-weight: bold;
}

/* 全屏模式样式 */
.rich-text-editor.fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 9999;
  background-color: white;
  border: none;
  border-radius: 0;
}

.rich-text-editor.fullscreen .editor-container {
  max-height: calc(100vh - 60px);
}

/* 字数统计样式 */
.word-count-content {
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.word-count-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  padding: 10px;
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.word-count-item:last-child {
  margin-bottom: 0;
}

.word-count-label {
  font-weight: 500;
  color: #606266;
}

.word-count-value {
  font-weight: bold;
  color: #409eff;
  font-size: 18px;
}
</style>