/**
 * 创融通 FirstFuse - 全局 TS 类型定义
 * 所有业务实体接口，与 PRD 保持对齐，三端共用
 */

// ========== 枚举/状态机 ==========

/** 用户角色 */
export enum UserRole {
  ENTREPRENEUR = 'entrepreneur',     // 创业者
  INVESTOR = 'investor',             // 投资人/机构
  PLATFORM_ADMIN = 'platform_admin', // 平台管理员
}

/** 租户类型 */
export enum TenantType {
  INSTITUTION = 'institution',       // 机构租户
  PLATFORM = 'platform',             // 平台租户
}

/** 租户状态 */
export enum TenantStatus {
  ACTIVE = 'active',                 // 开通
  FROZEN = 'frozen',                 // 冻结
  TRIAL = 'trial',                   // 试用
}

/** Element Plus 标签语义色（用于 el-tag 的 type，空态一律用 info） */
export type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger';

/** 项目状态机 */
export enum ProjectStatus {
  DRAFT = 'draft',                   // 草稿
  PENDING_REVIEW = 'pending_review', // 待审核
  REVIEW_PASS = 'review_pass',       // 审核通过
  REVIEW_REJECT = 'review_reject',   // 审核驳回
  PUBLISHED = 'published',           // 已发布
  ARCHIVED = 'archived',             // 已归档
}

/** 投资阶段 */
export enum InvestmentStage {
  ANGEL = 'angel',
  PRE_SEED = 'pre_seed',
  SEED = 'seed',
  SERIES_A = 'series_a',
  SERIES_B = 'series_b',
  SERIES_C = 'series_c',
  PRE_IPO = 'pre_ipo',
  STRATEGIC = 'strategic',
}

/** 投递状态机 */
export enum ApplicationStatus {
  SUBMITTED = 'submitted',           // 已投递
  VIEWED = 'viewed',                 // 已查看
  SHORTLISTED = 'shortlisted',       // 已初选
  INTERVIEW = 'interview',           // 面试中
  TERM_SHEET = 'term_sheet',         // TS阶段
  REJECTED = 'rejected',             // 已拒绝
  ACCEPTED = 'accepted',             // 已接受
}

/** 路演状态 */
export enum RoadshowStatus {
  DRAFT = 'draft',
  PUBLISHED = 'published',
  ONGOING = 'ongoing',
  COMPLETED = 'completed',
  CANCELLED = 'cancelled',
}

/** 订单状态 */
export enum OrderStatus {
  PENDING = 'pending',
  PAID = 'paid',
  CANCELLED = 'cancelled',
  REFUNDED = 'refunded',
}

/** 评审结论 */
export enum ReviewDecision {
  PASS = 'pass',
  REJECT = 'reject',
  PENDING = 'pending',
}

// ========== 基础实体 ==========

export interface BaseEntity {
  id: string;
  createdAt: string;
  updatedAt: string;
  tenantId: string;
}

