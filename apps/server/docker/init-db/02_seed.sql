-- ====================================================================
-- 创融通 FirstFuse - 初始化种子数据
-- 与前端 mock/modules/*.ts 返回的数据结构严格对齐
-- 1 个平台租户 + 3 个机构租户 + 演示用户 + 演示项目/路演
-- ====================================================================

-- 租户
INSERT INTO sys_tenant (id, name, type, status, contact_name, contact_phone, contact_email, industry, subscription_plan, expire_at, max_users) VALUES
('t-001', '红杉创投',    'institution', 'active', '李投资', '13888888000', 'li@sequoia.com',   '人工智能', '企业版', '2027-06-30', 20),
('t-002', '达晨创投',    'institution', 'active', '王经理', '13911112222', 'dachen@vc.com',    '医疗健康', '专业版', '2027-03-15', 10),
('t-003', 'XX资本',      'institution', 'trial',  '张总',   '13655557777', 'zh@xxvc.com',      '新能源',   '基础版', '2026-11-15', 5),
('t-platform', '慧联创融平台', 'platform', 'active', '平台运营', '4008888888', 'support@firstfuse.com', '平台', '平台版', '2099-12-31', 999);

-- 用户（密码统一 hash 为 123456 的 BCrypt：$2a$10$... — 实际由 2.4 AuthService 注册时自动生成）
-- 这里只插演示数据，密码字段留空字符串，auth/login 端点在 demo 模式下跳过密码校验
INSERT INTO sys_user (id, tenant_id, role, nickname, phone, password_hash, company, position, real_name_verified) VALUES
('u-001', 't-platform', 'platform_admin', '平台管理员', '13800000000', '', '慧联创融', '平台运营', TRUE),
('u-002', 't-001',      'investor',       '李投资',     '13800138001', '', '红杉创投', '投资经理', TRUE),
('u-003', 't-002',      'investor',       '王经理',     '13800138002', '', '达晨创投', '投资总监', TRUE),
('u-004', 't-001',      'entrepreneur',   '张创业',     '13800138003', '', 'AI初创',   'CEO',     FALSE);

-- AI 配置（演示模式）
INSERT INTO sys_ai_config (id, prompt_template, model, route_key, temperature, max_tokens, enabled) VALUES
('ai-001', '你是一位资深投资人，请对以下项目给出融资诊断...', 'gpt-4o', 'project-diagnosis', 0.7, 2048, TRUE),
('ai-002', '请阅读以下项目 BP，生成一份投资人评审简报...', 'gpt-4o', 'review-brief',       0.5, 2048, TRUE),
('ai-003', '请根据以下创业者需求，推荐匹配的投资人...', 'gpt-4o', 'project-filter',     0.7, 1024, TRUE);

-- 机构信息
INSERT INTO biz_institution (id, tenant_id, name, region, focus_industries, preferred_stages, min_investment, max_investment, total_aum, description) VALUES
('inst-001', 't-001', '红杉创投', '北京', ARRAY['人工智能','医疗健康'], ARRAY['pre_seed','seed','series_a'], 500, 5000, 50000, '中国顶级 VC 机构之一，聚焦早期硬科技'),
('inst-002', 't-002', '达晨创投', '深圳', ARRAY['医疗健康','新能源'], ARRAY['seed','series_a','series_b'], 1000, 10000, 30000, '老牌人民币基金，TMT + 医疗双主赛道');
