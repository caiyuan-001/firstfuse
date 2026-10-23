# 创融通深化开发完整计划（Spec）

> Status: 待用户审批
> Source: 用户请求「创融通继续深化开发需要做哪些事情，请根据规划为我列出完整的计划」
> Mode: 多阶段宏观规划（含后端高风险信号：鉴权 / 数据模型 / 接口迁移）
> Last updated: 2026-10-22
> 项目根: `C:\Users\26232\Documents\Trae-CN\temp_20261019_01`

## 1. Requirements summary

前端 Demo（25 页 + 预览中心）已主体完成并通过构建。本规格回答「接下来要做什么」，覆盖四件事：
1. 前端 Demo 收尾清单（被打断的登录页入口任务 + 管理端打磨）；
2. 人工验收的执行标准（后端开发的唯一前置硬门槛）；
3. 验收通过后后端开发的分阶段方案（数据模型、接口迁移映射、鉴权多租户）；
4. 前端从 Mock 切换到真实后端、部署运维的路径。

**不可逾越的硬约束（来自项目记忆，全程生效）**：
- 未收到用户明确验收确认与下一步指令前，严禁编写任何后端代码（Java / PostgreSQL / NebulaGraph / MinIO / Docker）。
- 阶段 0/1 内所有接口仍只用 vite-plugin-mock，Mock 结构严格对齐 PRD `/api/v1` 入参出参。

## 2. 现状基线（已核实事实）

- 页面：37 个 `.vue`，覆盖 PRD 25 页（创业者 P1-P8 含 H5、机构 I1-I8、管理 A1-A8）+ 预览中心 + 登录/H5 登录。
- Mock：9 个模块（user / tenant / roadshow / project / order / knowledge / institution / ai / blacklist），约 30 个 `/api/v1/*` 端点，统一 `{code,message,data,timestamp}` 包装（`apps/web/mock/_utils.ts`）。
- 共享类型：`packages/shared/src/types/index.ts` 含 11 个枚举（UserRole / TenantType / TenantStatus / ProjectStatus / InvestmentStage / ApplicationStatus / RoadshowStatus / OrderStatus / ReviewDecision / TagType 等）+ 20+ 实体（User / Tenant / Project / ProjectApplication / ProjectDiagnosis / InvestorInstitution / Roadshow / Order / AIConfig / KnowledgeBase / BlacklistEntry / GraphNode/Edge/Data / ApiResponse / PageResult 等）。
- 基础设施：`src/utils/request.ts` 自动注入 `Authorization: Bearer` + `X-Tenant-ID`，401 自动登出；`src/store/user.ts` sessionStorage 持久化；路由守卫按 `meta.roles` 三端隔离。
- 质量：`vue-tsc` 0 错误；`vite build` 成功（26.25s，2472 模块，按路由分包）。
- 环境限制：本机 git 不可用（PATH 无 git），worktree/分支策略不适用，改动保护以「先完成当前 todo 再开新任务」代替。

## 3. 阶段划分总览

| 阶段 | 名称 | 前置条件 | 产出 |
|---|---|---|---|
| 0 | 前端收尾 | 无（当前即可做） | 登录页入口 + 注册/找回页 + A1-A8 打磨 |
| 1 | 人工验收 | 阶段 0 完成 | 用户书面确认「前端 Demo 验收通过」 |
| 2 | 后端开发 | **阶段 1 用户明确指令** | Spring Boot 后端 + 数据库 + 30 端点 |
| 3 | 前后端联调切换 | 阶段 2 单模块就绪即可增量开始 | Mock 摘除、真实接口全量替换 |
| 4 | 部署运维 | 阶段 3 完成 | Docker Compose 一键部署 + CI/CD |

## 4. 阶段 0：前端收尾（Spec 级明细）

### 4.1 登录页「忘记密码 / 注册账号」（恢复被打断的 t1-t3）
- 改 `apps/web/src/views/login/LoginView.vue`：密码行右侧加「忘记密码？」链接 → `/forgot-password`；登录按钮下方加「还没有账号？注册账号」→ `/register`。
- 新增 `apps/web/src/views/login/ForgotPasswordView.vue`：手机号/邮箱 + 验证码 + 新密码表单，提交走 mock `POST /api/v1/auth/reset-password`，成功 toast 后跳回登录。
- 新增 `apps/web/src/views/login/RegisterView.vue`：角色选择（创业者/投资机构）、账号、密码、机构名（投资角色时显示）、协议勾选，提交走 mock `POST /api/v1/auth/register`，成功跳登录。
- 改 `apps/web/src/router/index.ts`：publicRoutes 增加两条（`meta: { title, layout: 'blank', hidden: true }`），不加 requiresAuth。
- 改 `apps/web/mock/modules/user.ts`：新增 `reset-password`、`register` 两个 mock 端点，返回 `ok(null)`。
- 验证：`vue-tsc --noEmit` 0 错误；浏览器走通 登录→注册→登录、登录→忘记密码→登录 两条路径。

