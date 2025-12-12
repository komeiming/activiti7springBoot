<template>
  <div class="menu-management-container">
    <h1 class="page-title">菜单管理</h1>
    
    <!-- 操作按钮区域 -->
    <div class="action-bar">
      <el-button type="primary" @click="handleAddMenu">
        <el-icon><Plus /></el-icon>
        新增菜单
      </el-button>
      <el-button type="success" @click="handleExportMenus">
        <el-icon><Download /></el-icon>
        导出菜单
      </el-button>
      <el-upload
        ref="upload"
        :auto-upload="false"
        :on-change="handleImportFileChange"
        accept=".json"
        class="upload-btn"
      >
        <el-button type="warning">
          <el-icon><Upload /></el-icon>
          导入菜单
        </el-button>
      </el-upload>
      <el-button type="info" @click="handleRefreshCache">
        <el-icon><RefreshRight /></el-icon>
        刷新缓存
      </el-button>
      <el-button type="danger" @click="handleBatchDelete" :disabled="selectedMenus.length === 0">
        <el-icon><Delete /></el-icon>
        批量删除
      </el-button>
    </div>
    
    <!-- 菜单树表格 -->
    <el-card shadow="hover" class="menu-tree-card">
      <el-table
        :data="menuTree"
        border
        :expand-row-keys="defaultExpandedKeys"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        @selection-change="handleSelectionChange"
        @row-click="handleRowClick"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="菜单名称" width="200">
          <template #default="{ row }">
            <span class="menu-name">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="code" label="菜单编码" width="150" />
        <el-table-column prop="menuType" label="菜单类型" width="100">
          <template #default="{ row }">
            {{ menuTypeMap[row.menuType] || row.menuType }}
          </template>
        </el-table-column>
        <el-table-column prop="menuOrder" label="菜单顺序" width="100" />
        <el-table-column prop="path" label="路径" width="200" />
        <el-table-column prop="component" label="组件" width="200" />
        <el-table-column prop="hidden" label="隐藏" width="80">
          <template #default="{ row }">
            {{ row.hidden ? '是' : '否' }}
          </template>
        </el-table-column>
        <el-table-column prop="alwaysShow" label="总是显示" width="100">
          <template #default="{ row }">
            {{ row.alwaysShow ? '是' : '否' }}
          </template>
        </el-table-column>
        <el-table-column prop="url" label="URL" width="200" />
        <el-table-column prop="method" label="方法" width="80" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleEditMenu(row)"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDeleteMenu(row)"
            >
              删除
            </el-button>
            <el-button
              type="success"
              size="small"
              @click="handleAddSubMenu(row)"
            >
              添加子菜单
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 菜单操作日志 -->
    <el-card shadow="hover" class="log-card">
      <template #header>
        <div class="card-header">
          <h3>菜单操作日志</h3>
          <el-button type="primary" size="small" @click="refreshOperationLogs">
            <el-icon><RefreshRight /></el-icon>
            刷新日志
          </el-button>
        </div>
      </template>
      <el-table :data="operationLogs" stripe style="width: 100%">
        <el-table-column prop="operationTime" label="操作时间" width="180" />
        <el-table-column prop="operationType" label="操作类型" width="100" />
        <el-table-column prop="menuId" label="菜单ID" width="80" />
        <el-table-column prop="operator" label="操作人" width="100" />
        <el-table-column prop="description" label="操作描述" />
        <el-table-column prop="ipAddress" label="IP地址" width="120" />
      </el-table>
    </el-card>
    
    <!-- 新增/编辑菜单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      destroy-on-close
    >
      <el-form
        :model="formData"
        ref="menuForm"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="父菜单" prop="parentId">
          <el-select v-model="formData.parentId" placeholder="选择父菜单">
            <el-option label="根菜单" :value="0" />
            <el-tree-select
              v-model="formData.parentId"
              :data="menuOptions"
              :props="{ label: 'name', value: 'id' }"
              placeholder="选择父菜单"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="菜单名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入菜单名称" />
        </el-form-item>
        
        <el-form-item label="菜单编码" prop="code">
          <el-input v-model="formData.code" placeholder="请输入菜单编码" />
        </el-form-item>
        
        <el-form-item label="菜单类型" prop="menuType">
          <el-select v-model="formData.menuType" placeholder="选择菜单类型">
            <el-option label="目录" value="directory" />
            <el-option label="菜单" value="menu" />
            <el-option label="按钮" value="button" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="菜单顺序" prop="menuOrder">
          <el-input-number v-model="formData.menuOrder" :min="0" :step="1" placeholder="请输入菜单顺序" />
        </el-form-item>
        
        <el-form-item label="图标" prop="icon">
          <el-input v-model="formData.icon" placeholder="请输入图标名称，如：el-icon-menu" />
        </el-form-item>
        
        <el-form-item label="路径" prop="path">
          <el-input v-model="formData.path" placeholder="请输入路由路径" />
        </el-form-item>
        
        <el-form-item label="组件" prop="component">
          <el-input v-model="formData.component" placeholder="请输入组件路径" />
        </el-form-item>
        
        <el-form-item label="重定向">
          <el-input v-model="formData.redirect" placeholder="请输入重定向路径" />
        </el-form-item>
        
        <el-form-item label="隐藏">
          <el-switch v-model="formData.hidden" />
        </el-form-item>
        
        <el-form-item label="总是显示">
          <el-switch v-model="formData.alwaysShow" />
        </el-form-item>
        
        <el-form-item label="固定标签">
          <el-switch v-model="formData.affix" />
        </el-form-item>
        
        <el-form-item label="URL" prop="url">
          <el-input v-model="formData.url" placeholder="请输入接口URL" />
        </el-form-item>
        
        <el-form-item label="请求方法" prop="method">
          <el-select v-model="formData.method" placeholder="选择请求方法">
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入菜单描述"
          />
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
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Download, Upload, RefreshRight, Delete } from '@element-plus/icons-vue';
import MenuService from '../services/MenuService';
import eventBus from '../utils/eventBus';
import { ElTreeSelect } from 'element-plus'; // 导入el-tree-select组件

