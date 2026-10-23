-- ====================================================================
-- 创融通 FirstFuse - M3 AI 域结构补丁与种子（幂等）
-- 对齐前端 mock/modules/admin.ts 的 prompts / modelRoutes 与 ai.ts
-- ====================================================================

-- Prompt 模板表（mock: {id,name,scene,model,enabled,usageCount,avgScore,isDefault}）
CREATE TABLE IF NOT EXISTS sys_ai_prompt (
    id              VARCHAR(64)   PRIMARY KEY,
    name            VARCHAR(255)  NOT NULL,
    scene           VARCHAR(50)   NOT NULL,
    model           VARCHAR(100)  NOT NULL,
    prompt_template TEXT          NOT NULL DEFAULT '',
    enabled         BOOLEAN       NOT NULL DEFAULT TRUE,
    usage_count     INTEGER       NOT NULL DEFAULT 0,
    avg_score       NUMERIC(3,1)  NOT NULL DEFAULT 0,
    is_default      BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- 模型路由：复用 sys_ai_config，补 mock 需要的列
ALTER TABLE sys_ai_config ADD COLUMN IF NOT EXISTS scene          VARCHAR(50);
ALTER TABLE sys_ai_config ADD COLUMN IF NOT EXISTS primary_model  VARCHAR(100);
ALTER TABLE sys_ai_config ADD COLUMN IF NOT EXISTS fallback_model VARCHAR(100);

-- 清理 M1 阶段的旧路由键（与 mock 路由键体系不一致）
DELETE FROM sys_ai_config WHERE id IN ('ai-001', 'ai-002', 'ai-003');

-- Prompt 模板种子（5 条，与 mock/id 一致）
INSERT INTO sys_ai_prompt (id, name, scene, model, enabled, usage_count, avg_score, is_default, prompt_template) VALUES
('pf-001', '项目诊断-商业模式分析', 'diagnosis',    'gpt-4o',      TRUE, 1256, 4.6, TRUE,  '你是一位资深投资人，请分析该项目的商业模式...'),
('pf-002', '项目诊断-团队评估',     'diagnosis',    'gpt-4o',      TRUE,  980, 4.5, FALSE, '请评估该项目的核心团队背景与能力匹配度...'),
('pf-003', 'BP打磨助手-结构优化',   'bp_assistant', 'gpt-4o-mini', TRUE, 2340, 4.7, TRUE,  '请对该 BP 的结构与表达给出优化建议...'),
('pf-004', 'AI项目筛选-匹配分析',   'ai_filter',    'gpt-4o',      TRUE,  560, 4.4, TRUE,  '请根据投资人偏好对候选项目进行匹配打分...'),
('pf-005', '评审简报-生成',         'review_brief', 'gpt-4o',      TRUE,  320, 4.8, TRUE,  '请阅读项目 BP 并生成投资人评审简报...')
ON CONFLICT (id) DO NOTHING;

-- 模型路由种子（5 条，id = routeKey，与 mock 一致）
INSERT INTO sys_ai_config (id, route_key, scene, primary_model, fallback_model, model, prompt_template, temperature, max_tokens, enabled) VALUES
('diagnosis',    'diagnosis',    '融资诊断', 'gpt-4o',      'gpt-4o-mini', 'gpt-4o',      '你是一位资深投资人，请对以下项目给出融资诊断...', 0.3, 2000, TRUE),
('bp_assistant', 'bp_assistant', 'BP打磨',   'gpt-4o',      'gpt-4o-mini', 'gpt-4o',      '请阅读以下项目 BP，给出打磨建议...',             0.7, 4000, TRUE),
('ai_filter',    'ai_filter',    '项目筛选', 'gpt-4o',      'gpt-4o-mini', 'gpt-4o',      '请根据以下创业者需求，推荐匹配的投资人...',       0.2, 1000, TRUE),
('review_brief', 'review_brief', '评审简报', 'gpt-4o',      '',            'gpt-4o',      '请阅读以下项目 BP，生成一份投资人评审简报...',   0.4, 6000, TRUE),
('chat',         'chat',         '通用对话', 'gpt-4o-mini', '',            'gpt-4o-mini', '你是创融通的融资助手，请回答用户的问题...',       0.8, 2000, TRUE)
ON CONFLICT (route_key) DO NOTHING;

-- ====================================================================
-- 图谱演示数据（/graph 由 PG 派生节点与边；机构 inst-001/inst-002 来自 02_seed.sql）
-- ====================================================================

-- 机构成员（person 节点）
INSERT INTO biz_institution_member (id, institution_id, name, position, bio) VALUES
('im-001', 'inst-001', '沈南鹏', '创始及执行合伙人', '红杉中国创始人'),
('im-002', 'inst-001', '周逵',   '合伙人',           '专注硬科技与医疗投资'),
('im-003', 'inst-002', '肖冰',   '总裁',             '达晨创投总裁')
ON CONFLICT (id) DO NOTHING;

-- 投资关系（institution → project，edge: invested_in）
INSERT INTO biz_portfolio_project (id, tenant_id, institution_id, project_id, investment_amount, investment_date, post_money_valuation, equity_percent, current_stage, status) VALUES
('pf-inv-001', 't-001', 'inst-001', 'p-001', 1200, '2025-06-15', 8000, 15.0, 'seed', 'active'),
('pf-inv-002', 't-001', 'inst-001', 'p-003', 900,  '2025-09-20', 6000, 12.0, 'series_a', 'active'),
('pf-inv-003', 't-002', 'inst-002', 'p-003', 1500, '2026-01-10', 9000, 10.0, 'series_a', 'active')
ON CONFLICT (id) DO NOTHING;

-- 项目申请（institution → project，edge: applies）
INSERT INTO biz_project_application (id, tenant_id, project_id, investor_tenant_id, status, cover_letter) VALUES
('pa-001', 't-002', 'p-001', 't-002', 'viewed',      '关注 AI Agent 方向，希望进一步沟通'),
('pa-002', 't-001', 'p-003', 't-001', 'shortlisted', '医疗影像赛道与我们有协同')
ON CONFLICT (id) DO NOTHING;
