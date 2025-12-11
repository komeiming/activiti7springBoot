// 调试脚本 - 在浏览器控制台中运行以检查Vue应用状态

// 检查DOM元素
console.log('DOM元素检查:');
console.log('App容器:', document.getElementById('app'));
console.log('App容器内容:', document.getElementById('app').innerHTML);

// 检查Vue实例
console.log('\nVue实例检查:');
if (window.__VUE__ || window.vueApp) {
  console.log('找到Vue实例:', window.__VUE__ || window.vueApp);
} else {
  console.log('未找到全局Vue实例');
}

// 检查路由状态
console.log('\n路由状态检查:');
if (window.vueRouter) {
  console.log('路由实例:', window.vueRouter);
  console.log('当前路由:', window.vueRouter.currentRoute.value);
} else {
  console.log('未找到路由实例');
}

// 检查可能的错误
console.log('\n检查控制台错误:');
const errors = [];
try {
  // 尝试访问Vue应用
  if (typeof Vue !== 'undefined') {
    console.log('Vue全局对象:', Vue);
  }
  // 尝试执行一些基本操作
  document.body.appendChild(document.createElement('div'));
} catch (e) {
  errors.push(e.message);
}
console.log('执行错误:', errors.length > 0 ? errors : '无错误');
