<template>
  <div class="notification-template-management">
    <div class="page-header">
      <h1>通知模板管理</h1>
      <el-button type="primary" size="large" @click="handleAddTemplate" icon="Plus">
        新建模板
      </el-button>
    </div>

    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="demo-form-inline">
        <el-form-item label="模板名称">
          <el-input v-model="searchForm.name" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="模板类型">
          <el-select v-model="searchForm.type" placeholder="请选择模板类型">
            <el-option label="邮件" value="EMAIL" />
            <el-option label="短信" value="SMS" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态">
            <el-option label="启用" value="ENABLED" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table
      v-loading="loading"
      :data="templatesData"
      style="width: 100%"
      border
      @row-click="handleRowClick"
    >
      <el-table-column prop="id" label="模板ID" width="180" />
      <el-table-column prop="name" label="模板名称" min-width="180" />
      <el-table-column prop="code" label="模板编码" min-width="180" />
      <el-table-column prop="type" label="模板类型" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.type === 'EMAIL' ? 'primary' : 'success'">
            {{ scope.row.type === 'EMAIL' ? '邮件' : '短信' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 'ENABLED' ? 'success' : 'danger'">
            {{ scope.row.status === 'ENABLED' ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdBy" label="创建人" width="120" />
      <el-table-column prop="createdTime" label="创建时间" width="180">
        <template #default="scope">
          {{ formatDate(scope.row.createdTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="updatedTime" label="更新时间" width="180">
        <template #default="scope">
          {{ formatDate(scope.row.updatedTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button type="primary" size="small" @click="handleEdit(scope.row)">
            编辑
          </el-button>
          <el-button
            :type="scope.row.status === 'ENABLED' ? 'danger' : 'success'"
            size="small"
            @click="handleToggleStatus(scope.row)"
          >
            {{ scope.row.status === 'ENABLED' ? '禁用' : '启用' }}
          </el-button>
          <el-button type="text" size="small" @click="handleDelete(scope.row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 模板详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="70%"
      :close-on-click-modal="false"
    >
      <el-form
        ref="templateFormRef"
        :model="templateForm"
        :rules="templateRules"
        label-width="100px"
      >
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="templateForm.name" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="模板编码" prop="code">
          <el-input v-model="templateForm.code" placeholder="请输入模板编码" />
        </el-form-item>
        <el-form-item label="模板类型" prop="type">
          <el-select v-model="templateForm.type" placeholder="请选择模板类型">
            <el-option label="邮件" value="EMAIL" />
            <el-option label="短信" value="SMS" />
          </el-select>
        </el-form-item>
        <el-form-item label="邮件主题" v-if="templateForm.type === 'EMAIL'" prop="subject">
          <el-input v-model="templateForm.subject" placeholder="请输入邮件主题" />
        </el-form-item>
        <el-form-item label="模板内容" prop="content">
          <!-- 邮件模板使用富文本编辑器 -->
          <rich-text-editor
            v-if="templateForm.type === 'EMAIL'"
            v-model="templateForm.content"
            :placeholder="'请输入邮件内容，支持${变量名}格式的占位符'"
          />
          
          <!-- 短信模板使用短信编辑器 -->
          <sms-editor
            v-else
            v-model="templateForm.content"
            :placeholder="'请输入短信内容，支持${变量名}格式的占位符'"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="templateForm.description"
            type="textarea"
            rows="3"
            placeholder="请输入模板描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSave">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import NotificationTemplateService from '../services/NotificationTemplateService';
import RichTextEditor from '../components/RichTextEditor.vue';
import SmsEditor from '../components/SmsEditor.vue';
import { Plus } from '@element-plus/icons-vue';

export default {
  name: 'NotificationTemplateManagement',
  components: {
    RichTextEditor,
    SmsEditor,
    Plus
  },
  data() {
    return {
      loading: false,
      templates: [],
      searchForm: {
        name: '',
        type: '',
        status: ''
      },
      pagination: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
      dialogVisible: false,
      dialogTitle: '',
      editingTemplateId: null,
      templateForm: {
        name: '',
        code: '',
        type: 'EMAIL',
        subject: '',
        content: '',
        description: ''
      },
      templateRules: {
        name: [
          { required: true, message: '请输入模板名称', trigger: 'blur' },
          { max: 100, message: '模板名称不能超过100个字符', trigger: 'blur' }
        ],
        code: [
          { required: true, message: '请输入模板编码', trigger: 'blur' },
          { pattern: /^[a-zA-Z0-9_]+$/, message: '模板编码只能包含字母、数字和下划线', trigger: 'blur' },
          { max: 50, message: '模板编码不能超过50个字符', trigger: 'blur' }
        ],
        type: [
          { required: true, message: '请选择模板类型', trigger: 'change' }
        ],
        subject: [
          { required: true, message: '请输入邮件主题', trigger: 'blur' },
          { max: 200, message: '邮件主题不能超过200个字符', trigger: 'blur' }
        ],
        content: [
          { required: true, message: '请输入模板内容', trigger: 'blur' }
        ],
        description: [
          { max: 500, message: '描述不能超过500个字符', trigger: 'blur' }
        ]
      }
    };
  },
  computed: {
    templatesData() {
      return this.templates;
    }
  },
  mounted() {
    this.loadTemplates();
  },
  methods: {
    // 加载模板列表
    async loadTemplates() {
      this.loading = true;
      try {
        const params = {
          ...this.searchForm,
          pageNum: this.pagination.currentPage,
          pageSize: this.pagination.pageSize
        };
        const response = await NotificationTemplateService.getTemplates(params);
        
        // 兼容不同的响应格式 - 避免访问undefined的list属性
        let templates = [];
        let total = 0;
        
        if (Array.isArray(response)) {
          // 直接返回数组
          templates = response;
          total = response.length;
        } else if (typeof response === 'object' && response) {
          // 响应是对象，检查是否有list、data或直接是数组
          if (Array.isArray(response.list)) {
            // 标准分页格式: { list: [...], total: ... }
            templates = response.list;
            total = response.total || 0;
          } else if (Array.isArray(response.data)) {
            // 直接返回数据数组
            templates = response.data;
            total = response.total || response.data.length;
          } else if (response.data && typeof response.data === 'object') {
            // 嵌套格式: { data: { list: [...], total: ... } }
            if (Array.isArray(response.data.list)) {
              templates = response.data.list;
              total = response.data.total || 0;
            } else if (Array.isArray(response.data)) {
              // 另一种嵌套格式: { data: [...] }
              templates = response.data;
              total = response.data.length;
            }
          } else if (Array.isArray(response.templates)) {
            // 可能的格式: { templates: [...], total: ... }
            templates = response.templates;
            total = response.total || 0;
          }
        }
        
        this.templates = templates;
        this.pagination.total = total;
      } catch (error) {
        this.$message.error('加载模板列表失败');
        console.error('加载模板列表失败:', error);
      } finally {
        this.loading = false;
      }
    },

    // 查询
    handleSearch() {
      this.pagination.currentPage = 1;
      this.loadTemplates();
    },

    // 重置
    handleReset() {
      this.searchForm = {
        name: '',
        type: '',
        status: ''
      };
      this.pagination.currentPage = 1;
      this.loadTemplates();
    },

    // 分页大小变化
    handleSizeChange(size) {
      this.pagination.pageSize = size;
      this.loadTemplates();
    },

    // 当前页码变化
    handleCurrentChange(current) {
      this.pagination.currentPage = current;
      this.loadTemplates();
    },

    // 格式化日期
    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return d.toLocaleString('zh-CN');
    },

    // 行点击
    handleRowClick(row) {
      // 可以在这里处理行点击事件，例如查看详情
    },

    // 添加模板
    handleAddTemplate() {
      this.dialogTitle = '新建模板';
      this.editingTemplateId = null;
      this.templateForm = {
        name: '',
        code: '',
        type: 'EMAIL',
        subject: '',
        content: '',
        description: ''
      };
      this.dialogVisible = true;
    },

    // 编辑模板
    async handleEdit(row) {
      try {
        const templateResponse = await NotificationTemplateService.getTemplate(row.id);
        
        // 兼容不同的响应格式
        let template = null;
        if (templateResponse && typeof templateResponse === 'object') {
          if (templateResponse.id) {
            // 直接是模板对象
            template = templateResponse;
          } else if (templateResponse.data && templateResponse.data.id) {
            // 嵌套格式: { data: {...} }
            template = templateResponse.data;
          }
        }
        
        if (!template) {
          this.$message.error('模板不存在');
          return;
        }
        this.dialogTitle = '编辑模板';
        this.editingTemplateId = row.id;
        this.templateForm = {
          name: template.name,
          code: template.code,
          type: template.type,
          subject: template.subject || '',
          content: template.content,
          description: template.description || ''
        };
        this.dialogVisible = true;
      } catch (error) {
        this.$message.error('获取模板详情失败');
        console.error('获取模板详情失败:', error);
      }
    },

    // 切换状态
    async handleToggleStatus(row) {
      const action = row.status === 'ENABLED' ? '禁用' : '启用';
      const confirmMessage = `确定要${action}模板「${row.name}」吗？`;
      
      this.$confirm(confirmMessage, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          if (row.status === 'ENABLED') {
            await NotificationTemplateService.disableTemplate(row.id);
          } else {
            await NotificationTemplateService.enableTemplate(row.id);
          }
          this.$message.success(`${action}成功`);
          this.loadTemplates();
        } catch (error) {
          this.$message.error(`${action}失败`);
          console.error(`${action}模板失败:`, error);
        }
      }).catch(() => {
        // 取消操作
      });
    },

    // 删除模板
    handleDelete(row) {
      this.$confirm(`确定要删除模板「${row.name}」吗？删除后不可恢复。`, '警告', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'danger'
      }).then(async () => {
        try {
          await NotificationTemplateService.deleteTemplate(row.id);
          this.$message.success('删除成功');
          this.loadTemplates();
        } catch (error) {
          this.$message.error('删除失败');
          console.error('删除模板失败:', error);
        }
      }).catch(() => {
        // 取消操作
      });
    },

    // 保存模板
    async handleSave() {
      const formRef = this.$refs.templateFormRef;
      if (!formRef) {
        this.$message.error('表单引用未找到');
        return;
      }
      
      await formRef.validate(async (valid) => {
        if (valid) {
          try {
            if (this.editingTemplateId) {
              // 更新模板
              await NotificationTemplateService.updateTemplate(
                this.editingTemplateId,
                this.templateForm
              );
              this.$message.success('更新成功');
            } else {
              // 新增模板
              await NotificationTemplateService.createTemplate(this.templateForm);
              this.$message.success('创建成功');
            }
            this.dialogVisible = false;
            this.loadTemplates();
          } catch (error) {
            this.$message.error(this.editingTemplateId ? '更新失败' : '创建失败');
            console.error('保存模板失败:', error);
          }
        }
      });
    }
  }
};
</script>

<style scoped>
.notification-template-management {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.search-bar {
  background: white;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.el-table {
  background: white;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>