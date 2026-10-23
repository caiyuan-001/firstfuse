# 创融通 M2 契约核对表（mock ↔ 后端）

> 关联 tasks: 2.6 ｜ 生成: 2026-10-22
> 事实源: `apps/web/mock/modules/*.ts`（10 模块）、`packages/shared/src/types/index.ts`、`apps/server/docker/init-db/01_schema.sql`
> 响应包装统一: `{ code:200, message:'success', data:T, timestamp:ms }`；分页统一 `data:{ list,total,pageNum,pageSize,totalPages }`
> 端点总数: **47**（M2 35 / M3 12）

## 通用约定（全表适用）

- 出参字段名一律 camelCase（与 shared types 一致）；PostgreSQL 列 snake_case 由 SQL 别名/RowMapper 转换
- 时间格式：mock 混用 ISO（`2026-10-01T...`）与纯日期（`2026-10-01`）；后端统一 ISO-8601（jackson 已配 UTC）
- 分页参数 query：`pageNum`（默认 1）、`pageSize`（各域默认不同，见下）；`totalPages = ceil(total/pageSize)`
- 鉴权：非 `/auth/**`、`/health` 均需 `Authorization: Bearer` + `X-Tenant-ID`；platform_admin 可跨租户

---

## 一、Auth 域（user.ts，6 端点 + send-code）— 2.7

| # | 端点 | 方法 | 入参 | 出参 data | mock 行为 | 后端要点 |
|---|---|---|---|---|---|---|
| 1 | /auth/login | POST | `{role?,phone?,code?,password?,register?}` | `{token,user:User}` | 按 role 映射昵称张创业/李投资/王运营，固定 demo 租户 | 已实现（M1）；2.7 切 LoginRequest DTO；登录模式：一键体验(仅role)/密码/验证码 |
| 2 | /auth/me | GET | — | `User` | 固定 investor 用户 | **缺**：JwtService 解析 token → 查 demo 用户返回 User；role/tenantId 来自 token claims |
| 3 | /auth/wechat/login | POST | — | `{token}` | 固定 mock token | **缺**：demo 返回固定 token（真实对接在 Follow-ups） |
| 4 | /auth/refresh | POST | — | `{token}` | 新 token | **缺**：校验旧 token 有效 → 签新 access token |
| 5 | /auth/register | POST | `{role,phone,code,inviteCode?}` | `{id,registered:true}` | 返回新 id+registered | 已实现但出参是 AuthResponse；**2.7 改为 `{id,registered:true}` 对齐 mock** |
| 6 | /auth/reset-password | POST | `{phone,code,newPassword}` | `{reset:true}` | 固定成功 | 已实现 ✓ |
| 7 | /auth/send-code | POST | `{phone}` | `{sent:true,demo_code:'123456'}` | mock 未定义（前端直调） | 已实现 ✓（保留 demo_code 提示字段） |

**差异警示**：register 出参不一致——后端当前返回 AuthResponse，mock 返回 `{id,registered}`。前端 RegisterView 已按 mock 出参处理，必须改后端。

## 二、Tenant 域（tenant.ts，4 端点）— 2.8

| # | 端点 | 方法 | 入参 | 出参 data | mock 行为 | 后端要点 |
|---|---|---|---|---|---|---|
| 8 | /tenants | GET | `keyword?,type?,status?,pageNum?,pageSize?=20` | `Page<Tenant>` | name 模糊 + type/status 精确过滤 | platform_admin only；查 sys_tenant |
| 9 | /tenants/:id | GET | — | `Tenant\|null` | find or null | platform_admin or 本租户 |
| 10 | /tenants | POST | Tenant 表单 | `Tenant`（新对象） | 返回 t-003 克隆改 id | platform_admin only；INSERT sys_tenant 返回新行 |
| 11 | /tenants/:id/status | PUT | `{status}` | `{success:true}` | 固定成功 | platform_admin only；UPDATE status |

## 三、Institution 域（institution.ts，2 端点）— 2.8

| # | 端点 | 方法 | 入参 | 出参 data | mock 行为 | 后端要点 |
|---|---|---|---|---|---|---|
| 12 | /institutions | GET | `pageNum?,pageSize?=12` | `Page<Institution>` | 全量分页（无过滤参数） | 查 biz_institution；**注意 mock 数据无 tenantId 字段，Entity 出参含 tenantId 亦可（前端不消费）** |
| 13 | /institutions/:id | GET | — | `Institution\|null` | find or null | 查单条 |

## 四、Blacklist 域（blacklist.ts，3 端点）— 2.8

| # | 端点 | 方法 | 入参 | 出参 data | mock 行为 | 后端要点 |
|---|---|---|---|---|---|---|
| 14 | /blacklist | GET | `keyword?,type?,pageNum?,pageSize?=20` | `Page<Entry>` | targetValue/reason 模糊 + type 精确 | platform_admin only；mock 多一个 `operatorName` 字段（shared 无）——JOIN sys_user 或存 operatorName 快照 |
| 15 | /blacklist | POST | `{type,targetValue,reason,expiresAt?}` | `{id}` | unshift 新条目 | INSERT sys_blacklist，operatorId 取当前用户 |
| 16 | /blacklist/:id | DELETE | — | `{success:true}` | 删除 | platform_admin only |

