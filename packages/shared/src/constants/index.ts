/**
 * 全局常量定义
 */

/** 平台信息 */
export const PLATFORM_INFO = {
  name: '创融通',
  subtitle: '慧联创融',
  fullName: '慧联创融 FirstFuse 产融对接平台',
  /** 品牌完整口号 */
  slogan: '创融通：科技创业者的资本搭子',
  /** 品牌口号短句（品牌名已突出的场景使用，避免重复） */
  sloganShort: '科技创业者的资本搭子',
  version: '0.1.0',
};

/** API 基础路径 */
export const API_BASE = '/api/v1';

/** 默认租户 ID（演示/模拟用） */
export const DEFAULT_TENANT_ID = 'demo-tenant-001';

/** 默认分页 */
export const DEFAULT_PAGE_SIZE = 20;

/** 角色路由前缀 */
export const ROUTE_PREFIX = {
  ENTREPRENEUR: '/entrepreneur',
  INSTITUTION: '/institution',
  ADMIN: '/admin',
};

/** 行业分类 */
export const INDUSTRY_OPTIONS = [
  { value: 'ai', label: '人工智能' },
  { value: 'semiconductor', label: '半导体/芯片' },
  { value: 'new_energy', label: '新能源' },
  { value: 'biotech', label: '生物医药' },
  { value: 'robotics', label: '机器人' },
  { value: 'iot', label: '物联网' },
  { value: 'enterprise_software', label: '企业软件/SaaS' },
  { value: 'fintech', label: '金融科技' },
  { value: 'consumer', label: '消费科技' },
  { value: 'hardware', label: '硬科技/硬件' },
  { value: 'materials', label: '新材料' },
  { value: 'other', label: '其他' },
];

/** 地区选项 */
export const REGION_OPTIONS = [
  { value: 'beijing', label: '北京' },
  { value: 'shanghai', label: '上海' },
  { value: 'shenzhen', label: '深圳' },
  { value: 'guangzhou', label: '广州' },
  { value: 'hangzhou', label: '杭州' },
  { value: 'nanjing', label: '南京' },
  { value: 'suzhou', label: '苏州' },
  { value: 'chengdu', label: '成都' },
  { value: 'xian', label: '西安' },
  { value: 'wuhan', label: '武汉' },
  { value: 'other', label: '其他' },
];

/** 投资阶段 */
export const STAGE_OPTIONS = [
  { value: 'angel', label: '天使轮' },
  { value: 'pre_seed', label: 'Pre-Seed' },
  { value: 'seed', label: '种子轮' },
  { value: 'series_a', label: 'A轮' },
  { value: 'series_b', label: 'B轮' },
  { value: 'series_c', label: 'C轮' },
  { value: 'pre_ipo', label: 'Pre-IPO' },
  { value: 'strategic', label: '战略投资' },
];

/** 项目状态 */
export const PROJECT_STATUS_OPTIONS = [
  { value: 'draft', label: '草稿', color: '#909399' },
  { value: 'pending_review', label: '待审核', color: '#E6A23C' },
  { value: 'review_pass', label: '审核通过', color: '#67C23A' },
  { value: 'review_reject', label: '审核驳回', color: '#F56C6C' },
  { value: 'published', label: '已发布', color: '#409EFF' },
  { value: 'archived', label: '已归档', color: '#C0C4CC' },
];

/** 投递状态 */
export const APPLICATION_STATUS_OPTIONS = [
  { value: 'submitted', label: '已投递', color: '#909399' },
  { value: 'viewed', label: '已查看', color: '#409EFF' },
  { value: 'shortlisted', label: '已初选', color: '#67C23A' },
  { value: 'interview', label: '面试中', color: '#E6A23C' },
  { value: 'term_sheet', label: 'TS阶段', color: '#9B59B6' },
  { value: 'rejected', label: '已拒绝', color: '#F56C6C' },
  { value: 'accepted', label: '已接受', color: '#27AE60' },
];

/** 路演状态 */
export const ROADSHOW_STATUS_OPTIONS = [
  { value: 'draft', label: '草稿' },
  { value: 'published', label: '已发布' },
  { value: 'ongoing', label: '进行中' },
  { value: 'completed', label: '已结束' },
  { value: 'cancelled', label: '已取消' },
];