// 菜单类型映射
const menuTypeMap = {
  directory: '目录',
  menu: '菜单',
  button: '按钮'
};

// 响应式数据
const menuTree = ref([]);
const menuOptions = ref([]);
const selectedMenus = ref([]);
const dialogVisible = ref(false);
const dialogTitle = ref('新增菜单');
const defaultExpandedKeys = ref([]);
const formData = reactive({
  id: null,
  parentId: 0,
  name: '',
  code: '',
  menuType: 'menu',
  menuOrder: 0,
  icon: '',
  path: '',
  component: '',
  redirect: '',
  hidden: false,
  alwaysShow: false,
  affix: false,
  url: '',
  method: 'GET',
  description: ''
});
const formRules = reactive({
  name: [
    { required: true, message: '请输入菜单名称', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入菜单编码', trigger: 'blur' }
  ],
  menuType: [
    { required: true, message: '请选择菜单类型', trigger: 'change' }
  ]
});
const menuForm = ref(null);
const operationLogs = ref([]);
const upload = ref(null);

// 处理菜单数据，确保符合el-tree-select的要求
const processMenuOptions = (menus) => {
  if (!Array.isArray(menus)) {
    return [];
  }
  return menus.map(menu => {
    const processedMenu = {
      ...menu,
      // 确保id和name存在
      id: menu.id || '',
      name: menu.name || '',
      // 递归处理子菜单
      children: processMenuOptions(menu.children || [])
    };
    return processedMenu;
  });
};

// 方法
const loadMenus = async () => {
  try {
    const response = await MenuService.getAllMenus();
    menuTree.value = response;
    // 处理菜单数据，确保符合el-tree-select的要求
    menuOptions.value = processMenuOptions(response);
  } catch (error) {
    ElMessage.error('加载菜单失败: ' + error.message);
  }
};

const loadOperationLogs = async () => {
  try {
    const response = await MenuService.getMenuOperationLogs();
    operationLogs.value = response;
  } catch (error) {
    ElMessage.error('加载操作日志失败: ' + error.message);
  }
};

const handleAddMenu = () => {
  dialogTitle.value = '新增菜单';
  resetForm();
  dialogVisible.value = true;
};

const handleAddSubMenu = (parentMenu) => {
  dialogTitle.value = '新增子菜单';
  resetForm();
  formData.parentId = parentMenu.id;
  dialogVisible.value = true;
};

const handleEditMenu = (menu) => {
  dialogTitle.value = '编辑菜单';
  Object.assign(formData, JSON.parse(JSON.stringify(menu)));
  if (menu.parentId === null) {
    formData.parentId = 0;
  }
  dialogVisible.value = true;
};

const handleDeleteMenu = (menu) => {
  ElMessageBox.confirm(
    `确定要删除菜单 "${menu.name}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await MenuService.deleteMenu(menu.id);
      ElMessage.success('删除菜单成功');
      await loadMenus();
      await loadOperationLogs();
      // 触发菜单更新事件
      eventBus.emit('menuUpdated');
    } catch (error) {
      ElMessage.error('删除菜单失败: ' + error.message);
    }
  }).catch(() => {
    // 用户取消删除
  });
};

const handleBatchDelete = () => {
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedMenus.value.length} 个菜单吗？`,
    '批量删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const ids = selectedMenus.value.map(menu => menu.id);
      await MenuService.batchDeleteMenus(ids);
      ElMessage.success('批量删除菜单成功');
      await loadMenus();
      await loadOperationLogs();
      selectedMenus.value = [];
      // 触发菜单更新事件
      eventBus.emit('menuUpdated');
    } catch (error) {
      ElMessage.error('批量删除菜单失败: ' + error.message);
    }
  }).catch(() => {
    // 用户取消删除
  });
};