### 4.2 管理后台 A1-A8 深度打磨（历史遗留，需用户下指令后启动）
- 现状：7 个管理页表格数据均为组件内硬编码数组（如 `admin/KnowledgeView.vue:77-83`、`admin/OrderView.vue:75-81`），未接 mock。
- 目标：参照机构端 I1-I8 的完成标准——筛选/分页/表单弹窗/状态流转全部接通对应 mock 模块（tenant / project / roadshow / order / knowledge / blacklist / ai）。
- 逐页清单：OverviewView（统计接通）、TenantManageView（冻结/解冻流转）、ProjectReviewView（审核通过/驳回调 `PUT /projects/:id` + ReviewDecision）、RoadshowOpView、OrderView、KnowledgeView（发布开关调 mock）、AIConfigView（Prompt 模板与模型路由的增删改）。
- 验证：每页筛选→翻页→新增→编辑→状态变更全链路浏览器走查。

### 4.3 可选项（需用户逐项确认，默认不做）
- 全局公共组件 9 个（CftCard / CftPagination 等）+ `/preview/components` 预览页。
- 创业者端 PC 页面窄屏适配。
- H5 登录页同步增加注册/忘记密码入口。

## 5. 阶段 1：人工验收（硬门槛）

验收检查清单（全部通过才算验收）：
1. `pnpm dev` 一次启动成功，PC + H5 双端可访问；
2. 25 个 PRD 页面逐页走查：表单、筛选、分页、弹窗、状态流转无死链无报错；
3. 三角色登录跳转正确（entrepreneur→/entrepreneur/workspace、investor→/institution/dashboard、platform_admin→/admin/overview），越权访问被守卫拦截；
4. Mock 响应结构抽查与 PRD `/api/v1` 契约一致；
5. `vue-tsc` 0 错误、`vite build` 成功；
6. 品牌口号在登录页/预览页/H5/三套布局侧栏统一展示。

**通过形式**：用户明确回复「验收通过，开始后端」之类指令。否则停留在阶段 0/1 循环修复。

## 6. 阶段 2：后端开发（验收前只规划、不写码）

### 6.1 关键决策（ADR）

**Option A：模块化单体 Spring Boot（选定）**
- 一个 Spring Boot 3 应用，按 domain 分包（auth / tenant / project / roadshow / order / knowledge / ai / graph / file）。
- Pros：与 25 页 Demo 规模匹配；本地 Docker Compose 一键起；事务跨域简单；团队维护成本最低。
- Cons：远期横向扩展需再拆分。

**Option B：微服务（否决）**
- 按域拆 6+ 服务 + 网关。
- 否决理由：当前无流量规模与团队规模支撑；会拖慢「验收后快速出可用后端」的第一目标；分布式事务反而引入风险。

**决策驱动**：上线速度 > 团队熟悉度 > 维护成本 > 远期扩展性。

### 6.2 技术栈
Spring Boot 3 + Spring Security（JWT）+ JPA/MyBatis-Plus + PostgreSQL（业务数据）+ NebulaGraph（关系图谱）+ MinIO（BP 文件）+ Redis（验证码/令牌/限流）+ Docker Compose。Maven 多模块或单模块按 domain 分包均可，实施时再定。

### 6.3 数据模型（直接对齐 shared types）
- PostgreSQL 表 = 20+ 共享实体的持久化映射：`sys_user`、`sys_tenant`、`biz_project`（含 amount_sought / valuation / equity_offered / status / view_count / favorite_count / bp_file_id）、`biz_project_application`、`biz_project_diagnosis`（+ dimension 子表或 JSONB）、`biz_investor_institution`（+ member 子表）、`biz_roadshow`（agenda/projects 用 JSONB）、`biz_roadshow_application`、`biz_order`、`biz_knowledge_base`、`biz_blacklist`、`sys_ai_config`（prompt 模板 + 模型路由两张）、`sys_file`。
- 枚举列用 CHECK 或字典表，取值严格等于 shared 枚举字符串（如 `project.status ∈ draft/pending_review/review_pass/review_reject/published/archived`）。
- NebulaGraph：Tag/Edge 对齐 `GraphNode`/`GraphGraphEdge`（project / institution / person / roadshow 节点，invests / applies / partners 等边），为机构端 RelationGraphView 供数。
- 多租户：业务表统一 `tenant_id` 列；平台域数据 `tenant_id` 为平台租户。