**差异警示**：mock 出参含 `operatorName`，DB 只有 operator_id → 查询时 LEFT JOIN sys_user 取 nickname。

## 五、Project 域（project.ts，8 端点）— 2.9

| # | 端点 | 方法 | 入参 | 出参 data | mock 行为 | 后端要点 |
|---|---|---|---|---|---|---|
| 17 | /projects | GET | `pageNum?,pageSize?=10` | `Page<Project>` | 全量分页（无过滤） | 查 biz_project；公开市场语义：published 全平台可见，draft 等仅 owner/同租户 |
| 18 | /projects/:id | GET | — | `Project\|null` | find or null | 同上权限；tags TEXT[] → 数组 |
| 19 | /projects | POST | Project 表单 | `{id:'new-p-001'}` | 固定 id | owner=当前用户，tenant=当前租户，status=draft |
| 20 | /projects/:id | PUT | Project 表单 | `{success:true}` | 固定成功 | 仅 owner/同租户可改 |
| 21 | /projects/:id/submit | POST | — | `{success:true}` | 固定成功 | 状态机 draft→pending_review（PROJECT_STATUS_INVALID 拦截非法流转） |
| 22 | /projects/diagnosis | POST | —（demo 不读 body） | `{overallScore,dimensions:[{name,score,fullMark,description}]}` | 固定 78 分 5 维度 | demo 固定返回；M3 接 AI 后由 2.13 替换 |
| 23 | /admin/projects/reviews | GET | `status?(pending/pass/reject/all),pageNum?,pageSize?=50` | `Page<ReviewItem>` | tab 名映射 pending→pending_review 等；含 aiScore/aiComment/aiSuspicious/ownerName/submitTime 等扩展字段 | platform_admin only；查 biz_project(pending_review 等) **JOIN sys_user 得 ownerName**；aiScore 等字段 demo 用固定值或存 biz_review_record |
| 24 | /admin/projects/reviews/:id | PUT | `{decision:'pass'\|'reject',reason?}` | `{success:true}` | pass→review_pass；reject→review_reject+rejectReason | platform_admin only；更新 biz_project.status + 写 biz_review_record |

**差异警示**：#23 mock 出参是宽 DTO（非 shared Project）：`{id,title,industry,stageLabel,amount,ownerName,submitTime,shortDesc,longDesc,region,valuation,equityOffered,foundedAt,aiScore,aiComment,aiSuspicious,status,rejectReason?}`，industry/stage 是中文标签。后端必须按此形状返回（stageLabel/industry 中文映射在前端 tab 或后端做——**选后端映射**，与 mock 一致）。

## 六、Roadshow 域（roadshow.ts，5 端点）— 2.10

| # | 端点 | 方法 | 入参 | 出参 data | mock 行为 | 后端要点 |
|---|---|---|---|---|---|---|
| 25 | /roadshows | GET | `keyword?,organizer?,status?,pageNum?,pageSize?=12` | `Page<RoadshowWide>` | title/organizer 模糊 + status 精确；出参含 organizer(名称)/projectCount/participants/statusLabel/isOfficial | 查 biz_roadshow JOIN sys_tenant 得 organizer 名称；projectCount=数组长度；statusLabel 后端映射 |
| 26 | /roadshows/:id | GET | — | `RoadshowWide`（缺省 `{}`） | find or `{}` | 注意：mock 缺省返回 `{}` 非 null，后端返回 null 可接受（前端判空容错） |
| 27 | /roadshows | POST | Roadshow 表单 | `{id}` | unshift，默认 draft | organizerId=当前用户；agenda/projects JSONB/TEXT[] |
| 28 | /roadshows/:id | PUT | 部分字段 | `{success:true}` | Object.assign | organizer 所在租户可改 |
| 29 | /roadshows/:id/apply | POST | — | `{success:true}` | 固定成功 | 写 biz_roadshow_application(pending) |

**差异警示**：mock 出参字段与 shared Roadshow 不同（organizer 而非 organizerId 直出、projectCount/participants 冗余字段、statusLabel、isOfficial、type 还出现了 mock 特有值 `hybrid`——DB CHECK 只允许 online/offline，**type 列建议迁移为放开约束或后端拒绝 hybrid**；稳妥：建表已固定 online/offline，r-002 种子须写 online，前端展示不受影响）。查 01_schema.sql 后确认：biz_roadshow.type CHECK ('online','offline')，种子数据未见 roadshow → 无冲突。

## 七、Knowledge 域（knowledge.ts，6 端点）— 2.10

