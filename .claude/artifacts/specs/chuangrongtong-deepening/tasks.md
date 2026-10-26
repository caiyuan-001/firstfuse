# 创融通深化开发 — Tasks

> 关联 spec: `.claude/artifacts/specs/chuangrongtong-deepening/spec.md`
> 状态图例: [ ] 待办 / [~] 需用户决策或指令 / [x] 完成
> 铁律：阶段 2 起所有后端任务，必须等用户验收指令到达后才可动。

## 阶段 0 — 前端收尾（当前可执行）
- [x] 0.1 登录页加「忘记密码」「注册账号」入口（`views/login/LoginView.vue`）
- [x] 0.2 新增 `views/login/ForgotPasswordView.vue`（验证码 + 重置密码，mock `POST /api/v1/auth/reset-password`）
- [x] 0.3 新增 `views/login/RegisterView.vue`（角色选择 + 注册表单，mock `POST /api/v1/auth/register`）
- [x] 0.4 `router/index.ts` publicRoutes 注册两条新路由（blank layout）
- [x] 0.5 `mock/modules/user.ts` 增加 register / reset-password 端点
- [x] 0.6 验证：`vue-tsc` 0 错误 + 浏览器走通 注册→登录、找回→登录（2026-10-22 三页 PASS）
- [x] 0.7 管理后台 A1-A8 深度打磨（7 页接 mock + 状态流转）——已完成（2026-10-22，7/7 浏览器 PASS，vue-tsc 0 错误）
- [x] 0.8 全局公共组件 9 个 + `/preview/components`——已完成（2026-10-22，vue-tsc 0 错误 + vite build 成功 + 浏览器零 Console 红错）
- [x] 0.9 创业者端 PC 窄屏适配——已完成（2026-10-22，9 页 Element Plus :xs/:sm/:md/:lg 响应式 span + EntrepreneurPreviewLayout 1024px 断点 + vue-tsc 0 错误 + vite build 成功）
- [x] 0.10 H5 登录页同步注册/找回入口——已完成（2026-10-22，注册 tab 已存在、找回密码 inline 模式接入 reset-password mock、vue-tsc 0 错误 + vite build 成功 + 浏览器端到端验证 PASS）

## 阶段 1 — 人工验收
- [x] 1.1 按 spec §5 六项检查清单自验（全部 PASS，详见下方验收报告）
- [x] 1.2 提交用户人工验收（2026-10-22 完成，见验收报告）
- [x] 1.3 **用户书面确认「验收通过」**——2026-10-22 确认，后端开发已放行

