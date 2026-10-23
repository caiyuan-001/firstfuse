-- ====================================================================
-- 创融通 FirstFuse - PostgreSQL DDL
-- 生成时间: 2026-10-22
-- 与 packages/shared/src/types/index.ts 严格对齐
-- 枚举列用 CHECK 约束，取值 = shared enum 的小写字符串
-- ====================================================================

-- ========== 平台域 ==========

CREATE TABLE sys_tenant (
    id              VARCHAR(64)   PRIMARY KEY,
    name            VARCHAR(255)  NOT NULL,
    type            VARCHAR(20)   NOT NULL CHECK (type IN ('institution', 'platform')),
    status          VARCHAR(20)   NOT NULL CHECK (status IN ('active', 'frozen', 'trial')),
    contact_name    VARCHAR(100)  NOT NULL,
    contact_phone   VARCHAR(30)   NOT NULL,
    contact_email   VARCHAR(255),
    industry        VARCHAR(100),
    subscription_plan VARCHAR(50),
    expire_at       TIMESTAMPTZ,
    max_users       INTEGER,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE sys_user (
    id                    VARCHAR(64)   PRIMARY KEY,
    tenant_id             VARCHAR(64)   NOT NULL REFERENCES sys_tenant(id),
    role                  VARCHAR(30)   NOT NULL CHECK (role IN ('entrepreneur', 'investor', 'platform_admin')),
    nickname              VARCHAR(100)  NOT NULL,
    avatar                TEXT,
    phone                 VARCHAR(20),
    password_hash         VARCHAR(255),
    email                 VARCHAR(255),
    wechat_openid         VARCHAR(255),
    company               VARCHAR(255),
    position              VARCHAR(100),
    real_name_verified    BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ   NOT NULL DEFAULT now(),
    UNIQUE (tenant_id, phone)
);

-- ========== 项目域 ==========

CREATE TABLE biz_project (
    id                    VARCHAR(64)   PRIMARY KEY,
    tenant_id             VARCHAR(64)   NOT NULL REFERENCES sys_tenant(id),
    owner_id              VARCHAR(64)   NOT NULL REFERENCES sys_user(id),
    title                 VARCHAR(255)  NOT NULL,
    subtitle              TEXT,
    industry              VARCHAR(100)  NOT NULL,
    sub_industry          VARCHAR(100),
    region                VARCHAR(100)  NOT NULL,
    stage                 VARCHAR(30)   NOT NULL CHECK (stage IN ('angel','pre_seed','seed','series_a','series_b','series_c','pre_ipo','strategic')),
    amount_sought         NUMERIC(12,2) NOT NULL DEFAULT 0,
    valuation             NUMERIC(12,2),
    equity_offered        NUMERIC(5,2),
    short_description     TEXT,
    long_description      TEXT,
    business_model        TEXT,
    competitive_advantage TEXT,
    market_size           TEXT,
    revenue               NUMERIC(12,2),
    profit                NUMERIC(12,2),
    team_size             INTEGER,
    founded_at            DATE,
    website               VARCHAR(255),
    bp_file_id            VARCHAR(64),
    status                VARCHAR(30)   NOT NULL CHECK (status IN ('draft','pending_review','review_pass','review_reject','published','archived')) DEFAULT 'draft',
    view_count            INTEGER       NOT NULL DEFAULT 0,
    favorite_count        INTEGER       NOT NULL DEFAULT 0,
    tags                  TEXT[],
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE biz_project_diagnosis (
    id                    VARCHAR(64)   PRIMARY KEY,
    tenant_id             VARCHAR(64)   NOT NULL REFERENCES sys_tenant(id),
    project_id            VARCHAR(64)   NOT NULL REFERENCES biz_project(id),
    overall_score         INTEGER       NOT NULL DEFAULT 0,
    dimensions            JSONB         NOT NULL,  -- DiagnosisDimension[]
    ai_summary            TEXT,
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE biz_project_application (
    id                    VARCHAR(64)   PRIMARY KEY,
    tenant_id             VARCHAR(64)   NOT NULL REFERENCES sys_tenant(id),
    project_id            VARCHAR(64)   NOT NULL REFERENCES biz_project(id),
    investor_tenant_id    VARCHAR(64)   NOT NULL REFERENCES sys_tenant(id),
    status                VARCHAR(30)   NOT NULL CHECK (status IN ('submitted','viewed','shortlisted','interview','term_sheet','rejected','accepted')) DEFAULT 'submitted',
    cover_letter          TEXT,
    feedback              TEXT,
    submitted_at          TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- ========== 机构域 ==========

CREATE TABLE biz_institution (
    id                    VARCHAR(64)   PRIMARY KEY,
    tenant_id             VARCHAR(64)   NOT NULL REFERENCES sys_tenant(id),
    name                  VARCHAR(255)  NOT NULL,
    logo                  TEXT,
    website               VARCHAR(255),
    region                VARCHAR(100),
    focus_industries      TEXT[],
    preferred_stages      TEXT[],
    min_investment        NUMERIC(12,2),
    max_investment        NUMERIC(12,2),
    total_aum             NUMERIC(12,2),
    founded_at            DATE,
    description           TEXT,
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE biz_institution_member (
    id                    VARCHAR(64)   PRIMARY KEY,
    institution_id        VARCHAR(64)   NOT NULL REFERENCES biz_institution(id),
    name                  VARCHAR(100)  NOT NULL,
    position              VARCHAR(100),
    avatar                TEXT,
    bio                   TEXT,
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE biz_portfolio_project (
    id                    VARCHAR(64)   PRIMARY KEY,
    tenant_id             VARCHAR(64)   NOT NULL REFERENCES sys_tenant(id),
    institution_id        VARCHAR(64)   NOT NULL REFERENCES biz_institution(id),
    project_id            VARCHAR(64)   NOT NULL REFERENCES biz_project(id),
    investment_amount     NUMERIC(12,2) NOT NULL,
    investment_date       DATE          NOT NULL,
    post_money_valuation  NUMERIC(12,2),
    equity_percent        NUMERIC(5,2),
    current_stage         VARCHAR(50),
    status                VARCHAR(30),
    notes                 TEXT,
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- ========== 评审域 ==========

CREATE TABLE biz_review_record (
    id                    VARCHAR(64)   PRIMARY KEY,
    tenant_id             VARCHAR(64)   NOT NULL REFERENCES sys_tenant(id),
    target_type           VARCHAR(30)   NOT NULL CHECK (target_type IN ('project','roadshow')),
    target_id             VARCHAR(64)   NOT NULL,
    reviewer_id           VARCHAR(64)   REFERENCES sys_user(id),
    decision              VARCHAR(20)   NOT NULL CHECK (decision IN ('pass','reject','pending')),
    score                 INTEGER,
    comment               TEXT,
    ai_summary            TEXT,
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- ========== 路演域 ==========

CREATE TABLE biz_roadshow (
    id                    VARCHAR(64)   PRIMARY KEY,
    tenant_id             VARCHAR(64)   NOT NULL REFERENCES sys_tenant(id),
    organizer_id          VARCHAR(64)   NOT NULL REFERENCES sys_user(id),
    title                 VARCHAR(255)  NOT NULL,
    description           TEXT,
    type                  VARCHAR(20)   NOT NULL CHECK (type IN ('online','offline')),
    is_closed_door        BOOLEAN       NOT NULL DEFAULT FALSE,
    start_time            TIMESTAMPTZ   NOT NULL,
    end_time              TIMESTAMPTZ   NOT NULL,
    location              VARCHAR(255),
    max_participants      INTEGER,
    current_participants  INTEGER       DEFAULT 0,
    cover_image           TEXT,
    status                VARCHAR(20)   NOT NULL CHECK (status IN ('draft','published','ongoing','completed','cancelled')) DEFAULT 'draft',
    agenda                JSONB,
    projects              TEXT[],
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE biz_roadshow_application (
    id                    VARCHAR(64)   PRIMARY KEY,
    tenant_id             VARCHAR(64)   NOT NULL REFERENCES sys_tenant(id),
    roadshow_id           VARCHAR(64)   NOT NULL REFERENCES biz_roadshow(id),
    project_id            VARCHAR(64)   NOT NULL REFERENCES biz_project(id),
    status                VARCHAR(20)   NOT NULL CHECK (status IN ('pending','approved','rejected')) DEFAULT 'pending',
    submitted_at          TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- ========== AI 域 ==========

CREATE TABLE sys_ai_config (
    id                    VARCHAR(64)   PRIMARY KEY,
    prompt_template       TEXT          NOT NULL,
    model                 VARCHAR(100)  NOT NULL,
    route_key             VARCHAR(100)  NOT NULL UNIQUE,
    temperature           NUMERIC(3,2)  DEFAULT 0.7,
    max_tokens            INTEGER       DEFAULT 2048,
    enabled               BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE biz_ai_chat_session (
    id                    VARCHAR(64)   PRIMARY KEY,
    tenant_id             VARCHAR(64)   NOT NULL REFERENCES sys_tenant(id),
    user_id               VARCHAR(64)   NOT NULL REFERENCES sys_user(id),
    title                 VARCHAR(255)  NOT NULL,
    messages              JSONB         NOT NULL,
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- ========== 知识库 ==========

CREATE TABLE biz_knowledge (
    id                    VARCHAR(64)   PRIMARY KEY,
    tenant_id             VARCHAR(64)   NOT NULL REFERENCES sys_tenant(id),
    title                 VARCHAR(255)  NOT NULL,
    category              VARCHAR(100),
    content               TEXT          NOT NULL,
    tags                  TEXT[],
    view_count            INTEGER       NOT NULL DEFAULT 0,
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- ========== 订单域 ==========

CREATE TABLE biz_order (
    id                    VARCHAR(64)   PRIMARY KEY,
    tenant_id             VARCHAR(64)   NOT NULL REFERENCES sys_tenant(id),
    user_id               VARCHAR(64)   NOT NULL REFERENCES sys_user(id),
    type                  VARCHAR(30)   NOT NULL CHECK (type IN ('subscription','service','extension')),
    title                 VARCHAR(255)  NOT NULL,
    amount                NUMERIC(12,2) NOT NULL,
    currency              VARCHAR(10)   DEFAULT 'CNY',
    status                VARCHAR(20)   NOT NULL CHECK (status IN ('pending','paid','cancelled','refunded')) DEFAULT 'pending',
    paid_at               TIMESTAMPTZ,
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- ========== 文件域 ==========

CREATE TABLE sys_file (
    file_id               VARCHAR(64)   PRIMARY KEY,
    original_name         VARCHAR(255)  NOT NULL,
    storage_key           VARCHAR(512)  NOT NULL,  -- MinIO object key
    file_size             BIGINT        NOT NULL,
    content_type          VARCHAR(100),
    tenant_id             VARCHAR(64)   REFERENCES sys_tenant(id),
    uploader_id           VARCHAR(64)   REFERENCES sys_user(id),
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- ========== 风控域 ==========

CREATE TABLE sys_blacklist (
    id                    VARCHAR(64)   PRIMARY KEY,
    type                  VARCHAR(30)   NOT NULL CHECK (type IN ('user','tenant','project','phone','ip')),
    target_value          VARCHAR(255)  NOT NULL,
    reason                TEXT          NOT NULL,
    operator_id           VARCHAR(64)   REFERENCES sys_user(id),
    expires_at            TIMESTAMPTZ,
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- ========== 索引 ==========

CREATE INDEX idx_user_tenant  ON sys_user(tenant_id);
CREATE INDEX idx_user_phone  ON sys_user(phone);
CREATE INDEX idx_project_owner ON biz_project(owner_id);
CREATE INDEX idx_project_status ON biz_project(status);
CREATE INDEX idx_project_tenant ON biz_project(tenant_id);
CREATE INDEX idx_app_project ON biz_project_application(project_id);
CREATE INDEX idx_app_investor ON biz_project_application(investor_tenant_id);
CREATE INDEX idx_roadshow_status ON biz_roadshow(status);
CREATE INDEX idx_roadshow_tenant ON biz_roadshow(tenant_id);
CREATE INDEX idx_order_tenant ON biz_order(tenant_id);
CREATE INDEX idx_blacklist_target ON sys_blacklist(type, target_value);