| # | 端点 | 方法 | 入参 | 出参 data | mock 行为 | 后端要点 |
|---|---|---|---|---|---|---|
| 30 | /knowledge | GET | `keyword?,category?,pageNum?,pageSize?=20` | `Page<KnowledgeWide>` | title 模糊 + category 精确；出参多 `published` 字段 | 查 biz_knowledge；**DB 无 published 列 → 需 ALTER TABLE 加列或出参恒 true**（选：加列 BOOLEAN DEFAULT TRUE，需补 DDL） |
| 31 | /knowledge/:id | GET | — | `KnowledgeWide`（缺省 `{}`） | find or `{}` | 单查 |
| 32 | /knowledge | POST | `{title,category,content,tags?}` | `{id}` | unshift | INSERT，tenant=当前 |
| 33 | /knowledge/:id | PUT | 部分字段 | `{success:true}` | assign+updatedAt | 本租户可改 |
| 34 | /knowledge/:id/publish | PATCH | `{published?}` | `{success:true}` | 切换 published | UPDATE published |
| 35 | /knowledge/:id | DELETE | — | `{success:true}` | 删除 | 本租户/平台可删 |

## 八、Order 域（order.ts，2 端点）— 2.10

| # | 端点 | 方法 | 入参 | 出参 data | mock 行为 | 后端要点 |
|---|---|---|---|---|---|---|
| 36 | /orders | GET | `orderNo?,tenant?,status?,pageNum?,pageSize?=20` | `Page<OrderWide>` | orderNo/tenantName 模糊 + status 精确；出参是宽 DTO：`{orderNo,tenantId,tenantName,type,title,amount,status,createdAt,paidAt}` | platform_admin 全量，其他角色仅本租户；**orderNo 即业务 id**——biz_order.id 直接用 ORD 序号；JOIN sys_tenant 得 tenantName |
| 37 | /orders/:id | GET | — | `OrderWide`（缺省 `{}`） | 按 orderNo find | 同上 |

**差异警示**：mock 出参无 currency/userId；amount 数字。id=orderNo（ORD20261019001 格式）。

---

## 九、AI 域（ai.ts，3 端点）— 2.13（M3）

| # | 端点 | 方法 | 出参 data | 归属 |
|---|---|---|---|---|
| 38 | /ai/project-filter | POST | `{results:[{projectId,matchScore,dimensions:{}}]}` | M3 接模型网关 |
| 39 | /ai/review-brief | POST | `{id,projectId,overallScore,sections[],overallAssessment,riskPoints[],highlights[]}` | M3 |
| 40 | /ai/chat | POST | `{reply,sessionId}` | M3 |

## 十、Admin/AIConfig 域（admin.ts，9 端点）— 2.12（M3）

| # | 端点 | 方法 | 出参 | 归属 |
|---|---|---|---|---|
| 41 | /admin/stats/overview | GET | `{kpis[],trend,tenantByIndustry,pie,todos,pendingCount,topProjects,feedStream}` | M3（图表数据聚合） |
| 42 | /admin/ai/prompts | GET | `[{id,name,scene,model,enabled,usageCount,avgScore,isDefault}]` | 2.12 两表 CRUD |
| 43 | /admin/ai/prompts | POST | `{id}` | 2.12 |
| 44 | /admin/ai/prompts/:id | PUT | `{success:true}` | 2.12 |
| 45 | /admin/ai/prompts/:id | DELETE | `{success:true}` | 2.12 |
| 46 | /admin/ai/models | GET | `[{routeKey,scene,primaryModel,fallbackModel,temperature,maxTokens,enabled}]` | 2.12 |
| 47 | /admin/ai/models | POST | `{success:true}` | 2.12 |
| 48 | /admin/ai/models/:id | PUT | `{success:true}` | 2.12（按 routeKey 定位） |

---

## DDL 补丁清单（2.8-2.10 执行时同步 NAS PG）

1. `biz_knowledge` 加 `published BOOLEAN NOT NULL DEFAULT TRUE`
2. `sys_blacklist` 加 `operator_name VARCHAR(100)`（或查询时 JOIN sys_user——选 JOIN，不改表）
3. `biz_order.id` 种子需用 ORD 前缀格式（已有种子检查）
4. admin reviews 宽字段（aiScore/aiComment/aiSuspicious）暂用 demo 固定值，不入库；rejectReason 存 biz_review_record.comment

## 权限矩阵（2.11 测试依据）

| 接口组 | entrepreneur | investor | platform_admin | 其他租户 |
|---|---|---|---|---|
| /auth/** | ✓ | ✓ | ✓ | 公开 |
| GET /projects(市场) | ✓ published+本租户 | ✓ published+本租户 | ✓ 全部 | 同左 |
| 写 /projects | ✓ 本租户 | ✗ | ✓ | ✗ 403 |
| /admin/**（reviews/tenants/blacklist/stats） | ✗ 403 | ✗ 403 | ✓ | ✗ |
| /orders | 本租户 | 本租户 | ✓ 全量 | ✗ 403 |
| /knowledge 读 | ✓ | ✓ | ✓ | ✓（公开知识库） |
| /knowledge 写 | 本租户 | 本租户 | ✓ | ✗ 403 |