### 6.4 接口迁移映射（Mock → 真实，逐模块）
| Mock 模块 | 端点数 | 后端 Controller | 依赖 |
|---|---|---|---|
| user.ts | 6（login/me/wechat/refresh + 新增 register/reset-password） | AuthController | JWT、Redis 验证码 |
| tenant.ts | 4 | TenantController | 平台管理员权限 |
| project.ts | 6 | ProjectController + DiagnosisService | PG、MinIO bpFileId |
| roadshow.ts | 4 | RoadshowController | PG |
| order.ts | ~4 | OrderController | PG、（远期支付网关） |
| knowledge.ts | ~4 | KnowledgeController | PG |
| institution.ts | ~4 | InstitutionController | PG |
| ai.ts | 3（project-filter/review-brief/chat） | AiController | 大模型 API 网关、AIConfig 表 |
| blacklist.ts | ~4 | BlacklistController | PG |

验收标准：每个端点的请求/响应 JSON 与现有 mock 的入参出参**逐字段一致**（前端零改动切换）。

### 6.5 后端里程碑
- **M1 基础设施与认证**：工程骨架、PostgreSQL/Redis/MinIO Docker Compose、JWT 登录/刷新/注册/找回、租户拦截器（解析 `X-Tenant-ID` → TenantContext）、统一 `ApiResponse` 与异常码。
- **M2 核心业务域**：tenant / project（含诊断与审核流转）/ roadshow / institution / blacklist / knowledge / order 全 CRUD + 状态机。
- **M3 AI 与图谱**：AIConfig 管理、project-filter / review-brief / chat 接大模型；NebulaGraph 建图与查询接口。
- **M4 运营与加固**：订单结算导出、文件上传下载闭环、操作日志、限流、备份脚本。

### 6.6 鉴权与安全（高风险点）
- 登录发 JWT（access 2h + refresh 7d），`Authorization: Bearer` 校验；
- 每个请求校验 `X-Tenant-ID` 与当前用户所属租户一致（平台管理员放行）；
- 角色三值与 shared `UserRole` 一致，服务端逐接口鉴权（不依赖前端守卫）；
- 注册/找回密码验证码走 Redis 5 分钟过期 + 频率限制。

## 7. 阶段 3：前端切换真实后端

- `apps/web/.env.development` 增加 `VITE_API_BASE_URL=http://localhost:8080`；`src/utils/request.ts` 已读该变量，无需改码。
- `vite.config.ts` 按环境变量条件加载 vite-plugin-mock（默认关闭）。
- 切换策略（选定增量切换）：每完成一个后端模块，即把对应前端页面切到真实接口联调；全部切完后删除 `mock/` 目录与 vite-plugin-mock 依赖。
- 备选（否决）：一次性全量切换——风险集中、问题定位困难。
- 联调验收：阶段 1 的同一份 25 页检查清单在真实后端下重跑一遍。

## 8. 阶段 4：部署运维

- 根目录 `docker-compose.yml`：postgres / redis / minio / nebula（graphd+metad+storaged）/ backend / nginx（托管前端 `vite build` 产物）。
- Nginx 反代 `/api` → backend，静态资源直接服务；H5 与 PC 同域。
- CI（GitHub Actions 或等价）：前端 `vue-tsc + build`、后端 `mvn verify`、镜像构建推送。
- 观测：后端 Actuator + 结构化日志；数据库每日备份；MinIO 版本化。

## 9. 风险与缓解

| 风险 | 缓解 |
|---|---|
| 用户在 A1-A8 打磨未完成时就要求开始后端 | 重述硬约束，先把阶段 0/1 闭环 |
| Mock 契约与 PRD 有偏差导致后端返工 | M1 先出一份「契约核对表」逐端点比对 PRD 后再写 Controller |
| AI/图谱依赖外部服务不可用拖期 | M3 与 M2 并行推进；前端该部分本就是静态模拟，允许最后切换 |
| 无 git 环境导致改动无法回滚 | 每个任务完成后立即构建验证；大改前复制目标文件快照到临时目录 |
| 多租户数据越权 | M1 即落地 TenantContext 拦截 + M2 每域补越权测试用例 |

## 10. Pre-mortem（高风险场景）

1. **场景**：验收后发现 mock 与后端字段命名不一致，前端大量返工。
   触发：M2 联调阶段接口 400/字段缺失。
   缓解：6.4 的逐字段一致性验收前置到 M1 契约核对表；以 shared types 为唯一事实源。
2. **场景**：NebulaGraph 本地资源占用过高拖垮开发机。
   触发：docker compose up 后内存占满。
   缓解：M3 才启用 nebula 服务，compose profile 隔离；前期图谱接口用 PG 临时表模拟。
3. **场景**：鉴权多租户拦截器漏判导致越权数据泄露。
   触发：M2 越权测试失败或代码审查发现绕过路径。
   缓解：拦截器单测覆盖三角色 × 跨租户用例；上线前安全自查清单逐条过。

## 11. 本规格外（Follow-ups，需用户另行决策）

- 支付网关真实接入（当前订单仅状态模拟）；
- 微信扫码登录真实对接；
- 管理端操作审计日志前端页；
- 性能压测与生产环境部署方案。
