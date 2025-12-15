import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import LoginSysUserView from '../views/LoginSysUserView.vue'
import LoginTenantView from '../views/LoginTenantView.vue'
import DashboardView from '../views/DashboardView.vue'
import UserProcessDefinitionManagement from '../views/UserProcessDefinitionManagement.vue'
import GenericProcessApply from '../views/GenericProcessApply.vue'
import EnhancedTaskList from '../views/EnhancedTaskList.vue'
import ProcessHistory from '../views/ProcessHistory.vue'
import TaskDetailView from '../views/TaskDetailView.vue'
import CompletedTasksView from '../views/CompletedTasksView.vue'
import LeaveApplicationView from '../views/LeaveApplicationView.vue'
import ProcessApply from '../views/ProcessApply.vue'
import OnboardingApplicationView from '../views/OnboardingApplicationView.vue'
import ProcessDefinitionManagement from '../views/ProcessDefinitionManagement.vue'
import UserManagement from '../views/UserManagement.vue'
import RoleManagement from '../views/RoleManagement.vue'
import NotificationTemplateManagement from '../views/NotificationTemplateManagement.vue'
import { ElMessage } from 'element-plus'
import NotificationTestView from '../views/NotificationTestView.vue'
import NotificationLogView from '../views/NotificationLogView.vue'
import PendingNotificationView from '../views/PendingNotificationView.vue'
import ApprovalCenter from '../views/ApprovalCenter.vue'
import TenantApprovalView from '../views/TenantApprovalView.vue'

console.log('路由配置加载中...')

