package com.chuangrongtong.firstfuse.domain.oplog;

import com.chuangrongtong.firstfuse.common.PageResult;
import com.chuangrongtong.firstfuse.security.RoleGuard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 操作日志：拦截器写入，admin 分页查询。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final NamedParameterJdbcTemplate jdbc;

    /** 写操作日志（失败仅告警，不影响主流程） */
    public void record(String userId, String role, String tenantId, String method, String path,
                       int statusCode, long costMs, String clientIp, String detail) {
        try {
            jdbc.update("""
                    INSERT INTO sys_operation_log (user_id, role, tenant_id, method, path, status_code, cost_ms, client_ip, detail)
                    VALUES (:uid, :role, :tid, :method, :path, :status, :cost, :ip, :detail)
                    """, new MapSqlParameterSource()
                    .addValue("uid", userId)
                    .addValue("role", role)
                    .addValue("tid", tenantId)
                    .addValue("method", method)
                    .addValue("path", path)
                    .addValue("status", statusCode)
                    .addValue("cost", costMs)
                    .addValue("ip", clientIp)
                    .addValue("detail", detail == null ? null
                            : detail.substring(0, Math.min(500, detail.length()))));
        } catch (Exception e) {
            log.warn("[OpLog] 写入失败：{}", e.getMessage());
        }
    }

    /** GET /admin/logs — 仅 platform_admin */
    public Map<String, Object> list(String path, String userId, int pageNum, int pageSize) {
        RoleGuard.requirePlatformAdmin();
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        MapSqlParameterSource p = new MapSqlParameterSource();
        if (path != null && !path.isBlank()) {
            where.append(" AND path LIKE :path");
            p.addValue("path", "%" + path + "%");
        }
        if (userId != null && !userId.isBlank()) {
            where.append(" AND user_id = :uid");
            p.addValue("uid", userId);
        }
        Long total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM sys_operation_log" + where, p, Long.class);
        p.addValue("limit", pageSize).addValue("offset", (pageNum - 1) * pageSize);
        List<Map<String, Object>> rows = jdbc.query(
                "SELECT * FROM sys_operation_log" + where
                        + " ORDER BY id DESC LIMIT :limit OFFSET :offset",
                p, (rs, i) -> mapRow(rs));
        return PageResult.of(rows, total == null ? 0 : total, pageNum, pageSize);
    }

    private static Map<String, Object> mapRow(ResultSet rs) throws SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", rs.getLong("id"));
        m.put("userId", rs.getString("user_id"));
        m.put("role", rs.getString("role"));
        m.put("tenantId", rs.getString("tenant_id"));
        m.put("method", rs.getString("method"));
        m.put("path", rs.getString("path"));
        m.put("statusCode", rs.getInt("status_code"));
        m.put("costMs", rs.getLong("cost_ms"));
        m.put("clientIp", rs.getString("client_ip"));
        m.put("detail", rs.getString("detail"));
        m.put("createdAt", rs.getTimestamp("created_at").toInstant().toString());
        return m;
    }
}
