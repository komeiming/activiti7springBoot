<template>
  <div class="template-management-container">
    <h3 class="section-title">通知模板管理</h3>
    
    <!-- 操作按钮和筛选条件 -->
    <el-card shadow="hover" class="filter-card">
      <div class="filter-section">
        <div class="filter-left">
          <el-button type="primary" @click="handleCreateTemplate">
            <el-icon><Plus /></el-icon> 创建模板
          </el-button>
          <el-button type="danger" @click="handleBatchDelete" :disabled="selectedTemplates.length === 0">
            <el-icon><Delete /></el-icon> 批量删除
          </el-button>
        </div>
        
        <div class="filter-right">
          <el-input
            v-model="filterForm.templateName"
            placeholder="模板名称"
            clearable
            class="filter-input"
            @keyup.enter="handleSearch"
          />
          
          <el-select
            v-model="filterForm.templateType"
            placeholder="模板类型"
            class="filter-select"
            clearable
          >
            <el-option label="短信" value="sms" />
            <el-option label="邮件" value="email" />
            <el-option label="APP推送" value="app" />
            <el-option label="站内信" value="internal" />
          </el-select>
          
          <el-select
            v-model="filterForm.status"
            placeholder="状态"
            class="filter-select"
            clearable
          >
            <el-option label="待审核" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已禁用" value="disabled" />
          </el-select>
          
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          
          <el-button @click="handleResetFilter">重置</el-button>
        </div>
      </div>
    </el-card>
    
    <!-- 模板列表 -->
    <el-card shadow="hover" class="template-card">
      <el-table
        :data="templates"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
        @row-click="handleRowClick"
        row-key="id"
        v-loading="loading"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="模板ID" width="120" />
        <el-table-column prop="name" label="模板名称" width="200" />
        <el-table-column prop="type" label="模板类型" width="120">
          <template #default="scope">
            <el-tag :type="getTemplateTypeTagType(scope.row.type)" size="small">
              {{ getTemplateTypeName(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getTemplateStatusTagType(scope.row.status)" size="small">
              {{ getTemplateStatusName(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="180" />
        <el-table-column prop="updatedTime" label="最后修改时间" width="180" />
        <el-table-column prop="action" label="操作" width="200">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEditTemplate(scope.row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDeleteTemplate(scope.row)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
            <el-button type="warning" size="small" @click="handleToggleStatus(scope.row)">
              <el-icon>
                <Close v-if="scope.row.status === 'ENABLED'" />
                <Check v-else />
              </el-icon>
              {{ scope.row.status === 'ENABLED' ? '禁用' : '启用' }}
            </el-button>
            <el-button type="success" size="small" @click="handlePreviewTemplate(scope.row)">
              <el-icon><View /></el-icon> 预览
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalTemplates"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 模板创建/编辑弹窗 -->
    <el-dialog
      v-model="templateDialogVisible"
      :title="templateDialogTitle"
      width="600px"
    >
      <el-form ref="templateForm" :model="formData" label-width="120px">
          <el-form-item label="模板名称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入模板名称" maxlength="50" />
          </el-form-item>
          
          <el-form-item label="模板编码" prop="code">
            <el-input v-model="formData.code" placeholder="请输入模板编码，留空自动生成" maxlength="100" />
          </el-form-item>
          
          <el-form-item label="模板类型" prop="type">
            <el-select v-model="formData.type" placeholder="请选择模板类型">
              <el-option label="短信" value="sms" />
              <el-option label="邮件" value="email" />
              <el-option label="APP推送" value="app" />
              <el-option label="站内信" value="internal" />
            </el-select>
          </el-form-item>
        
        <el-form-item label="模板标签">
          <el-tag
            v-for="tag in formData.tags"
            :key="tag"
            closable
            @close="handleTagClose(tag)"
            class="template-tag"
          >
            {{ tag }}
          </el-tag>
          <el-input
            v-if="showTagInput"
            v-model="newTag"
            ref="tagInputRef"
            placeholder="输入标签后按Enter添加"
            class="tag-input"
            @keyup.enter="handleAddTag"
            @blur="handleTagInputBlur"
          />
          <el-button
            v-else
            type="dashed"
            @click="showTagInput = true"
            class="add-tag-btn"
          >
            <el-icon><Plus /></el-icon> 添加标签
          </el-button>
        </el-form-item>
        
        <el-form-item label="模板内容" prop="content">
          <!-- 邮件模板使用富文本编辑器 -->
          <rich-text-editor
            v-if="formData.type === 'email'"
            v-model="formData.content"
            :placeholder="'请输入邮件内容，支持${变量名}格式的占位符'"
          />
          
          <!-- 其他模板使用短信编辑器 -->
          <sms-editor
            v-else
            v-model="formData.content"
            :placeholder="'请输入模板内容，支持${变量名}格式的占位符'"
          />
        </el-form-item>
        
        <!-- 移除了变量配置功能，改为根据模板内容中的${变量名}自动识别 -->
        <el-form-item label="变量说明">
          <el-input
            type="textarea"
            placeholder="模板内容中支持使用${变量名}格式的占位符，如：${username}、${date}"
            readonly
            rows="2"
            class="variable-tip"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="templateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveTemplate">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 模板预览弹窗 -->
    <el-dialog
      v-model="previewDialogVisible"
      title="模板预览"
      width="600px"
    >
      <div v-if="previewTemplate" class="preview-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="模板名称">{{ previewTemplate.name }}</el-descriptions-item>
          <el-descriptions-item label="模板类型">{{ getTemplateTypeName(previewTemplate.type) }}</el-descriptions-item>
          <el-descriptions-item label="模板内容">
            <!-- 使用v-html渲染富文本内容 -->
            <div class="template-content" v-html="previewTemplate.content"></div>
          </el-descriptions-item>
          <el-descriptions-item label="变量列表">
            <div class="variable-list">
              <el-tag v-for="variable in previewTemplate.variables" :key="variable.name">
                {{ variable.name }} ({{ variable.type }}{{ variable.required ? ', 必填' : '' }})
              </el-tag>
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="previewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Delete, Search, Edit, Check, Close, View
} from '@element-plus/icons-vue'
import axios from '@/utils/axiosConfig'
// 引入富文本编辑器和短信编辑器组件
import RichTextEditor from '@/components/RichTextEditor.vue'
import SmsEditor from '@/components/SmsEditor.vue'

// 模板列表数据
const templates = ref([])

// 筛选条件
const filterForm = reactive({
  templateName: '',
  templateType: '',
  status: ''
})

// 分页数据
const currentPage = ref(1)
const pageSize = ref(10)
const totalTemplates = ref(0)
const selectedTemplates = ref([])
const loading = ref(false)

// 获取模板列表
const getTemplates = async () => {
  try {
    loading.value = true
    
    // 构建请求参数
    const params = {
      templateName: filterForm.templateName,
      type: filterForm.templateType,
      status: filterForm.status,
      page: currentPage.value,
      size: pageSize.value
    }
    
    // 调用后端API获取模板列表
    const response = await axios.get('/api/notification/templates', { params })
    
    // 更新模板数据
    templates.value = response.list || []
    totalTemplates.value = response.total || 0
    
  } catch (error) {
    console.error('获取模板列表失败:', error)
    ElMessage.error('获取模板列表失败: ' + (error.message || '请稍后重试'))
  } finally {
    loading.value = false
  }
}

// 组件挂载时获取模板列表
onMounted(() => {
  getTemplates()
})

// 模板弹窗相关
const templateDialogVisible = ref(false)
const templateDialogTitle = ref('创建模板')
const formData = reactive({
  id: '',
  name: '',
  code: '',
  type: '',
  content: '',
  variables: [],
  tags: []
})

// 标签相关
const showTagInput = ref(false)
const newTag = ref('')
const tagInputRef = ref(null)

// 预览弹窗相关
const previewDialogVisible = ref(false)
const previewTemplate = ref(null)

// 模板类型映射
const getTemplateTypeName = (type) => {
  const typeMap = {
    sms: '短信',
    email: '邮件',
    app: 'APP推送',
    internal: '站内信'
  }
  return typeMap[type] || type
}

// 模板类型标签样式
const getTemplateTypeTagType = (type) => {
  const typeMap = {
    sms: 'success',
    email: 'info',
    app: 'warning',
    internal: 'primary'
  }
  return typeMap[type] || 'info'
}

// 模板状态映射
const getTemplateStatusName = (status) => {
  const statusMap = {
    ENABLED: '已启用',
    DISABLED: '已禁用',
    PENDING: '待审核',
    APPROVED: '已通过'
  }
  return statusMap[status] || status
}

// 模板状态标签样式
const getTemplateStatusTagType = (status) => {
  const statusMap = {
    ENABLED: 'success',
    DISABLED: 'danger',
    PENDING: 'warning',
    APPROVED: 'success'
  }
  return statusMap[status] || 'info'
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  getTemplates()
}

// 重置筛选条件
const handleResetFilter = () => {
  filterForm.templateName = ''
  filterForm.templateType = ''
  filterForm.status = ''
  currentPage.value = 1
  getTemplates()
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedTemplates.value.length === 0) {
    ElMessage.warning('请选择要删除的模板')
    return
  }
  
  try {
    // 批量删除模板
      for (const template of selectedTemplates.value) {
        await axios.delete(`/api/notification/templates/${template.id}`)
      }
    
    ElMessage.success(`已删除${selectedTemplates.value.length}个模板`)
    selectedTemplates.value = []
    getTemplates()
  } catch (error) {
    console.error('批量删除模板失败:', error)
    ElMessage.error('批量删除模板失败: ' + (error.message || '请稍后重试'))
  }
}

// 选择模板
const handleSelectionChange = (selection) => {
  selectedTemplates.value = selection
}

// 行点击
const handleRowClick = (row) => {
  // 可以在这里处理行点击事件
}

// 分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  getTemplates()
}

// 页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  getTemplates()
}

// 创建模板
const handleCreateTemplate = () => {
  templateDialogTitle.value = '创建模板'
  formData.id = ''
  formData.name = ''
  formData.type = ''
  formData.content = ''
  formData.tags = []
  templateDialogVisible.value = true
}

// 编辑模板
const handleEditTemplate = (row) => {
  templateDialogTitle.value = '编辑模板'
  formData.id = row.id || ''
  formData.name = row.name || ''
  formData.code = row.code || ''
  formData.type = row.type || 'email'
  formData.content = row.content || ''
  // 处理tags可能为undefined的情况
  formData.tags = row.tags ? [...row.tags] : []
  templateDialogVisible.value = true
}

// 删除模板
const handleDeleteTemplate = async (row) => {
  try {
    // 调用后端API删除模板
    await axios.delete(`/api/notification/templates/${row.id}`)
    
    ElMessage.success(`已删除模板：${row.name}`)
    getTemplates()
    
  } catch (error) {
    console.error('删除模板失败:', error)
    ElMessage.error('删除模板失败: ' + (error.message || '请稍后重试'))
  }
}

// 切换模板状态
const handleToggleStatus = async (row) => {
  try {
    // 构建新状态
    const newStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
    
    // 调用后端API更新模板状态
    const apiPath = newStatus === 'ENABLED' ? 
      `/api/notification/templates/${row.id}/enable` : 
      `/api/notification/templates/${row.id}/disable`
    
    await axios.put(apiPath)
    
    ElMessage.success(`已${newStatus === 'ENABLED' ? '启用' : '禁用'}模板：${row.name}`)
    getTemplates()
    
  } catch (error) {
    console.error('更新模板状态失败:', error)
    ElMessage.error('更新模板状态失败: ' + (error.message || '请稍后重试'))
  }
}

// 预览模板
const handlePreviewTemplate = (row) => {
  previewTemplate.value = row
  previewDialogVisible.value = true
}

// 保存模板
const handleSaveTemplate = async () => {
  try {
    // 验证表单
    if (!formData.name) {
      ElMessage.error('请输入模板名称')
      return
    }
    if (!formData.type) {
      ElMessage.error('请选择模板类型')
      return
    }
    if (!formData.content) {
      ElMessage.error('请输入模板内容')
      return
    }
    
    // 构建请求数据
    const templateData = {
      name: formData.name,
      code: formData.code,
      type: formData.type,
      content: formData.content,
      tags: formData.tags
    }
    
    let response
    if (formData.id) {
      // 编辑模板
      response = await axios.put(`/api/notification/templates/${formData.id}`, templateData)
    } else {
      // 创建模板
      response = await axios.post('/api/notification/templates', templateData)
    }
    
    ElMessage.success('模板保存成功')
    templateDialogVisible.value = false
    getTemplates()
    
  } catch (error) {
    console.error('保存模板失败:', error)
    ElMessage.error('保存模板失败: ' + (error.message || '请稍后重试'))
  }
}



// 添加标签
const handleAddTag = () => {
  if (newTag.value && !formData.tags.includes(newTag.value)) {
    formData.tags.push(newTag.value)
    newTag.value = ''
    showTagInput.value = false
  }
}

// 关闭标签
const handleTagClose = (tag) => {
  const index = formData.tags.indexOf(tag)
  if (index !== -1) {
    formData.tags.splice(index, 1)
  }
}

// 标签输入框失焦
const handleTagInputBlur = () => {
  if (newTag.value) {
    handleAddTag()
  } else {
    showTagInput.value = false
  }
}

// 添加变量
const handleAddVariable = () => {
  formData.variables.push({
    name: '',
    type: 'string',
    required: false
  })
}

// 删除变量
const handleDeleteVariable = (index) => {
  formData.variables.splice(index, 1)
}
</script>

<style scoped>
.template-management-container {
  padding: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #303133;
}

.filter-card {
  margin-bottom: 16px;
}

.filter-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.filter-left {
  display: flex;
  gap: 10px;
}

.filter-right {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.filter-input {
  width: 200px;
}

.filter-select {
  width: 150px;
}

.template-card {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.template-tag {
  margin-right: 10px;
  margin-bottom: 10px;
}

.tag-input {
  width: 200px;
}

.preview-content {
  padding: 10px 0;
}

.template-content {
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  white-space: pre-wrap;
}

.variable-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
</style>