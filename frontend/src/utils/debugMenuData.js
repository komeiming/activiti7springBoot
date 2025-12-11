// 调试菜单数据获取和处理逻辑
import PermissionService from '../services/PermissionService';

// 测试获取用户菜单数据
async function debugMenuData() {
  try {
    console.log('=== 开始调试菜单数据获取 ===');
    
    // 获取当前用户信息
    const userInfo = localStorage.getItem('user');
    console.log('当前用户信息:', userInfo ? JSON.parse(userInfo) : '未登录');
    
    // 获取菜单数据
    console.log('开始调用 PermissionService.getUserMenus()...');
    const menus = await PermissionService.getUserMenus();
    console.log('菜单数据获取成功，返回数据类型:', typeof menus);
    console.log('菜单数据完整内容:', menus);
    
    // 检查返回数据是否为数组
    if (Array.isArray(menus)) {
      console.log('菜单数据是数组，长度:', menus.length);
      
      // 遍历菜单数据，检查结构
      menus.forEach((menu, index) => {
        console.log(`\n菜单 ${index + 1}:`);
        console.log(`  名称: ${menu.name}`);
        console.log(`  类型: ${menu.menuType}`);
        console.log(`  路径: ${menu.path}`);
        console.log(`  子菜单数量: ${menu.children ? menu.children.length : 0}`);
        
        // 如果有子菜单，也打印出来
        if (menu.children && menu.children.length > 0) {
          menu.children.forEach((subMenu, subIndex) => {
            console.log(`    子菜单 ${subIndex + 1}:`);
            console.log(`      名称: ${subMenu.name}`);
            console.log(`      类型: ${subMenu.menuType}`);
            console.log(`      路径: ${subMenu.path}`);
          });
        }
      });
    } else {
      console.error('菜单数据不是数组，结构异常');
    }
    
    return menus;
  } catch (error) {
    console.error('获取菜单数据失败:', error);
    console.error('错误详情:', error.response || error);
    return null;
  }
}

// 执行调试
debugMenuData();
