import { defineConfig, loadEnv } from 'vite';
import vue from '@vitejs/plugin-vue';
import AutoImport from 'unplugin-auto-import/vite';
import Components from 'unplugin-vue-components/vite';
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers';
import path from 'node:path';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');

  return {
    plugins: [
      vue(),
      AutoImport({
        imports: ['vue', 'vue-router', 'pinia'],
        resolvers: [ElementPlusResolver()],
        dts: 'src/auto-imports.d.ts',
      }),
      Components({
        resolvers: [ElementPlusResolver()],
        dts: 'src/components.d.ts',
      }),
    ],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src'),
        '@shared': path.resolve(__dirname, '../../packages/shared/src'),
      },
    },
    // 这些子路径必须作为独立预打包入口，否则 vue-echarts 会内联一份自己的 echarts/core 副本，
    // 与 main.ts 中 use() 注册的模块不是同一个实例，导致 "Renderer is not imported" 图表空白。
    optimizeDeps: {
      include: [
        'vue-echarts',
        'echarts/core',
        'echarts/renderers',
        'echarts/charts',
        'echarts/components',
      ],
    },
    css: {
      preprocessorOptions: {
        scss: {
          additionalData: `@use "@/styles/variables.scss" as *;`,
        },
      },
    },
    server: {
      host: '0.0.0.0',
      port: 5173,
      open: true,
      // 阶段 3 起默认走真实后端：/api 代理到 Spring Boot（默认 8080，可用 VITE_API_BASE_URL 覆盖）
      proxy: {
        '/api': {
          target: env.VITE_API_BASE_URL || 'http://localhost:8080',
          changeOrigin: true,
        },
      },
    },
    build: {
      outDir: 'dist',
      sourcemap: false,
      chunkSizeWarningLimit: 1500,
      rollupOptions: {
        output: {
          manualChunks: {
            vue: ['vue', 'vue-router', 'pinia'],
            elementPlus: ['element-plus', '@element-plus/icons-vue'],
            echarts: ['echarts', 'vue-echarts'],
          },
        },
      },
    },
  };
});
