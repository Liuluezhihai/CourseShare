import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, '.', '')
  return {
    base: env.VITE_BASE_PATH || '/',
    plugins: [
      vue(),
      Components({
        dts: false,
        resolvers: [ElementPlusResolver()]
      })
    ],
    server: {
      port: 5173,
      proxy: {
        '/api': {
          target: 'http://localhost:8080/course-share',
          changeOrigin: false
        }
      }
    },
    build: {
      outDir: 'dist',
      sourcemap: false
    }
  }
})

