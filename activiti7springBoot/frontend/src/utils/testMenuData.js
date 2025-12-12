// 测试菜单数据获取和处理逻辑
import PermissionService from '../services/PermissionService';

// 测试获取用户菜单数据
async function testMenuData() {
  try {
    console.log('开始获取菜单数据...');
    const menus = await PermissionService.getUserMenus();
    console.log('菜单数据获取成功:', menus);
    
    // 查找通知模板管理菜单项
    const findMenuByName = (menuList, name) => {
      for (const menu of menuList) {
        if (menu.name === name) {
          return menu;
        }
        if (menu.children && menu.children.length > 0) {
          const found = findMenuByName(menu.children, name);
          if (found) {
            return found;
          }
        }
      }
      return null;
    };
    
    const notificationTemplateMenu = findMenuByName(menus, '通知模板管理');
    console.log('通知模板管理菜单项:', notificationTemplateMenu);
    
    const menuManagementMenu = findMenuByName(menus, '菜单管理');
    console.log('菜单管理菜单项:', menuManagementMenu);
    
    return { menus, notificationTemplateMenu, menuManagementMenu };
  } catch (error) {
    console.error('获取菜单数据失败:', error);
    return null;
  }
}

testMenuData();
