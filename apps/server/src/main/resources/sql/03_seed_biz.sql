-- ====================================================================
-- 创融通 FirstFuse - M2 业务域种子数据（幂等，可重复执行）
-- 对齐前端 mock/modules/*.ts 的演示数据
-- 由 spring.sql.init.mode=always 在应用启动时自动执行
-- ====================================================================

-- 补丁：知识库发布状态列（契约表 DDL 补丁 #1）
ALTER TABLE biz_knowledge ADD COLUMN IF NOT EXISTS published BOOLEAN NOT NULL DEFAULT TRUE;

-- ========== 项目（3 条市场项目 + 4 条审核队列） ==========
INSERT INTO biz_project (id, tenant_id, owner_id, title, subtitle, industry, sub_industry, region, stage, amount_sought, valuation, equity_offered, short_description, long_description, status, view_count, favorite_count, tags, founded_at) VALUES
('p-001', 't-001', 'u-004', '下一代 AI Agent 协作平台', '面向企业级的 AI Agent 协作编排平台', 'ai', '企业软件', '上海', 'pre_seed', 300, 3000, 15, '面向企业级的 AI Agent 协作编排平台', '完整项目描述...', 'published', 3420, 128, ARRAY['AI','Agent','企业服务'], '2025-06-01'),
('p-002', 't-001', 'u-004', '固态电池关键材料技术', '新型硫化物固态电解质材料', 'new_energy', '材料', '深圳', 'seed', 1200, 8000, 20, '新型硫化物固态电解质材料', '完整项目描述...', 'published', 2980, 96, ARRAY['新能源','固态电池'], '2024-03-01'),
('p-003', 't-002', 'u-003', '智能影像诊断 SaaS', '基于深度学习的医学影像辅助诊断系统', 'biotech', '医疗AI', '北京', 'series_a', 5000, 30000, 10, '基于深度学习的医学影像辅助诊断系统', '完整项目描述...', 'published', 2560, 88, ARRAY['医疗AI','SaaS'], '2022-08-01'),
('p-rv-001', 't-001', 'u-004', '下一代 AI Agent 协作平台', NULL, 'ai', NULL, '上海', 'pre_seed', 300, 3000, 15, '面向企业级的 AI Agent 协作编排平台', '完整项目描述...', 'pending_review', 0, 0, ARRAY['AI'], '2025-06-01'),
('p-rv-002', 't-002', 'u-003', '智能影像诊断 SaaS', NULL, 'biotech', NULL, '北京', 'series_a', 5000, 30000, 10, '基于深度学习的医学影像辅助诊断系统', '完整项目描述...', 'pending_review', 0, 0, ARRAY['医疗AI'], '2022-08-01'),
('p-rv-003', 't-001', 'u-004', '边缘计算芯片设计', NULL, 'semiconductor', NULL, '深圳', 'seed', 1200, 8000, 20, '自研边缘 AI 推理芯片', '完整项目描述...', 'pending_review', 0, 0, ARRAY['半导体'], '2024-03-01'),
('p-rv-004', 't-002', 'u-003', '固态电池关键材料技术', NULL, 'new_energy', NULL, '深圳', 'seed', 1200, 8000, 20, '新型硫化物固态电解质材料', '完整项目描述...', 'review_pass', 0, 0, ARRAY['新能源'], '2024-03-01')
ON CONFLICT (id) DO NOTHING;

-- ========== 路演（4 条） ==========
INSERT INTO biz_roadshow (id, tenant_id, organizer_id, title, description, type, is_closed_door, start_time, end_time, location, max_participants, current_participants, status) VALUES
('r-001', 't-001', 'u-002', '人工智能闭门路演 · 第12期', '红杉创投主办的人工智能专场闭门路演', 'online', TRUE,  '2026-11-15 14:00', '2026-11-15 17:30', NULL, 50, 45, 'ongoing'),
('r-002', 't-platform', 'u-001', '新能源产业专场路演', '慧联创融官方主办的新能源产业专场', 'online', FALSE, '2026-11-20 10:00', '2026-11-20 16:00', NULL, 150, 128, 'published'),
('r-003', 't-002', 'u-003', '医疗健康创新路演', '达晨创投主办的医疗健康创新路演', 'online', TRUE,  '2026-12-05 14:00', '2026-12-05 18:00', NULL, 80, 62, 'published'),
('r-004', 't-platform', 'u-001', '半导体/芯片产业闭门路演', '慧联创融官方主办的半导体闭门路演', 'online', TRUE, '2026-12-10 09:30', '2026-12-10 17:00', NULL, 40, 38, 'draft')
ON CONFLICT (id) DO NOTHING;

-- ========== 知识库（5 条） ==========
INSERT INTO biz_knowledge (id, tenant_id, title, category, content, tags, view_count, published, created_at, updated_at) VALUES
('kn-001', 't-platform', '好 BP 的标准：投资人眼中的项目计划书', 'BP写作', '（正文内容）', ARRAY['BP','投资人视角']::text[], 15230, TRUE, '2026-08-15', '2026-10-10'),
('kn-002', 't-platform', '创业公司估值方法全解析：从可比公司到 DCF', '估值方法', '（正文内容）', ARRAY['估值','财务']::text[], 12860, TRUE, '2026-07-20', '2026-09-25'),
('kn-003', 't-platform', 'Term Sheet 逐条解读：融资条款怎么谈判', '法律文件', '（正文内容）', ARRAY['TS','条款']::text[], 10520, TRUE, '2026-06-10', '2026-08-01'),
('kn-004', 't-platform', '融资全流程指南：从天使轮到 Pre-IPO', '融资流程', '（正文内容）', ARRAY['融资','流程']::text[], 9860, TRUE, '2026-05-01', '2026-07-15'),
('kn-005', 't-platform', '人工智能行业投资趋势报告 2026', '行业研究', '（正文内容）', ARRAY['AI','行业报告']::text[], 8230, TRUE, '2026-09-15', '2026-10-15')
ON CONFLICT (id) DO NOTHING;

-- ========== 订单（5 条，id = orderNo） ==========
INSERT INTO biz_order (id, tenant_id, user_id, type, title, amount, currency, status, paid_at, created_at) VALUES
('ORD20261019001', 't-001', 'u-002', 'subscription', '企业版年付订阅', 29800, 'CNY', 'paid', '2026-10-19 10:31', '2026-10-19 10:30'),
('ORD20261018002', 't-002', 'u-003', 'subscription', '专业版年付订阅', 9800, 'CNY', 'paid', '2026-10-18 14:21', '2026-10-18 14:20'),
('ORD20261015003', 't-003', 'u-002', 'service', 'AI 评审简报增值包（50份）', 5000, 'CNY', 'pending', NULL, '2026-10-15 09:00'),
('ORD20261010004', 't-001', 'u-002', 'extension', '套餐升级（基础版→专业版）', 5000, 'CNY', 'paid', '2026-10-10 16:42', '2026-10-10 16:40'),
('ORD20260920005', 't-002', 'u-003', 'subscription', '专业版年付订阅', 9800, 'CNY', 'refunded', '2026-09-20 11:01', '2026-09-20 11:00')
ON CONFLICT (id) DO NOTHING;

-- ========== 黑名单（5 条） ==========
INSERT INTO sys_blacklist (id, type, target_value, reason, operator_id, expires_at, created_at) VALUES
('bl-001', 'user',    'user-fraud-001',       '涉嫌伪造项目资料，重复注册多个账号提交', 'u-001', NULL, '2026-10-15 14:30'),
('bl-002', 'tenant',  'tenant-spam-002',      '该租户下所有项目均为虚假项目，用于刷榜', 'u-001', NULL, '2026-10-12 09:15'),
('bl-003', 'project', 'AI项目-虚假BP-003',    'AI 审核识别为完全重复的 BP 内容',       'u-001', '2027-10-10', '2026-10-10 23:58'),
('bl-004', 'phone',   '138****9999',          '关联多个黑名单账号',                     'u-001', NULL, '2026-10-08 16:20'),
('bl-005', 'ip',      '192.168.1.100',        '异常高频请求，疑似爬虫/恶意攻击',        'u-001', '2026-11-05', '2026-10-05 11:00')
ON CONFLICT (id) DO NOTHING;
