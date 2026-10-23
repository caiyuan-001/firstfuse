-- ============================================================
-- M4 运营与加固：文件记录 + 操作日志
-- 幂等：IF NOT EXISTS，可随 spring.sql.init.mode=always 重复执行
-- ============================================================

-- 文件元数据（MinIO 对象与业务侧的映射）
-- 注意：库中存在早期原型遗留的同名异构表（file_id 为主键），演示环境直接重建以保证结构确定
DROP TABLE IF EXISTS sys_file;
CREATE TABLE sys_file (
    id           VARCHAR(64) PRIMARY KEY,
    tenant_id    VARCHAR(64) NOT NULL,
    uploader_id  VARCHAR(64),
    file_name    VARCHAR(255) NOT NULL,
    content_type VARCHAR(128),
    size_bytes   BIGINT       NOT NULL DEFAULT 0,
    bucket       VARCHAR(64)  NOT NULL,
    object_key   VARCHAR(512) NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_sys_file_tenant ON sys_file (tenant_id, created_at DESC);

-- 操作日志（拦截器记录所有写操作 + 下载）
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id          BIGSERIAL PRIMARY KEY,
    user_id     VARCHAR(64),
    role        VARCHAR(32),
    tenant_id   VARCHAR(64),
    method      VARCHAR(8)   NOT NULL,
    path        VARCHAR(255) NOT NULL,
    status_code INT,
    cost_ms     BIGINT,
    client_ip   VARCHAR(64),
    detail      VARCHAR(512),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_oplog_created ON sys_operation_log (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_oplog_tenant ON sys_operation_log (tenant_id, created_at DESC);
