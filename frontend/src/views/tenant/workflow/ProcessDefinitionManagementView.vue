<template>
  <div class="process-definition-management">
    <h3 class="section-title">流程定义管理</h3>
    
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>流程定义管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="openDeployDialog">
              <el-icon><Upload /></el-icon> 部署流程
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-section">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="searchForm.key"
              placeholder="流程标识"
              clearable
              @keyup.enter="handleSearch"
            ></el-input>
          </el-col>
          <el-col :span="6">
            <el-input
              v-model="searchForm.name"
              placeholder="流程名称"
              clearable
              @keyup.enter="handleSearch"
            ></el-input>
          </el-col>
          <el-col :span="6">
            <el-select
              v-model="searchForm.suspended"
              placeholder="状态"
              clearable
            >
              <el-option label="激活" :value="false"></el-option>
              <el-option label="挂起" :value="true"></el-option>
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-col>
        </el-row>
      </div>

      <!-- 流程定义列表 -->
      <div class="process-definition-list">
        <el-table
          v-loading="loading"
          :data="processDefinitionsData"
          style="width: 100%"
          border
        >
          <el-table-column prop="id" label="流程定义ID" width="200" show-overflow-tooltip></el-table-column>
          <el-table-column prop="name" label="流程名称" width="180"></el-table-column>
          <el-table-column prop="key" label="流程标识" width="180"></el-table-column>
          <el-table-column prop="version" label="版本" width="80"></el-table-column>
          <el-table-column prop="resourceName" label="资源名称" show-overflow-tooltip></el-table-column>
          <el-table-column prop="deploymentId" label="部署ID" width="180" show-overflow-tooltip></el-table-column>
          <el-table-column prop="suspended" label="状态" width="80">
            <template #default="scope">
              <el-tag :type="scope.row.suspended ? 'warning' : 'success'">
                {{ scope.row.suspended ? '挂起' : '激活' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250" fixed="right">
            <template #default="scope">
              <el-button
                size="small"
                @click="scope.row.suspended ? activateProcess(scope.row) : suspendProcess(scope.row)"
              >
                {{ scope.row.suspended ? '激活' : '挂起' }}
              </el-button>
              <el-button
                size="small"
                type="danger"
                @click="deleteProcessDefinition(scope.row)"
              >
                删除
              </el-button>
              <el-button
                size="small"
                @click="viewProcessImage(scope.row)"
              >
                查看图
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="pagination.currentPage"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="pagination.total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          ></el-pagination>
        </div>
      </div>
    </el-card>

    <!-- 部署流程对话框 -->
    <el-dialog
      v-model="deployDialogVisible"
      title="部署流程"
      width="500px"
    >
      <el-form :model="deployForm" label-width="100px">
        <el-form-item label="部署名称" prop="name">
          <el-input v-model="deployForm.name" placeholder="请输入部署名称"></el-input>
        </el-form-item>
        <el-form-item label="流程文件" prop="file">
          <el-upload
            class="upload-demo"
            ref="uploadRef"
            :auto-upload="false"
            :file-list="fileList"
            accept=".bpmn,.bpmn20.xml"
            :on-change="handleFileChange"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                请上传 .bpmn 或 .bpmn20.xml 格式的文件
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeDeployDialog">取消</el-button>
          <el-button type="primary" @click="submitDeploy">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 流程图片查看对话框 -->
    <el-dialog
      v-model="processImageDialogVisible"
      :title="`${selectedProcessDefinition?.name || ''}流程图`"
      width="1000px"
    >
      <div class="process-image-container">
        <div v-if="processImageUrl" class="process-image-wrapper">
          <h4 class="section-title">原始流程图</h4>
          <div class="image-content">
            <img :src="processImageUrl" alt="流程图" class="process-image" />
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="processImageDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, Search } from '@element-plus/icons-vue'
import axios from '@/utils/axiosConfig'

// 响应式数据
const loading = ref(false)
const deployDialogVisible = ref(false)
const processImageDialogVisible = ref(false)
const uploadRef = ref(null)
const fileList = ref([])
const processDefinitions = ref([])
const selectedProcessDefinition = ref(null)
const processImageUrl = ref('')

// 流程定义列表数据计算属性
const processDefinitionsData = computed(() => processDefinitions.value)

// 搜索表单
const searchForm = reactive({
  key: '',
  name: '',
  suspended: ''
})

// 部署表单
const deployForm = reactive({
  name: '',
  file: null
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 加载流程定义列表
const loadProcessDefinitions = async () => {
  try {
    loading.value = true
    
    // 构建请求参数
    const params = {
      key: searchForm.key,
      name: searchForm.name,
      status: searchForm.suspended ? 'suspended' : (searchForm.suspended === false ? 'active' : ''),
      page: pagination.currentPage,
      size: pagination.pageSize
    }
    
    // 调用后端API获取流程定义列表
    const response = await axios.get('/api/workflow/process/definition', { params })
    
    // 更新流程定义数据
    processDefinitions.value = response.data || []
    pagination.total = response.total || 0
    
  } catch (error) {
    console.error('获取流程定义列表失败:', error)
    ElMessage.error('获取流程定义列表失败: ' + (error.message || '请稍后重试'))
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  loadProcessDefinitions()
}

// 重置搜索
const resetSearch = () => {
  searchForm.key = ''
  searchForm.name = ''
  searchForm.suspended = ''
  pagination.currentPage = 1
  loadProcessDefinitions()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadProcessDefinitions()
}

// 当前页变化
const handleCurrentChange = (page) => {
  pagination.currentPage = page
  loadProcessDefinitions()
}

// 打开部署对话框
const openDeployDialog = () => {
  deployForm.name = ''
  fileList.value = []
  deployForm.file = null
  deployDialogVisible.value = true
}

// 关闭部署对话框
const closeDeployDialog = () => {
  deployDialogVisible.value = false
}

// 文件变化处理
const handleFileChange = (file, fileList) => {
  if (file.raw) {
    deployForm.file = file.raw
  }
}

// 提交部署
const submitDeploy = async () => {
  if (!deployForm.file) {
    ElMessage.warning('请选择流程文件')
    return
  }

  try {
    loading.value = true
    
    // 创建FormData对象
    const formData = new FormData()
    formData.append('file', deployForm.file)
    formData.append('name', deployForm.name)
    
    // 调用后端API部署流程
    await axios.post('/api/workflow/process/definition/deploy', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    ElMessage.success('流程部署成功')
    closeDeployDialog()
    loadProcessDefinitions()
  } catch (error) {
    console.error('部署流程失败:', error)
    ElMessage.error('部署流程失败: ' + (error.message || '请稍后重试'))
  } finally {
    loading.value = false
  }
}

// 挂起流程
const suspendProcess = async (processDefinition) => {
  ElMessageBox.confirm(`确定要挂起流程 ${processDefinition.name} 吗？`, '确认操作', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await axios.put(`/api/workflow/process/definition/${processDefinition.id}/suspend`)
      ElMessage.success('流程挂起成功')
      loadProcessDefinitions()
    } catch (error) {
      console.error('挂起流程失败:', error)
      ElMessage.error('流程挂起失败: ' + (error.message || '请稍后重试'))
    }
  }).catch(() => {
    // 取消操作
  })
}

// 激活流程
const activateProcess = async (processDefinition) => {
  try {
    await axios.put(`/api/workflow/process/definition/${processDefinition.id}/activate`)
    ElMessage.success('流程激活成功')
    loadProcessDefinitions()
  } catch (error) {
    console.error('激活流程失败:', error)
    ElMessage.error('流程激活失败: ' + (error.message || '请稍后重试'))
  }
}

// 删除流程定义
const deleteProcessDefinition = async (processDefinition) => {
  ElMessageBox.confirm(`确定要删除流程 ${processDefinition.name} 吗？删除后将无法恢复。`, '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'danger'
  }).then(async () => {
    try {
      await axios.delete(`/api/workflow/process/definition/${processDefinition.id}`)
      ElMessage.success('流程删除成功')
      loadProcessDefinitions()
    } catch (error) {
      console.error('删除流程失败:', error)
      ElMessage.error('流程删除失败: ' + (error.message || '请稍后重试'))
    }
  }).catch(() => {
    // 取消操作
  })
}

// 查看流程图片
const viewProcessImage = async (processDefinition) => {
  selectedProcessDefinition.value = processDefinition
  processImageDialogVisible.value = true
  
  try {
    loading.value = true
    
    // 调用后端API获取流程图
    const response = await axios.get(`/api/workflow/process/definition/${processDefinition.id}/diagram`, {
      responseType: 'blob'
    })
    
    // 将blob转换为Data URL
    const blob = new Blob([response], { type: 'image/png' })
    processImageUrl.value = URL.createObjectURL(blob)
    
  } catch (error) {
    console.error('获取流程图失败:', error)
    ElMessage.error('获取流程图失败: ' + (error.message || '请稍后重试'))
  } finally {
    loading.value = false
  }
}

// 初始化
onMounted(() => {
  loadProcessDefinitions()
})
</script>

<style scoped>
.process-definition-management {
  padding: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #303133;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-section {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.process-definition-list {
  margin-top: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.process-image-container {
  display: flex;
  justify-content: center;
  overflow: auto;
  max-height: 600px;
}

.process-image {
  max-width: 100%;
  max-height: 600px;
}

.process-image-wrapper {
  width: 100%;
}

.section-title {
  margin: 0 0 10px 0;
  font-size: 14px;
  font-weight: bold;
  color: #333;
}

.image-content {
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  overflow: auto;
  max-height: 600px;
  background-color: #fafafa;
  padding: 10px;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>