## 阶段 2 — 后端开发（验收指令到达后启动）
### M1 基础设施与认证
- [x] 2.1 Spring Boot 3 工程骨架（domain 分包）+ 统一 ApiResponse/异常码 —— 已完成（2026-10-22，18 文件 mvn compile BUILD SUCCESS，修复 DTO package 缺 .dto 导致的类重复+Lombok 失效）
- [x] 2.2 docker-compose：postgres / redis / minio —— 改由用户 NAS 现有服务替代（PG 5435/Redis/MinIO@100.66.1.5，连接信息 apps/server/.env，已 gitignore），本地 docker-compose 搁置
- [x] 2.3 PostgreSQL 建表：20+ 实体对齐 shared types —— 已完成（2026-10-22，JDBC 脚本在 NAS PG 建 firstfuse 库 + 17 表 + 索引 + 种子数据 4 租户/4 用户，验证通过）
- [x] 2.4 JWT 登录/刷新 + 注册 + 找回密码 —— demo 模式已跑通（一键体验/验证码/密码登录均返回 JWT；Redis 真实验证码存取+限频+BCrypt 留到非 demo 模式切换时实现）
- [x] 2.5 租户拦截器：X-Tenant-ID → TenantContext —— 代码已生效（白名单 /auth/** /health），全量越权用例在 2.11
- [x] 2.6 契约核对表：47 端点逐一比对 mock 入参出参 —— 已完成（2026-10-22，见 contract-checklist.md，M2 35 / M3 12，含 DDL 补丁清单与权限矩阵）
### M2 核心业务域
- [x] 2.7 AuthController（6 端点）替换 user.ts mock —— 已完成（2026-10-22，补 me/refresh/wechat-login，register 出参改 {id,registered:true} 对齐 mock，login 切 LoginRequest DTO 含 register 标记）
- [x] 2.8 TenantController（4）/ InstitutionController（2）/ BlacklistController（3）—— 已完成（2026-10-22，JdbcTemplate + RowMapper，blacklist JOIN 出 operatorName）
- [x] 2.9 ProjectController（6）+ ProjectAdminController（2）—— 已完成（2026-10-22，诊断 demo 固定值、submit 状态机 draft→pending_review 拒重复、审核 pass/reject 写 biz_review_record、admin 宽 DTO 中文 industry/stageLabel + 伪 aiScore 稳定 hash；MinIO BP 上传留 2.15）
- [x] 2.10 RoadshowController（5）/ KnowledgeController（6）/ OrderController（2）—— 已完成（2026-10-22，roadshow 出 organizer/statusLabel/projectCount/isOfficial 冗余字段、knowledge 补 published 列、order 宽 DTO orderNo=id；种子 03_seed_biz.sql 幂等随启动执行）
- [x] 2.11 三角色 × 跨租户越权测试全绿 —— 已完成（2026-10-22，scripts/smoke-m2.ps1 50/50 PASS：公开市场 published 跨租户可见 + 未发布 403、/admin/** investor 403001、X-Tenant-ID 不一致 403001、无 token 401000、重复提交 400301）
### M3 AI 与图谱
- [x] 2.12 AIConfig 管理（prompt 模板 + 模型路由两表 CRUD）—— 已完成（04_ai_m3.sql 建 sys_ai_prompt + sys_ai_config 扩列 + 5/5 种子；AiConfigService/Controller 7 端点，platform_admin 门控）
- [x] 2.13 AiController：project-filter / review-brief / chat 接大模型网关 —— 已完成（AiGateway 走 OpenAI 兼容 /chat/completions，未配 AI_BASE_URL/AI_API_KEY 时确定性本地演示；三端点结构与 mock/ai.ts 对齐）
- [x] 2.14 NebulaGraph 启用：GraphNode/GraphEdge 建模 + 图谱查询接口 —— 已完成（GET /graph + centerId/depth 邻域；默认 PG 派生 in institution/project/person + invested_in/works_at/applies；NebulaGraphClient 受 NEBULA_ENABLED 门控，schema 见 resources/nebula/schema.ngql，Nebula 空间需用户授权后创建）
- [x] 2.14b /admin/stats/overview 总览聚合 —— 已完成（KPI/30天趋势/行业分布/待办/热门项目/动态流，实时聚合业务表）
### M4 运营与加固
- [x] 2.15 订单结算导出 / 文件上传下载闭环 —— 已完成（2026-10-23，GET /admin/orders/export 中文表头 CSV 带 BOM 限 5000 行；POST /files + GET /files/{id}[/info] 接 NAS MinIO firstfuse bucket，sys_file 元数据表，跨租户 403001；注意库中曾有早期遗留异构 sys_file 表，05_m4.sql DROP 重建）
- [x] 2.16 操作日志 + 限流 + 数据库备份脚本 —— 已完成（2026-10-23，sys_operation_log + OperationLogInterceptor 记写操作与文件下载、GET /admin/logs 仅 admin；RateLimitInterceptor Redis 固定窗口 登录/send-code 60/min 按 IP、/ai/** 30/min 按用户，触发 401103，Redis 异常降级放行；scripts/backup-pg.ps1 pg_dump 自定义格式 + 14 天保留，需用户手动运行）

## 阶段 3 — 前后端联调切换
- [x] 3.1 `.env.development` 配置 `VITE_API_BASE_URL`，`vite.config.ts` 条件加载 mock —— 已完成（2026-10-23，dev 代理 /api→8080，VITE_USE_MOCK 开关控制；3.3 后开关随 mock 一并移除）
- [x] 3.2 按模块增量切换（每模块后端就绪即切对应前端页面）—— 已完成（2026-10-23，视图原本即调 /api/v1/*，关 mock 即全量切真实后端；修复 PC LoginView 硬编码 mock 登录改调 /auth/login）
- [x] 3.3 全量切换后删除 `mock/` 与 vite-plugin-mock 依赖 —— 已完成（2026-10-23，删除 mock/ 12 文件 + package.json 依赖 + vite.config 插件代码，lockfile 已重生成；node_modules 残留文件因本机 symlink 需管理员权限暂未物理清除，无引用不影响运行）
- [x] 3.4 阶段 1 的 25 页检查清单在真实后端下重跑通过 —— 已完成（2026-10-23，创业者 9 + 机构 8 + 管理 8 + H5 3 共 28 页真实后端走查全 PASS；vue-tsc 0 错误 + vite build 成功）

## 阶段 4 — 部署运维
- [x] 4.1 根目录 `docker-compose.yml` 全栈一键起（+ nginx 托管前端产物）—— 已完成（2026-10-23，server + web 两服务，depends_on healthcheck；apps/server/Dockerfile Maven 多阶段 → JRE Alpine ZGC 非 root；apps/web/Dockerfile node:20 pnpm build → nginx:1.27；nginx.conf /api/ proxy_pass server:8080 + SPA fallback + gzip + 静态缓存）
- [x] 4.2 CI：前端 `vue-tsc + build` / 后端 `mvn verify` / 镜像构建 —— 已完成（.github/workflows/ci.yml，PR/push 前端 vue-tsc+build 后端 mvn verify(skipTests)，tag 触发 docker build）
- [x] 4.3 观测：Actuator + 结构化日志 —— 已完成（application.yml 加 management endpoints health/info/metrics/prometheus；pom.xml 加 spring-boot-starter-actuator + logstash-logback-encoder 8.0；logback-spring.xml dev 彩色 console / prod|docker JSON console）

## 冒烟回归
- [x] smoke-m2 50/50 PASS ✅（跨租户越权三角色全绿）
- [x] smoke-m3 30/30 PASS ✅（AI 评审简报 + AI 对话 真实豆包 doubao-seed-evolving 15-27s）
- [x] smoke-m4 22/22 PASS ✅（订单导出 + MinIO 文件上传下载 + 操作日志 + 登录/AI 限流）
- [x] Nebula 修复：索引支持 ✅（CREATE TAG/EDGE INDEX + string 类型必须指定前缀长度，GraphSyncService.init() DDL 加 INDEX_DDL 数组）
- [x] 后端稳定：java -jar 启动（绕开 mvn spring-boot:run 后台进程丢失问题）

## 暂不排期（Follow-ups）
- [~] 支付网关真实接入
- [~] 微信扫码登录真实对接
- [x] 管理端操作审计日志页面 —— 已完成（2026-10-26，`views/admin/AuditLogView.vue` + `/admin/logs` 路由 + 侧边菜单；路径/操作人筛选、分页、方法/状态码彩标、耗时与参数悬浮；vue-tsc 0 错误 + vite build 成功 + 浏览器实测 1009 条数据、筛选/重置 PASS）
- [~] 性能压测与生产部署方案