// 定义路由
const routes = [
  // 公共层页面
  {
    path: '/register',
    name: 'register',
    component: () => import('../views/public/RegisterView.vue'),
    meta: {
      requiresAuth: false,
      title: '租户注册'
    }
  },
  // 登录页面
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: {
      requiresAuth: false,
      title: '登录选择'
    }
  },
  // 系统用户登录页面
  {
    path: '/login-sys',
    name: 'loginSys',
    component: LoginSysUserView,
    meta: {
      requiresAuth: false,
      title: '系统用户登录'
    }
  },
  // 租户用户登录页面
  {
    path: '/login-tenant',
    name: 'loginTenant',
    component: LoginTenantView,
    meta: {
      requiresAuth: false,
      title: '租户用户登录'
    }
  },
  // 忘记密码页面
  {
    path: '/forgot-password',
    name: 'forgotPassword',
    component: () => import('../views/public/ForgotPasswordView.vue'),
    meta: {
      requiresAuth: false,
      title: '忘记密码'
    }
  },
  // 403页面
  {
    path: '/403',
    name: 'accessDenied',
    component: () => import('../views/public/AccessDeniedView.vue'),
    meta: {
      requiresAuth: false,
      title: '无权限访问'
    }
  },
  // 500页面
  {
    path: '/500',
    name: 'serverError',
    component: () => import('../views/public/ServerErrorView.vue'),
    meta: {
      requiresAuth: false,
      title: '服务器错误'
    }
  },
  // 仪表盘页面
  {
    path: '/dashboard',
    name: 'dashboard',
    component: DashboardView,
    meta: {
      requiresAuth: true,
      title: '仪表盘'
    }
  },
  // 待办任务页面
  {
    path: '/tasks',
    name: 'tasks',
    component: () => import('../views/TaskListView.vue'),
    meta: {
      requiresAuth: true,
      title: '待办任务'
    }
  },
  // 单个任务详情页面
  {
    path: '/tasks/:id',
    name: 'taskDetail',
    component: () => import('../views/TaskDetailView.vue'),
    meta: {
      requiresAuth: true,
      title: '任务详情'
    }
  },
  // 请假申请页面
  {
    path: '/leave-application',
    name: 'leaveApplication',
    component: () => import('../views/LeaveApplicationView.vue'),
    meta: {
      requiresAuth: true,
      title: '请假申请'
    }
  },
  // 流程申请页面
  {
    path: '/process-apply',
    name: 'processApply',
    component: () => import('../views/ProcessApply.vue'),
    meta: {
      requiresAuth: true,
      title: '流程申请'
    }
  },
  // 流程定义管理页面
  {
    path: '/process-definition-management',
    name: 'ProcessDefinitionManagement',
    component: ProcessDefinitionManagement,
    meta: { requiresAuth: true, title: '流程定义管理' }
  },
  // 流程设计器页面
  {
    path: '/process-designer',
    name: 'ProcessDesigner',
    component: () => import('../views/ProcessDesignerView.vue'),
    meta: { requiresAuth: true, title: '流程设计器' }
  },
  // 用户管理页面
  {
    path: '/user-management',
    name: 'UserManagement',
    component: UserManagement,
    meta: { requiresAuth: true, title: '用户管理', adminRequired: true }
  },
  // 角色管理页面
  {
    path: '/role-management',
    name: 'RoleManagement',
    component: RoleManagement,
  },
  // 通知模板管理页面
  {
    path: '/notification-templates',
    name: 'NotificationTemplateManagement',
    component: NotificationTemplateManagement,
    meta: { requiresAuth: true, title: '通知模板管理', adminRequired: true }
  },
  // 通知测试页面
  {
    path: '/notification-templates-test',
    name: 'NotificationTestView',
    component: NotificationTestView,
    meta: {
      title: '通知测试',
      requiresAuth: true
    }
  },
  // 通知日志页面
  {
    path: '/notification-logs',
    name: 'NotificationLogView',
    component: NotificationLogView,
    meta: {
      title: '通知日志',
      requiresAuth: true
    }
  },
  // 待发送通知页面
  {
    path: '/pending-notifications',
    name: 'PendingNotificationView',
    component: PendingNotificationView,
    meta: {
      title: '待发送通知',
      requiresAuth: true
    }
  },
  // 我的流程定义页面
    {      path: '/user-process-definition',
      name: 'userProcessDefinition',
      component: UserProcessDefinitionManagement,
      meta: {
        requiresAuth: true,
        title: '我的流程定义'
      }
    },
    // 通用流程申请页面
    {
      path: '/generic-process-apply',
      name: 'genericProcessApply',
      component: GenericProcessApply,
      meta: {
        requiresAuth: true,
        title: '流程申请'
      }
    },
    {
      path: '/enhanced-task-list',
      name: 'enhancedTaskList',
      component: EnhancedTaskList,
      meta: {
        requiresAuth: true,
        title: '我的待办'
      }
    },
    {
      path: '/process-history',
      name: 'processHistory',
      component: ProcessHistory,
      meta: {
        requiresAuth: true,
        title: '流程历史'
      }
    },
  // 已办任务页面
  {
    path: '/completed-tasks',
    name: 'completedTasks',
    component: () => import('../views/CompletedTasksView.vue'),
    meta: {
      requiresAuth: true,
      title: '已办任务'
    }
  },
  // 流程历史页面
  {
    path: '/process-history',
    name: 'processHistory',
    component: () => import('../views/ProcessHistoryView.vue'),
    meta: {
      requiresAuth: true,
      title: '流程历史'
    }
  },
  // 入职申请页面
  {
    path: '/onboarding-application',
    name: 'onboardingApplication',
    component: () => import('../views/OnboardingApplicationView.vue'),
    meta: {
      requiresAuth: true,
      title: '入职申请'
    }
  },
  // 审批中心页面
  {
    path: '/approval-center',
    name: 'approvalCenter',
    component: ApprovalCenter,
    meta: {
      requiresAuth: true,
      title: '审批中心'
    }
  },
  {
    path: '/tenant-approval',
    name: 'tenantApproval',
    component: TenantApprovalView,
    meta: {
      requiresAuth: true,
      adminRequired: true,
      title: '租户审批管理'
    }
  },
  // 菜单管理页面
  {
    path: '/menu-management',
    name: 'menuManagement',
    component: () => import('../views/MenuManagement.vue'),
    meta: {
      requiresAuth: true,
      title: '菜单管理',
      adminRequired: true
    }
  },
  // 角色菜单权限配置页面
  {
    path: '/role-menu-permission',
    name: 'roleMenuPermission',
    component: () => import('../views/RoleMenuPermission.vue'),
    meta: {
      requiresAuth: true,
      title: '角色菜单权限配置',
      adminRequired: true
    }
  },
  // 多语言配置页面
  {
    path: '/language-management',
    name: 'languageManagement',
    component: () => import('../views/LanguageManagement.vue'),
    meta: {
      requiresAuth: true,
      title: '多语言配置',
      adminRequired: true
    }
  },
  // 监控与日志页面
  {
    path: '/monitor/tenant-stats',
    name: 'tenantStats',
    component: () => import('../views/monitor/TenantStatsView.vue'),
    meta: {
      requiresAuth: true,
      title: '租户使用统计',
      adminRequired: true
    }
  },
  {
    path: '/monitor/api-stats',
    name: 'apiStats',
    component: () => import('../views/monitor/ApiStatsView.vue'),
    meta: {
      requiresAuth: true,
      title: 'API调用统计',
      adminRequired: true
    }
  },
  {
    path: '/monitor/system-logs',
    name: 'systemLogs',
    component: () => import('../views/monitor/SystemOperationLogView.vue'),
    meta: {
      requiresAuth: true,
      title: '系统操作日志',
      adminRequired: true
    }
  },
  // 首页，重定向到仪表盘
  {
    path: '/',
    redirect: '/dashboard'
  },
  // 监控与日志页面重定向，解决404问题
  {
    path: '/tenant-stats',
    redirect: '/monitor/tenant-stats'
  },
  {
    path: '/api-stats',
    redirect: '/monitor/api-stats'
  },
  {
    path: '/system-logs',
    redirect: '/monitor/system-logs'
  },
  // 租户管理后台路由
  {
    path: '/tenant',
    name: 'tenant',
    component: () => import('../views/tenant/Layout.vue'),
    meta: {
      requiresAuth: true,
      title: '租户管理'
    },
    children: [
      // 租户信息管理
      {
        path: 'info',
        name: 'tenantInfo',
        component: () => import('../views/tenant/TenantInfoView.vue'),
        meta: {
          requiresAuth: true,
          title: '基本信息'
        }
      },
      {
        path: 'auth',
        name: 'tenantAuth',
        component: () => import('../views/tenant/TenantAuthView.vue'),
        meta: {
          requiresAuth: true,
          title: '授权信息'
        }
      },
      {
        path: 'service',
        name: 'tenantService',
        component: () => import('../views/tenant/TenantServiceView.vue'),
        meta: {
          requiresAuth: true,
          title: '服务管理'
        }
      },
      // 通知模块管理
      {
        path: 'notification/templates',
        name: 'notificationTemplates',
        component: () => import('../views/tenant/notification/TemplateManagementView.vue'),
        meta: {
          requiresAuth: true,
          title: '通知模板管理'
        }
      },
      {
        path: 'notification/send',
        name: 'notificationSend',
        component: () => import('../views/tenant/notification/NotificationSendView.vue'),
        meta: {
          requiresAuth: true,
          title: '通知发送'
        }
      },
      {
        path: 'notification/logs',
        name: 'notificationLogs',
        component: () => import('../views/tenant/notification/SendLogsView.vue'),
        meta: {
          requiresAuth: true,
          title: '发送日志'
        }
      },
      {
        path: 'notification/config',
        name: 'notificationConfig',
        component: () => import('../views/tenant/notification/NotificationConfigView.vue'),
        meta: {
          requiresAuth: true,
          title: '通知配置'
        }
      },
      // 工作流模块管理
      {
        path: 'workflow/templates',
        name: 'workflowTemplates',
        component: () => import('../views/tenant/workflow/WorkflowTemplateManagementView.vue'),
        meta: {
          requiresAuth: true,
          title: '流程模板管理'
        }
      },
      {
        path: 'workflow/designer',
        name: 'workflowDesigner',
        component: () => import('../views/ProcessDesignerView.vue'),
        meta: {
          requiresAuth: true,
          title: '流程设计'
        }
      },
      {
        path: 'workflow/instances',
        name: 'workflowInstances',
        component: () => import('../views/tenant/workflow/WorkflowInstanceManagementView.vue'),
        meta: {
          requiresAuth: true,
          title: '流程实例管理'
        }
      },
      {
        path: 'workflow/logs',
        name: 'workflowLogs',
        component: () => import('../views/tenant/workflow/ExecutionLogsView.vue'),
        meta: {
          requiresAuth: true,
          title: '执行日志'
        }
      },
      {
        path: 'workflow/config',
        name: 'workflowConfig',
        component: () => import('../views/tenant/workflow/WorkflowConfigView.vue'),
        meta: {
          requiresAuth: true,
          title: '工作流配置'
        }
      },
      // 权限管理
      {
        path: 'permission/roles',
        name: 'permissionRoles',
        component: () => import('../views/tenant/permission/RoleConfigView.vue'),
        meta: {
          requiresAuth: true,
          title: '角色配置'
        }
      },
      {
        path: 'permission/custom',
        name: 'permissionCustom',
        component: () => import('../views/tenant/permission/CustomPermissionView.vue'),
        meta: {
          requiresAuth: true,
          title: '自定义权限申请'
        }
      },
      // 监控与日志
      {
        path: 'monitor/stats',
        name: 'monitorStats',
        component: () => import('../views/tenant/monitor/CallStatsView.vue'),
        meta: {
          requiresAuth: true,
          title: '调用统计'
        }
      },
      {
        path: 'monitor/logs',
        name: 'monitorLogs',
        component: () => import('../views/tenant/monitor/ApiLogsView.vue'),
        meta: {
          requiresAuth: true,
          title: '日志查询'
        }
      },
      {
        path: 'monitor/alerts',
        name: 'monitorAlerts',
        component: () => import('../views/tenant/monitor/AlertConfigView.vue'),
        meta: {
          requiresAuth: true,
          title: '告警配置'
        }
      }
    ]
  },
  // 404页面
  {
    path: '/:pathMatch(.*)*',
    component: () => import('../views/public/NotFoundView.vue'),
    meta: {
      requiresAuth: false,
      title: '页面不存在'
    }
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 检查用户是否已登录的辅助函数
function isAuthenticated() {
  try {
    const userStr = localStorage.getItem('user')
    if (!userStr) return false
    
    const user = JSON.parse(userStr)
    // 验证用户对象的完整性
    if (!user || !user.token || !user.username) {
      console.warn('用户信息不完整')
      localStorage.removeItem('user')
      return false
    }
    
    // 验证token是否有效（实际项目中可能需要更复杂的验证）
    // 这里简单检查token是否存在且长度合理
    if (user.token.length < 10) {
      console.warn('无效的token')
      localStorage.removeItem('user')
      return false
    }
    
    return true
  } catch (e) {
    console.error('用户信息验证失败:', e)
    localStorage.removeItem('user')
    return false
  }
}

// 路由守卫，检查登录状态和权限
router.beforeEach((to, from, next) => {
  // 页面标题由Layout组件动态设置，这里不再需要静态设置
  console.log(`当前路径: ${to.path}`)
  
  // 检查是否已登录
  const authenticated = isAuthenticated()
  
  // 如果用户已登录，访问登录页时重定向到仪表盘
  if (authenticated && to.path === '/login') {
    console.log('已登录用户尝试访问登录页，重定向到仪表盘')
    ElMessage.warning('您已登录，无需重复登录')
    next('/dashboard')
    return
  }
  
  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    if (authenticated) {
      // 检查是否需要管理员权限
      if (to.meta.adminRequired) {
        try {
          const userStr = localStorage.getItem('user')
          const user = JSON.parse(userStr)
          // 检查用户是否有管理员角色
          const isAdmin = user.roles && (user.roles.includes('ADMIN') || user.roles.includes('admin') || 
                                        user.roles.some(role => role.roleCode === 'ADMIN' || role.roleCode === 'admin'))
          
          if (!isAdmin) {
            ElMessage.error('没有权限访问该页面')
            next('/dashboard')
            return
          }
        } catch (e) {
          console.error('检查管理员权限失败:', e)
          ElMessage.error('权限检查失败')
          next('/dashboard')
          return
        }
      }
      // 已认证且有权限，允许访问
      next()
    } else {
      // 未认证，重定向到登录页面，并记录当前路径以便登录后返回
      console.log('未登录用户尝试访问受保护页面，重定向到登录页')
      // 只有当不是从登录页来的时候才显示提示信息
      if (from.path !== '/login') {
        ElMessage.warning('请先登录后再访问')
      }
      // 保存当前路径，登录成功后可以跳转到之前想访问的页面
      if (to.path !== '/' && to.path !== '/dashboard') {
        sessionStorage.setItem('redirectPath', to.fullPath)
      }
      next('/login')
    }
  } else {
    // 不需要认证的页面直接通过
    next()
  }
})

// 全局导航后置守卫，可以在这里处理一些登录后的操作
router.afterEach((to) => {
  if (to.path !== '/login') {
    // 清除重定向路径
    sessionStorage.removeItem('redirectPath')
  }
})

console.log('路由配置加载完成')
export default router