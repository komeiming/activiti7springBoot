import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    // 配置路径别名
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    // 配置代理，解决跨域问题
    proxy: {
      // 代理所有/api开头的请求到后端服务器
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
        // 不要移除/api前缀，因为后端接口实际路径已经包含/api
      },
      // 代理所有/v1开头的请求到后端服务器，这些请求应该映射到/api/v1
      '/v1': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // 将/v1替换为/api/v1，因为后端接口实际路径是/api/v1
        rewrite: (path) => path.replace(/^\/v1/, '/api/v1')
      },
      // 代理所有/auth开头的请求到后端服务器，这些请求应该映射到/api/auth
      '/auth': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // 将/auth替换为/api/auth，因为后端接口实际路径是/api/auth
        rewrite: (path) => path.replace(/^\/auth/, '/api/auth')
      }
    }
  }
})
