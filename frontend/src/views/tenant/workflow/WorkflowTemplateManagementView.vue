<template>
  <div class="workflow-template-container">
    <h3 class="section-title">流程模板管理</h3>
    
    <!-- 操作按钮和筛选条件 -->
    <el-card shadow="hover" class="filter-card">
      <div class="filter-section">
        <div class="filter-left">
          <el-button type="primary" @click="handleCreateTemplate">
            <el-icon><Plus /></el-icon> 创建流程模板
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
            v-model="filterForm.status"
            placeholder="状态"
            class="filter-select"
            clearable
          >
            <el-option label="未发布" value="draft" />
            <el-option label="已发布" value="published" />
            <el-option label="已下架" value="offline" />
          </el-select>
          
          <el-date-picker
            v-model="filterForm.timeRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            class="filter-date"
          />
          
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
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getTemplateStatusTagType(scope.row.status)" size="small">
              {{ getTemplateStatusName(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column prop="updatedAt" label="最后修改时间" width="180" />
        <el-table-column prop="action" label="操作" width="250">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEditTemplate(scope.row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button
              type="success"
              size="small"
              @click="handlePublish(scope.row)"
              :disabled="scope.row.status !== 'draft'"
            >
              <el-icon><Check /></el-icon> 发布
            </el-button>
            <el-button
              type="warning"
              size="small"
              @click="handleOffline(scope.row)"
              :disabled="scope.row.status !== 'published'"
            >
              <el-icon><Close /></el-icon> 下架
            </el-button>
            <el-button type="danger" size="small" @click="handleDeleteTemplate(scope.row)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
            <el-button type="info" size="small" @click="handlePreview(scope.row)">
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
    
    <!-- 创建/编辑模板对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      destroy-on-close
    >
      <el-form :model="templateForm" label-width="100px" :rules="rules" ref="templateFormRef">
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="templateForm.name" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="模板描述">
          <el-input v-model="templateForm.description" placeholder="请输入模板描述" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="流程JSON" prop="processJson">
          <el-input v-model="templateForm.processJson" placeholder="请输入流程JSON" type="textarea" :rows="6" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 预览模板对话框 -->
    <el-dialog
      v-model="previewVisible"
      title="模板预览"
      width="600px"
    >
      <div class="preview-content">
        <div class="preview-item">
          <label>模板名称：</label>
          <span>{{ previewTemplate.name }}</span>
        </div>
        <div class="preview-item">
          <label>模板状态：</label>
          <el-tag :type="getTemplateStatusTagType(previewTemplate.status)" size="small">
            {{ getTemplateStatusName(previewTemplate.status) }}
          </el-tag>
        </div>
        <div class="preview-item">
          <label>创建时间：</label>
          <span>{{ previewTemplate.createdAt }}</span>
        </div>
        <div class="preview-item">
          <label>最后修改：</label>
          <span>{{ previewTemplate.updatedAt }}</span>
        </div>
        <div class="preview-item">
          <label>模板描述：</label>
          <p>{{ previewTemplate.description || '无描述' }}</p>
        </div>
        <div class="preview-item">
          <label>流程JSON：</label>
          <pre>{{ previewTemplate.processJson }}</pre>
        </div>
      </div>
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

// 筛选条件
const filterForm = reactive({
  templateName: '',
  status: '',
  timeRange: []
})

// 模板列表数据
const templates = ref([])

// 分页数据
const currentPage = ref(1)
const pageSize = ref(10)
const totalTemplates = ref(0)
const selectedTemplates = ref([])
const loading = ref(false)

// 对话框相关
const dialogVisible = ref(false)
const previewVisible = ref(false)
const dialogTitle = ref('')
const templateFormRef = ref()
const isEditMode = ref(false)

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入模板名称', trigger: 'blur' },
    { min: 1, max: 50, message: '模板名称长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  processJson: [
    { required: true, message: '请输入流程JSON', trigger: 'blur' }
  ]
}

// 模板表单数据
const templateForm = reactive({
  id: '',
  name: '',
  description: '',
  processJson: '',
  status: 'draft'
})

// 预览模板数据
const previewTemplate = reactive({
  id: '',
  name: '',
  status: '',
  createdAt: '',
  updatedAt: '',
  description: '',
  processJson: ''
})

// 获取模板列表
const getTemplates = async () => {
  try {
    loading.value = true
    
    // 构建请求参数
    const params = {
      templateName: filterForm.templateName,
      status: filterForm.status,
      page: currentPage.value,
      size: pageSize.value
    }
    
    // 添加时间范围参数
    if (filterForm.timeRange && filterForm.timeRange.length === 2) {
      params.startTime = filterForm.timeRange[0]
      params.endTime = filterForm.timeRange[1]
    }
    
    // 调用后端API获取模板列表
    const response = await axios.get('/api/workflow/template', { params })
    
    // 更新模板数据 - 响应拦截器返回的是success字段为true的data，失败时会抛出错误
    templates.value = response.data || []
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

// 模板状态映射
const getTemplateStatusName = (status) => {
  const statusMap = {
    draft: '未发布',
    published: '已发布',
    offline: '已下架'
  }
  return statusMap[status] || status
}

// 模板状态标签样式
const getTemplateStatusTagType = (status) => {
  const statusMap = {
    draft: 'info',
    published: 'success',
    offline: 'warning'
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
  filterForm.status = ''
  filterForm.timeRange = []
  getTemplates()
}

// 批量删除
const handleBatchDelete = () => {
  if (selectedTemplates.value.length === 0) {
    ElMessage.warning('请选择要删除的模板')
    return
  }
  
  // 弹出确认对话框
  ElMessageBox.confirm('确定要删除选中的模板吗？', '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 批量删除模板
      for (const template of selectedTemplates.value) {
        await axios.delete(`/api/workflow/template/${template.id}`)
      }
      
      ElMessage.success(`已删除${selectedTemplates.value.length}个模板`)
      selectedTemplates.value = []
      getTemplates() // 刷新列表
    } catch (error) {
      console.error('批量删除模板失败:', error)
      ElMessage.error('批量删除模板失败: ' + (error.message || '请稍后重试'))
    }
  }).catch(() => {
    // 用户取消删除
  })
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
  // 重置表单
  resetForm()
  dialogTitle.value = '创建流程模板'
  isEditMode.value = false
  dialogVisible.value = true
}

// 编辑模板
const handleEditTemplate = (row) => {
  // 填充表单数据
  Object.assign(templateForm, row)
  dialogTitle.value = '编辑流程模板'
  isEditMode.value = true
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  templateForm.id = ''
  templateForm.name = ''
  templateForm.description = ''
  templateForm.processJson = ''
  templateForm.status = 'draft'
  if (templateFormRef.value) {
    templateFormRef.value.resetFields()
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!templateFormRef.value) return
  
  try {
    await templateFormRef.value.validate()
    
    if (isEditMode.value) {
      // 更新模板
      await axios.put(`/api/workflow/template/${templateForm.id}`, templateForm)
      ElMessage.success('模板更新成功')
    } else {
      // 创建模板
      await axios.post('/api/workflow/template', templateForm)
      ElMessage.success('模板创建成功')
    }
    
    // 关闭对话框
    dialogVisible.value = false
    // 刷新列表
    getTemplates()
  } catch (error) {
    if (error.name === 'Error') {
      // 表单验证通过，但API请求失败
      console.error(isEditMode.value ? '更新模板失败:' : '创建模板失败:', error)
      ElMessage.error((isEditMode.value ? '更新模板失败: ' : '创建模板失败: ') + (error.message || '请稍后重试'))
    }
    // 表单验证失败会自动处理，不需要额外处理
  }
}

// 发布模板
const handlePublish = (row) => {
  // 弹出确认对话框
  ElMessageBox.confirm(`确定要发布模板"${row.name}"吗？`, '发布确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'success'
  }).then(async () => {
    try {
        await axios.post(`/api/workflow/template/${row.id}/publish`)
        ElMessage.success(`已发布模板：${row.name}`)
        getTemplates() // 刷新列表
      } catch (error) {
        console.error('发布模板失败:', error)
        ElMessage.error('发布模板失败: ' + (error.message || '请稍后重试'))
      }
  }).catch(() => {
    // 用户取消发布
  })
}

// 下架模板
const handleOffline = (row) => {
  // 弹出确认对话框
  ElMessageBox.confirm(`确定要下架模板"${row.name}"吗？`, '下架确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
        await axios.post(`/api/workflow/template/${row.id}/offline`)
        ElMessage.success(`已下架模板：${row.name}`)
        getTemplates() // 刷新列表
      } catch (error) {
        console.error('下架模板失败:', error)
        ElMessage.error('下架模板失败: ' + (error.message || '请稍后重试'))
      }
  }).catch(() => {
    // 用户取消下架
  })
}

// 删除模板
const handleDeleteTemplate = (row) => {
  // 弹出确认对话框
  ElMessageBox.confirm(`确定要删除模板"${row.name}"吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'error'
  }).then(async () => {
    try {
        await axios.delete(`/api/workflow/template/${row.id}`)
        ElMessage.success(`已删除模板：${row.name}`)
        getTemplates() // 刷新列表
      } catch (error) {
        console.error('删除模板失败:', error)
        ElMessage.error('删除模板失败: ' + (error.message || '请稍后重试'))
      }
  }).catch(() => {
    // 用户取消删除
  })
}

// 预览模板
const handlePreview = (row) => {
  // 填充预览数据
  Object.assign(previewTemplate, row)
  previewVisible.value = true
}
</script>

<style scoped>
.workflow-template-container {
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

.filter-date {
  width: 300px;
}

.template-card {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 预览对话框样式 */
.preview-content {
  padding: 10px 0;
}

.preview-item {
  margin-bottom: 15px;
  line-height: 1.5;
}

.preview-item label {
  font-weight: 500;
  margin-right: 8px;
  color: #303133;
}

.preview-item pre {
  margin: 8px 0 0 0;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  overflow-x: auto;
  font-size: 12px;
  line-height: 1.4;
}

.preview-item p {
  margin: 0;
  word-break: break-word;
}
</style>