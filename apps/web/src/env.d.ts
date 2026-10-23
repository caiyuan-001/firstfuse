// Vite 环境变量类型声明
interface ImportMetaEnv {
  readonly VITE_USE_MOCK: string;
  readonly VITE_API_BASE_URL: string;
  readonly VITE_APP_TITLE: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}

// Mock 文件的 Vite 客户端类型
declare module 'vite/client' {}

// Element Plus 中文语言包（该 .mjs 子路径未随包提供类型声明）
declare module 'element-plus/dist/locale/zh-cn.mjs' {
  import type { ConfigProviderProps } from 'element-plus';
  const locale: NonNullable<ConfigProviderProps['locale']>;
  export default locale;
}
