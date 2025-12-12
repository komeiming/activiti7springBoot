<template>
  <div class="role-menu-permission-container">
    <h1 class="page-title">角色菜单权限配置</h1>
    
    <!-- 角色选择区域 -->
    <el-card shadow="hover" class="role-selection-card">
      <template #header>
        <div class="card-header">
          <span>选择角色</span>
        </div>
      </template>
      <div class="role-selection">
        <el-select v-model="selectedRoleId" placeholder="请选择角色" @change="handleRoleChange" style="width: 300px;">
          <el-option
            v-for="role in roles"
            :key="role.id"
            :label="role.roleName"
            :value="role.id"
          ></el-option>
        </el-select>
        <el-button type="primary" @click="handleSavePermission" :disabled="!selectedRoleId">
          <el-icon><Download /></el-icon>
          保存权限配置
        </el-button>
      </div>
    </el-card>
    
    <!-- 菜单权限配置区域 -->
    <el-card shadow="hover" class="permission-config-card">
      <template #header>
        <div class="card-header">
          <span>{{ selectedRoleName ? `${selectedRoleName} 角色的菜单权限配置` : '菜单权限配置' }}</span>
        </div>
      </template>
      <div class="permission-config">
        <el-tree
          ref="menuTreeRef"
          :data="menuTree"
          show-checkbox
          node-key="id"
          default-expand-all
          :props="treeProps"
          @check-change="handleCheckChange"
          :check-strictly="false"
          :loading="menuTree.length === 0"
        >
          <template #default="{ node, data }">
            <span class="custom-tree-node">
              <el-icon :size="18">
                <component :is="data.icon || MenuIcon" />
              </el-icon>
              <span>{{ node.label }}</span>
              <span class="menu-type-tag" :class="`menu-type-${data.menuType}`">
                {{ menuTypeMap[data.menuType] }}
              </span>
            </span>
          </template>
        </el-tree>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Download, Menu as MenuIcon } from '@element-plus/icons-vue';
import MenuService from '../services/MenuService';
import RoleService from '../services/RoleService';
import UserService from '../services/UserService';

// 菜单类型映射
const menuTypeMap = {
  directory: '目录',
  menu: '菜单',
  button: '按钮'
};

// 树节点配置
const treeProps = {
  label: 'name',
  children: 'children'
};

// 响应式数据
const roles = ref([]);
const menuTree = ref([]);
const selectedRoleId = ref(null);
const selectedRoleName = ref('');
const checkedMenuIds = ref(new Set());
const initialCheckedMenuIds = ref(new Set());
const menuTreeRef = ref(null);

// 计算属性：选中的菜单ID数组
const selectedMenuIds = computed(() => Array.from(checkedMenuIds.value));

// 加载角色列表
const loadRoles = async () => {
  try {
    const response = await RoleService.getRoles();
    roles.value = Array.isArray(response) ? response : response.data || [];
  } catch (error) {
    ElMessage.error('加载角色列表失败: ' + error.message);
  }
};

// 加载菜单树
const loadMenuTree = async () => {
  try {
    const response = await MenuService.getAllMenus();
    menuTree.value = Array.isArray(response) ? response : response.data || [];
  } catch (error) {
    ElMessage.error('加载菜单树失败: ' + error.message);
  }
};

// 加载角色已配置的菜单权限
const loadRoleMenuPermissions = async () => {
  if (!selectedRoleId.value) return;
  
  try {
    const response = await MenuService.getRoleMenuPermissions(selectedRoleId.value);
    // 由于axios拦截器已经处理了响应，response直接是权限ID列表
    const permissionIds = response || [];
    
    console.log('角色菜单权限ID列表:', permissionIds);
    
    // 重置选中状态
    checkedMenuIds.value = new Set(permissionIds);
    initialCheckedMenuIds.value = new Set(permissionIds);
    
    // 刷新树的选中状态
    await nextTick();
    if (menuTreeRef.value) {
      menuTreeRef.value.setCheckedKeys(Array.from(checkedMenuIds.value));
    }
  } catch (error) {
    console.error('加载角色菜单权限失败:', error);
    ElMessage.error('加载角色菜单权限失败: ' + error.message);
  }
};

// 处理角色选择变化
const handleRoleChange = async () => {
  if (!selectedRoleId.value) {
    selectedRoleName.value = '';
    return;
  }
  
  // 获取选中角色的名称
  const selectedRole = roles.value.find(role => role.id === selectedRoleId.value);
  if (selectedRole) {
    selectedRoleName.value = selectedRole.roleName;
  }
  
  // 加载该角色已配置的菜单权限
  await loadRoleMenuPermissions();
};

// 处理菜单选择变化
const handleCheckChange = (data, checked, indeterminate) => {
  if (checked) {
    checkedMenuIds.value.add(data.id);
  } else {
    checkedMenuIds.value.delete(data.id);
  }
};

// 保存角色菜单权限配置
const handleSavePermission = async () => {
  if (!selectedRoleId.value) {
    ElMessage.warning('请先选择角色');
    return;
  }
  
  try {
    const params = {
      roleId: selectedRoleId.value,
      menuIds: Array.from(checkedMenuIds.value)
    };
    
    await MenuService.saveRoleMenuPermissions(params);
    ElMessage.success('保存角色菜单权限配置成功');
    initialCheckedMenuIds.value = new Set(checkedMenuIds.value);
  } catch (error) {
    ElMessage.error('保存角色菜单权限配置失败: ' + error.message);
  }
};

// 检查是否为管理员
const isAdmin = computed(() => {
  return UserService.isAdmin();
});

// 生命周期钩子
onMounted(async () => {
  // 检查是否为管理员，非管理员不允许访问
  if (!isAdmin.value) {
    ElMessage.error('您没有权限访问此页面');
    // 跳转到首页
    window.location.href = '/';
    return;
  }
  
  await loadRoles();
  await loadMenuTree();
});

// 监听菜单树变化，刷新选中状态
watch(() => menuTree.value, async (newValue, oldValue) => {
  // 只有当菜单树真正有数据变化时才执行
  if (newValue && newValue.length > 0 && oldValue && oldValue.length !== newValue.length) {
    await nextTick();
    // 确保menuTreeRef和selectedRoleId都已初始化
    if (menuTreeRef.value && selectedRoleId.value) {
      try {
        menuTreeRef.value.setCheckedKeys(Array.from(checkedMenuIds.value));
      } catch (error) {
        console.error('刷新菜单选中状态失败:', error);
      }
    }
  }
});
</script>

<style scoped>
.role-menu-permission-container {
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

.role-selection-card {
  margin-bottom: 20px;
}

.permission-config-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.role-selection {
  display: flex;
  gap: 15px;
  align-items: center;
}

.permission-config {
  margin-top: 20px;
  max-height: 600px;
  overflow-y: auto;
  padding: 10px;
  background-color: #fff;
  border-radius: 8px;
}

.custom-tree-node {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  width: 100%;
  padding: 4px 0;
}

.menu-type-tag {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  margin-left: auto;
  font-weight: 500;
}

.menu-type-directory {
  background-color: #e6f7ff;
  color: #1890ff;
}

.menu-type-menu {
  background-color: #f6ffed;
  color: #52c41a;
}

.menu-type-button {
  background-color: #fff2e8;
  color: #fa8c16;
}
</style>