export interface User {
  id: string;
  tenantId: string;
  role: UserRole;
  nickname: string;
  avatar?: string;
  phone?: string;
  email?: string;
  wechatOpenid?: string;
  company?: string;
  position?: string;
  realNameVerified: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Tenant {
  id: string;
  name: string;
  type: TenantType;
  status: TenantStatus;
  contactName: string;
  contactPhone: string;
  contactEmail?: string;
  industry?: string;
  subscriptionPlan?: string;
  expireAt?: string;
  maxUsers?: number;
  createdAt: string;
  updatedAt: string;
}

// ========== 项目相关 ==========

export interface Project {
  id: string;
  tenantId: string;
  ownerId: string;
  title: string;
  subtitle?: string;
  industry: string;
  subIndustry?: string;
  region: string;
  stage: InvestmentStage;
  amountSought: number;         // 融资金额（万元）
  valuation?: number;           // 估值（万元）
  equityOffered?: number;       // 出让股权（%）
  shortDescription?: string;
  longDescription?: string;
  businessModel?: string;
  competitiveAdvantage?: string;
  marketSize?: string;
  revenue?: number;             // 营业收入（万元）
  profit?: number;              // 净利润（万元）
  teamSize?: number;
  foundedAt?: string;
  website?: string;
  bpFileId?: string;
  status: ProjectStatus;
  viewCount: number;
  favoriteCount: number;
  tags?: string[];
  createdAt: string;
  updatedAt: string;
}

export interface ProjectApplication {
  id: string;
  tenantId: string;
  projectId: string;
  investorTenantId: string;
  status: ApplicationStatus;
  coverLetter?: string;
  feedback?: string;
  submittedAt: string;
  updatedAt: string;
}

export interface ProjectDiagnosis {
  id: string;
  tenantId: string;
  projectId: string;
  overallScore: number;         // 综合评分 0-100
  dimensions: DiagnosisDimension[];
  aiSummary?: string;
  createdAt: string;
  updatedAt: string;
}

export interface DiagnosisDimension {
  name: string;                 // 维度名称
  score: number;                // 得分
  fullMark: number;             // 满分
  description?: string;
}

// ========== 机构/投资人相关 ==========

export interface InvestorInstitution {
  id: string;
  tenantId: string;
  name: string;
  logo?: string;
  website?: string;
  region?: string;
  focusIndustries?: string[];
  preferredStages?: InvestmentStage[];
  minInvestment?: number;       // 最低投资金额（万元）
  maxInvestment?: number;       // 最高投资金额（万元）
  totalAum?: number;            // 管理规模（万元）
  foundedAt?: string;
  description?: string;
  teamMembers?: InvestorMember[];
  createdAt: string;
  updatedAt: string;
}

export interface InvestorMember {
  id: string;
  name: string;
  position?: string;
  avatar?: string;
  bio?: string;
}

export interface PortfolioProject {
  id: string;
  tenantId: string;
  institutionId: string;
  projectId: string;
  investmentAmount: number;
  investmentDate: string;
  postMoneyValuation?: number;
  equityPercent?: number;
  currentStage?: string;
  status?: string;              // 正常/退出中/已退出
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

// ========== 评审相关 ==========

export interface ReviewRecord {
  id: string;
  tenantId: string;
  targetType: 'project' | 'roadshow';
  targetId: string;
  reviewerId?: string;
  decision: ReviewDecision;
  score?: number;
  comment?: string;
  aiSummary?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ReviewBrief {
  id: string;
  tenantId: string;
  projectId: string;
  generatedBy: 'ai' | 'human';
  sections: ReviewSection[];
  overallAssessment?: string;
  riskPoints?: string[];
  highlights?: string[];
  createdAt: string;
  updatedAt: string;
}

export interface ReviewSection {
  title: string;
  content: string;
  score?: number;
}

// ========== 路演相关 ==========

export interface Roadshow {
  id: string;
  tenantId: string;
  organizerId: string;
  title: string;
  description?: string;
  type: 'online' | 'offline';
  isClosedDoor: boolean;        // 闭门路演
  startTime: string;
  endTime: string;
  location?: string;
  maxParticipants?: number;
  currentParticipants?: number;
  coverImage?: string;
  status: RoadshowStatus;
  agenda?: RoadshowAgendaItem[];
  projects?: string[];          // 项目ID列表
  createdAt: string;
  updatedAt: string;
}

export interface RoadshowAgendaItem {
  time: string;
  speaker?: string;
  title: string;
}

export interface RoadshowApplication {
  id: string;
  tenantId: string;
  roadshowId: string;
  projectId: string;
  status: 'pending' | 'approved' | 'rejected';
  submittedAt: string;
  updatedAt: string;
}

// ========== 文件/上传 ==========

export interface FileInfo {
  fileId: string;
  fileName: string;
  fileSize: number;
  contentType: string;
  uploadUrl?: string;
  createdAt: string;
}

// ========== 订单/账单 ==========

export interface Order {
  id: string;
  tenantId: string;
  userId: string;
  type: 'subscription' | 'service' | 'extension';
  title: string;
  amount: number;
  currency: string;
  status: OrderStatus;
  paidAt?: string;
  createdAt: string;
  updatedAt: string;
}

// ========== AI 相关 ==========

export interface AIChatMessage {
  id: string;
  role: 'user' | 'assistant' | 'system';
  content: string;
  timestamp: string;
}

export interface AIChatSession {
  id: string;
  tenantId: string;
  userId: string;
  title: string;
  messages: AIChatMessage[];
  createdAt: string;
  updatedAt: string;
}

export interface AIConfig {
  id: string;
  promptTemplate: string;
  model: string;
  routeKey: string;
  temperature?: number;
  maxTokens?: number;
  enabled: boolean;
}

// ========== 知识库 ==========

export interface KnowledgeBase {
  id: string;
  tenantId: string;
  title: string;
  category?: string;
  content: string;
  tags?: string[];
  viewCount: number;
  createdAt: string;
  updatedAt: string;
}

// ========== 风控 ==========

export interface BlacklistEntry {
  id: string;
  type: 'user' | 'tenant' | 'project' | 'phone' | 'ip';
  targetValue: string;
  reason: string;
  operatorId?: string;
  createdAt: string;
  expiresAt?: string;
}

// ========== 图谱 ==========

export interface GraphNode {
  id: string;
  type: string;                 // 节点类型：company/person/investment
  name: string;
  properties?: Record<string, unknown>;
  x?: number;
  y?: number;
}

export interface GraphEdge {
  id: string;
  source: string;
  target: string;
  type: string;                 // 关系类型：invested_in/works_at/competitor_of
  properties?: Record<string, unknown>;
}

export interface GraphData {
  nodes: GraphNode[];
  edges: GraphEdge[];
}

// ========== API 通用类型 ==========

export interface ApiResponse<T = unknown> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

export interface PageQuery {
  pageNum: number;
  pageSize: number;
  keyword?: string;
  sortField?: string;
  sortOrder?: 'asc' | 'desc';
}

export interface PageResult<T> {
  list: T[];
  total: number;
  pageNum: number;
  pageSize: number;
  totalPages: number;
}
