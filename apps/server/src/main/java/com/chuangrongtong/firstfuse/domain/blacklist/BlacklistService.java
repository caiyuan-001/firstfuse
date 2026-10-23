package com.chuangrongtong.firstfuse.domain.blacklist;

import com.chuangrongtong.firstfuse.common.BusinessException;
import com.chuangrongtong.firstfuse.common.ErrorCode;
import com.chuangrongtong.firstfuse.common.PageResult;
import com.chuangrongtong.firstfuse.security.RoleGuard;
import com.chuangrongtong.firstfuse.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 风控黑名单（管理后台），对齐 mock/modules/blacklist.ts。
 * mock 出参含 operatorName → JOIN sys_user。
 */
@Service
@RequiredArgsConstructor
public class BlacklistService {

    private final NamedParameterJdbcTemplate jdbc;

    private static final List<String> TYPES = List.of("user", "tenant", "project", "phone", "ip");

    private static Map<String, Object> mapRow(ResultSet rs) throws SQLException {
        Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("id", rs.getString("id"));
        m.put("type", rs.getString("type"));
        m.put("targetValue", rs.getString("target_value"));
        m.put("reason", rs.getString("reason"));
        m.put("operatorId", rs.getString("operator_id"));
        m.put("operatorName", rs.getString("operator_name"));
        m.put("createdAt", rs.getTimestamp("created_at").toInstant());
        m.put("expiresAt", rs.getTimestamp("expires_at") == null ? "" : rs.getTimestamp("expires_at").toInstant().toString());
        return m;
    }

    /** GET /blacklist — 平台管理员专用 */
    public Map<String, Object> list(String keyword, String type, int pageNum, int pageSize) {
        RoleGuard.requirePlatformAdmin();
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        MapSqlParameterSource p = new MapSqlParameterSource();
        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND (b.target_value LIKE :kw OR b.reason LIKE :kw)");
            p.addValue("kw", "%" + keyword + "%");
        }
        if (type != null && !type.isBlank()) {
            where.append(" AND b.type = :type");
            p.addValue("type", type);
        }
        Long total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM sys_blacklist b" + where, p, Long.class);
        List<Map<String, Object>> rows = jdbc.query("""
                SELECT b.*, u.nickname AS operator_name
                FROM sys_blacklist b LEFT JOIN sys_user u ON u.id = b.operator_id
                """ + where + " ORDER BY b.created_at DESC LIMIT :limit OFFSET :offset",
                p.addValue("limit", pageSize).addValue("offset", (pageNum - 1) * pageSize),
                (rs, i) -> mapRow(rs));
        return PageResult.of(rows, total == null ? 0 : total, pageNum, pageSize);
    }

    /** POST /blacklist — 平台管理员加入黑名单，出参 {id} */
    public Map<String, Object> create(Map<String, Object> body) {
        RoleGuard.requirePlatformAdmin();
        String type = String.valueOf(body.get("type"));
        if (!TYPES.contains(type)) {
            throw new BusinessException(ErrorCode.PARAM_VALIDATION, "type 必须是 user/tenant/project/phone/ip");
        }
        String id = "bl-" + UUID.randomUUID().toString().substring(0, 8);
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("type", type)
                .addValue("targetValue", String.valueOf(body.get("targetValue")))
                .addValue("reason", String.valueOf(body.get("reason")))
                .addValue("operatorId", TenantContext.getUserId())
                .addValue("expiresAt", body.get("expiresAt") == null || String.valueOf(body.get("expiresAt")).isBlank()
                        ? null : String.valueOf(body.get("expiresAt")));
        jdbc.update("""
                INSERT INTO sys_blacklist (id, type, target_value, reason, operator_id, expires_at)
                VALUES (:id, :type, :targetValue, :reason, :operatorId, :expiresAt)
                """, p);
        return Map.of("id", id);
    }

    /** DELETE /blacklist/:id — 平台管理员移除 */
    public Map<String, Object> delete(String id) {
        RoleGuard.requirePlatformAdmin();
        jdbc.update("DELETE FROM sys_blacklist WHERE id = :id", new MapSqlParameterSource("id", id));
        return Map.of("success", true);
    }
}
