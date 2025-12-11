<template>
  <div class="language-management-container">
    <h1 class="page-title">多语言配置</h1>
    
    <el-card shadow="hover" class="language-card">
      <template #header>
        <div class="card-header">
          <h3>语言列表</h3>
          <el-button type="primary" @click="handleAddLanguage">
            <el-icon><Plus /></el-icon>
            新增语言
          </el-button>
        </div>
      </template>
      <el-table :data="languages" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="code" label="语言代码" width="120" />
        <el-table-column prop="name" label="语言名称" width="150" />
        <el-table-column prop="isDefault" label="默认语言" width="120">
          <template #default="scope">
            <el-switch v-model="scope.row.isDefault" @change="handleSetDefault(scope.row)" />
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="180" />
        <el-table-column prop="updatedTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEditLanguage(scope.row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDeleteLanguage(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 新增/编辑语言对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      destroy-on-close
    >
      <el-form
        :model="formData"
        ref="languageForm"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="语言代码" prop="code">
          <el-input v-model="formData.code" placeholder="请输入语言代码，如：zh-CN, en-US" />
        </el-form-item>
        <el-form-item label="语言名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入语言名称，如：中文, English" />
        </el-form-item>
        <el-form-item label="默认语言">
          <el-switch v-model="formData.isDefault" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';

// 响应式数据
const languages = ref([
  {
    id: 1,
    code: 'zh-CN',
    name: '中文',
    isDefault: true,
    createdTime: '2023-01-01 00:00:00',
    updatedTime: '2023-01-01 00:00:00'
  },
  {
    id: 2,
    code: 'en-US',
    name: 'English',
    isDefault: false,
    createdTime: '2023-01-01 00:00:00',
    updatedTime: '2023-01-01 00:00:00'
  }
]);
const dialogVisible = ref(false);
const dialogTitle = ref('新增语言');
const formData = reactive({
  id: null,
  code: '',
  name: '',
  isDefault: false
});
const formRules = reactive({
  code: [
    { required: true, message: '请输入语言代码', trigger: 'blur' },
    { pattern: /^[a-zA-Z]+-[a-zA-Z]+$/, message: '请输入有效的语言代码，如：zh-CN, en-US', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入语言名称', trigger: 'blur' }
  ]
});
const languageForm = ref(null);

// 方法
const handleAddLanguage = () => {
  dialogTitle.value = '新增语言';
  resetForm();
  dialogVisible.value = true;
};

const handleEditLanguage = (language) => {
  dialogTitle.value = '编辑语言';
  Object.assign(formData, { ...language });
  dialogVisible.value = true;
};

const handleDeleteLanguage = (language) => {
  if (language.isDefault) {
    ElMessage.warning('默认语言不能删除');
    return;
  }
  
  ElMessageBox.confirm(
    `确定要删除语言 "${language.name}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 模拟删除操作
    ElMessage.success('删除语言成功');
    const index = languages.value.findIndex(lang => lang.id === language.id);
    if (index !== -1) {
      languages.value.splice(index, 1);
    }
  }).catch(() => {
    // 用户取消删除
  });
};

const handleSetDefault = (language) => {
  // 模拟设置默认语言操作
  languages.value.forEach(lang => {
    lang.isDefault = lang.id === language.id;
  });
  ElMessage.success('默认语言设置成功');
};

const handleSubmit = async () => {
  try {
    // 模拟保存操作
    await new Promise(resolve => setTimeout(resolve, 500));
    
    if (formData.id) {
      // 更新语言
      const index = languages.value.findIndex(lang => lang.id === formData.id);
      if (index !== -1) {
        languages.value[index] = { ...formData };
        if (formData.isDefault) {
          languages.value.forEach(lang => {
            if (lang.id !== formData.id) {
              lang.isDefault = false;
            }
          });
        }
      }
      ElMessage.success('更新语言成功');
    } else {
      // 新增语言
      const newLanguage = {
        ...formData,
        id: languages.value.length + 1,
        createdTime: new Date().toLocaleString(),
        updatedTime: new Date().toLocaleString()
      };
      languages.value.push(newLanguage);
      if (formData.isDefault) {
        languages.value.forEach(lang => {
          if (lang.id !== newLanguage.id) {
            lang.isDefault = false;
          }
        });
      }
      ElMessage.success('新增语言成功');
    }
    
    dialogVisible.value = false;
  } catch (error) {
    ElMessage.error('保存语言失败');
  }
};

const resetForm = () => {
  Object.assign(formData, {
    id: null,
    code: '',
    name: '',
    isDefault: false
  });
  if (languageForm.value) {
    languageForm.value.resetFields();
  }
};

// 生命周期钩子
onMounted(() => {
  // 模拟从API获取语言列表
  console.log('加载语言列表');
});
</script>

<style scoped>
.language-management-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #333;
}

.language-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>