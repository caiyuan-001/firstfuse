package com.chuangrongtong.firstfuse.domain.order;

import com.chuangrongtong.firstfuse.common.PageResult;
import com.chuangrongtong.firstfuse.security.RoleGuard;
import com.chuangrongtong.firstfuse.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单域，对齐 mock/modules/order.ts（2 端点）。
 * 出参为宽 DTO：orderNo（=id）、tenantName（JOIN sys_tenant）。
 * platform_admin 全量；其他角色仅本租户。
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final NamedParameterJdbcTemplate jdbc;

    private static Map<String, Object> mapRow(ResultSet rs) throws SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        String id = rs.getString("id");
        m.put("id", id);
        m.put("orderNo", id);
        m.put("tenantId", rs.getString("tenant_id"));
        m.put("tenantName", rs.getString("tenant_name"));
        m.put("type", rs.getString("type"));
        m.put("title", rs.getString("title"));
        m.put("amount", rs.getDouble("amount"));
        m.put("currency", rs.getString("currency"));
        m.put("status", rs.getString("status"));
        m.put("createdAt", rs.getTimestamp("created_at").toInstant());
        m.put("paidAt", rs.getTimestamp("paid_at") == null ? "" : rs.getTimestamp("paid_at").toInstant().toString());
        return m;
    }

    private static final String SELECT_SQL = """
            SELECT o.*, t.name AS tenant_name FROM biz_order o JOIN sys_tenant t ON t.id = o.tenant_id
            """;

    /** GET /orders */
    public Map<String, Object> list(String orderNo, String tenant, String status, int pageNum, int pageSize) {
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        MapSqlParameterSource p = new MapSqlParameterSource();
        if (orderNo != null && !orderNo.isBlank()) {
            where.append(" AND o.id LIKE :orderNo");
            p.addValue("orderNo", "%" + orderNo + "%");
        }
        if (tenant != null && !tenant.isBlank()) {
            where.append(" AND t.name LIKE :tenant");
            p.addValue("tenant", "%" + tenant + "%");
        }
        if (status != null && !status.isBlank()) {
            where.append(" AND o.status = :status");
            p.addValue("status", status);
        }
        if (!RoleGuard.isPlatformAdmin()) {
            where.append(" AND o.tenant_id = :tid");
            p.addValue("tid", TenantContext.getTenantId());
        }
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM biz_order o JOIN sys_tenant t ON t.id = o.tenant_id" + where, p, Long.class);
        p.addValue("limit", pageSize).addValue("offset", (pageNum - 1) * pageSize);
        List<Map<String, Object>> rows = jdbc.query(
                SELECT_SQL + where + " ORDER BY o.created_at DESC LIMIT :limit OFFSET :offset",
                p, (rs, i) -> mapRow(rs));
        return PageResult.of(rows, total == null ? 0 : total, pageNum, pageSize);
    }

    /** GET /orders/:id — 缺省 {}（mock 语义），:id 即 orderNo */
    public Map<String, Object> getById(String id) {
        MapSqlParameterSource p = new MapSqlParameterSource("id", id);
        if (!RoleGuard.isPlatformAdmin()) {
            p.addValue("tid", TenantContext.getTenantId());
        }
        List<Map<String, Object>> rows = jdbc.query(
                SELECT_SQL + " WHERE o.id = :id" + (RoleGuard.isPlatformAdmin() ? "" : " AND o.tenant_id = :tid"),
                p, (rs, i) -> mapRow(rs));
        return rows.isEmpty() ? new LinkedHashMap<>() : rows.get(0);
    }

    /** GET /admin/orders/export — 全量导出（仅 platform_admin，最多 5000 行） */
    public List<Map<String, Object>> exportRows(String status, String tenant) {
        RoleGuard.requirePlatformAdmin();
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        MapSqlParameterSource p = new MapSqlParameterSource();
        if (status != null && !status.isBlank()) {
            where.append(" AND o.status = :status");
            p.addValue("status", status);
        }
        if (tenant != null && !tenant.isBlank()) {
            where.append(" AND t.name LIKE :tenant");
            p.addValue("tenant", "%" + tenant + "%");
        }
        return jdbc.query(SELECT_SQL + where + " ORDER BY o.created_at DESC LIMIT 5000",
                p, (rs, i) -> mapRow(rs));
    }
}