const handleExportMenus = async () => {
  try {
    const response = await MenuService.exportMenus();
    const menus = response;
    const jsonStr = JSON.stringify(menus, null, 2);
    const blob = new Blob([jsonStr], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `menus_${new Date().toISOString().slice(0, 10)}.json`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
    ElMessage.success('导出菜单成功');
    await loadOperationLogs();
  } catch (error) {
    ElMessage.error('导出菜单失败: ' + error.message);
  }
};

const handleImportFileChange = (file) => {
  const reader = new FileReader();
  reader.onload = async (e) => {
    try {
      const menus = JSON.parse(e.target.result);
      await handleImportMenus(menus);
    } catch (error) {
      ElMessage.error('导入菜单失败: 文件格式错误');
    }
  };
  reader.readAsText(file.raw);
};

const handleImportMenus = async (menus) => {
  try {
    await MenuService.importMenus(menus, false);
    ElMessage.success('导入菜单成功');
    await loadMenus();
    await loadOperationLogs();
    // 触发菜单更新事件
    eventBus.emit('menuUpdated');
  } catch (error) {
    ElMessage.error('导入菜单失败: ' + error.message);
  }
};

const handleRefreshCache = async () => {
  try {
    await MenuService.refreshMenuCache();
    ElMessage.success('刷新缓存成功');
    await loadMenus();
    await loadOperationLogs();
    // 触发菜单更新事件
    eventBus.emit('menuUpdated');
  } catch (error) {
    ElMessage.error('刷新缓存失败: ' + error.message);
  }
};

const handleSelectionChange = (selection) => {
  selectedMenus.value = selection;
};

const handleRowClick = (row) => {
  // 点击行时，展开/折叠子菜单
};

const handleSubmit = async () => {
  try {
    if (formData.id) {
      // 更新菜单
      await MenuService.updateMenu(formData.id, formData);
      ElMessage.success('更新菜单成功');
    } else {
      // 新增菜单
      await MenuService.createMenu(formData);
      ElMessage.success('新增菜单成功');
    }
    dialogVisible.value = false;
    await loadMenus();
    await loadOperationLogs();
    // 触发菜单更新事件
    eventBus.emit('menuUpdated');
  } catch (error) {
    ElMessage.error('保存菜单失败: ' + error.message);
  }
};

const resetForm = () => {
  Object.assign(formData, {
    id: null,
    parentId: 0,
    name: '',
    code: '',
    menuType: 'menu',
    menuOrder: 0,
    icon: '',
    path: '',
    component: '',
    redirect: '',
    hidden: false,
    alwaysShow: false,
    affix: false,
    url: '',
    method: 'GET',
    description: ''
  });
  if (menuForm.value) {
    menuForm.value.resetFields();
  }
};

const refreshOperationLogs = async () => {
  await loadOperationLogs();
};

// 生命周期钩子
onMounted(async () => {
  await loadMenus();
  await loadOperationLogs();
});
</script>

<style scoped>
.menu-management-container {
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

.action-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  align-items: center;
}

.upload-btn {
  display: inline-block;
}

.menu-tree-card {
  margin-bottom: 20px;
}

.log-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.menu-name {
  font-weight: 500;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>