import { createApp } from 'vue';
import { createPinia } from 'pinia';
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import zhCn from 'element-plus/dist/locale/zh-cn.mjs';
import * as ElementPlusIconsVue from '@element-plus/icons-vue';
import ECharts from 'vue-echarts';
// vue-echarts 仅提供 Vue 包装组件；必须显式注册渲染器与用到的图表类型。
// 注意：这里必须用 'echarts/core' 等子路径（与 vue-echarts 内部引用的是同一模块），
// 直接用 'echarts' 全量包会被 Vite 拆成独立 chunk，导致注册表割裂、图表空白。
import { use } from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import { LineChart, BarChart, PieChart, GraphChart, RadarChart } from 'echarts/charts';
import { GridComponent, TooltipComponent, LegendComponent, TitleComponent, RadarComponent } from 'echarts/components';

use([
  CanvasRenderer,
  LineChart,
  BarChart,
  PieChart,
  GraphChart,
  RadarChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent,
  RadarComponent,
]);

import App from './App.vue';
import router from './router';
import './styles/index.scss';

const app = createApp(App);

// Pinia + 持久化
const pinia = createPinia();
pinia.use(piniaPluginPersistedstate);
app.use(pinia);

// Element Plus
app.use(ElementPlus, { locale: zhCn });

// 注册 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component);
}

// 注册 ECharts 组件
app.component('VChart', ECharts);

// 创融通全局业务组件（Cft* 系列）
import * as CftComponents from '@/components/cft';
for (const [name, component] of Object.entries(CftComponents)) {
  app.component(name, component);
}

// Router
app.use(router);

app.mount('#app');

console.log('[创融通 FirstFuse] 前端 Demo 已启动');
console.log('[Mock模式]', import.meta.env.VITE_USE_MOCK === 'true' ? '开启' : '关闭');
