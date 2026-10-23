package com.chuangrongtong.firstfuse.domain.tenant;

import com.chuangrongtong.firstfuse.common.BusinessException;
import com.chuangrongtong.firstfuse.common.ErrorCode;
import com.chuangrongtong.firstfuse.common.PageResult;
import com.chuangrongtong.firstfuse.security.RoleGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 租户管理（管理后台），对齐 mock/modules/tenant.ts。
 */
@Service
@RequiredArgsConstructor
public class TenantService {

    private final NamedParameterJdbcTemplate jdbc;

    private static final RowMapper<Map<String, Object>> MAPPER = (rs, i) -> mapTenant(rs);

    private static Map<String, Object> mapTenant(ResultSet rs) throws SQLException {
        Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("id", rs.getString("id"));
        m.put("name", rs.getString("name"));
        m.put("type", rs.getString("type"));
        m.put("status", rs.getString("status"));
        m.put("contactName", rs.getString("contact_name"));
        m.put("contactPhone", rs.getString("contact_phone"));
        m.put("contactEmail", rs.getString("contact_email"));
        m.put("industry", rs.getString("industry"));
        m.put("subscriptionPlan", rs.getString("subscription_plan"));
        m.put("expireAt", toInstant(rs.getTimestamp("expire_at")));
        m.put("maxUsers", rs.getObject("max_users"));
        m.put("createdAt", toInstant(rs.getTimestamp("created_at")));
        m.put("updatedAt", toInstant(rs.getTimestamp("updated_at")));
        return m;
    }

    private static Object toInstant(Timestamp ts) {
        return ts == null ? null : ts.toInstant();
    }

    private static Object toInstant(OffsetDateTime odt) {
        return odt == null ? null : odt.toInstant();
    }

    /** GET /tenants — 平台管理员专用 */
    public Map<String, Object> list(String keyword, String type, String status, int pageNum, int pageSize) {
        RoleGuard.requirePlatformAdmin();
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        MapSqlParameterSource p = new MapSqlParameterSource();
        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND name LIKE :kw");
            p.addValue("kw", "%" + keyword + "%");
        }
        if (type != null && !type.isBlank()) {
            where.append(" AND type = :type");
            p.addValue("type", type);
        }
        if (status != null && !status.isBlank()) {
            where.append(" AND status = :status");
            p.addValue("status", status);
        }
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM sys_tenant" + where, p, Long.class);
        List<Map<String, Object>> rows = jdbc.query(
                "SELECT * FROM sys_tenant" + where + " ORDER BY created_at LIMIT :limit OFFSET :offset",
                p.addValue("limit", pageSize).addValue("offset", (pageNum - 1) * pageSize),
                MAPPER);
        return PageResult.of(rows, total == null ? 0 : total, pageNum, pageSize);
    }

    /** GET /tenants/:id — 平台管理员或本租户 */
    public Map<String, Object> getById(String id) {
        if (!RoleGuard.isPlatformAdmin()) {
            RoleGuard.requireSameTenant(id);
        }
        List<Map<String, Object>> rows = jdbc.query(
                "SELECT * FROM sys_tenant WHERE id = :id", new MapSqlParameterSource("id", id), MAPPER);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** POST /tenants — 平台管理员开通新租户 */
    public Map<String, Object> create(Map<String, Object> body) {
        RoleGuard.requirePlatformAdmin();
        String id = "t-" + UUID.randomUUID().toString().substring(0, 8);
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("name", str(body, "name", "未命名租户"))
                .addValue("type", str(body, "type", "institution"))
                .addValue("status", str(body, "status", "trial"))
                .addValue("contactName", str(body, "contactName", ""))
                .addValue("contactPhone", str(body, "contactPhone", ""))
                .addValue("contactEmail", str(body, "contactEmail", null))
                .addValue("industry", str(body, "industry", null))
                .addValue("subscriptionPlan", str(body, "subscriptionPlan", null))
                .addValue("expireAt", str(body, "expireAt", null))
                .addValue("maxUsers", body.get("maxUsers"));
        jdbc.update("""
                INSERT INTO sys_tenant (id, name, type, status, contact_name, contact_phone, contact_email,
                                        industry, subscription_plan, expire_at, max_users)
                VALUES (:id, :name, :type, :status, :contactName, :contactPhone, :contactEmail,
                        :industry, :subscriptionPlan, :expireAt, :maxUsers)
                """, p);
        return getById(id);
    }

    /** PUT /tenants/:id/status — 平台管理员变更租户状态 */
    public Map<String, Object> updateStatus(String id, String status) {
        RoleGuard.requirePlatformAdmin();
        if (status == null || !List.of("active", "frozen", "trial").contains(status)) {
            throw new BusinessException(ErrorCode.PARAM_VALIDATION, "status 必须是 active/frozen/trial");
        }
        int n = jdbc.update(
                "UPDATE sys_tenant SET status = :status, updated_at = now() WHERE id = :id",
                new MapSqlParameterSource().addValue("status", status).addValue("id", id));
        if (n == 0) throw new BusinessException(ErrorCode.TENANT_NOT_FOUND);
        return Map.of("success", true);
    }

    private static String str(Map<String, Object> body, String key, String def) {
        Object v = body.get(key);
        return v == null ? def : String.valueOf(v);
    }
}
