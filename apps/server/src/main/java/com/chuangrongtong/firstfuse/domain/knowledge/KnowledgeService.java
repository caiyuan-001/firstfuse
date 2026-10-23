package com.chuangrongtong.firstfuse.domain.knowledge;

import com.chuangrongtong.firstfuse.common.BusinessException;
import com.chuangrongtong.firstfuse.common.ErrorCode;
import com.chuangrongtong.firstfuse.common.PageResult;
import com.chuangrongtong.firstfuse.domain.institution.InstitutionService;
import com.chuangrongtong.firstfuse.domain.support.PgArrays;
import com.chuangrongtong.firstfuse.security.RoleGuard;
import com.chuangrongtong.firstfuse.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 知识库域，对齐 mock/modules/knowledge.ts（6 端点）。
 * published 列由种子 SQL 的 ALTER TABLE ADD COLUMN IF NOT EXISTS 保证存在。
 */
@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final NamedParameterJdbcTemplate jdbc;

    private static Map<String, Object> mapRow(ResultSet rs) throws SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", rs.getString("id"));
        m.put("tenantId", rs.getString("tenant_id"));
        m.put("title", rs.getString("title"));
        m.put("category", rs.getString("category"));
        m.put("content", rs.getString("content"));
        m.put("tags", InstitutionService.toList(rs.getArray("tags")));
        m.put("viewCount", rs.getInt("view_count"));
        m.put("published", rs.getBoolean("published"));
        m.put("createdAt", rs.getTimestamp("created_at").toInstant());
        m.put("updatedAt", rs.getTimestamp("updated_at").toInstant());
        return m;
    }

    /** GET /knowledge */
    public Map<String, Object> list(String keyword, String category, int pageNum, int pageSize) {
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        org.springframework.jdbc.core.namedparam.MapSqlParameterSource p =
                new org.springframework.jdbc.core.namedparam.MapSqlParameterSource();
        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND title LIKE :kw");
            p.addValue("kw", "%" + keyword + "%");
        }
        if (category != null && !category.isBlank()) {
            where.append(" AND category = :cat");
            p.addValue("cat", category);
        }
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM biz_knowledge" + where, p, Long.class);
        p.addValue("limit", pageSize).addValue("offset", (pageNum - 1) * pageSize);
        List<Map<String, Object>> rows = jdbc.query(
                "SELECT * FROM biz_knowledge" + where + " ORDER BY created_at DESC LIMIT :limit OFFSET :offset",
                p, (rs, i) -> mapRow(rs));
        return PageResult.of(rows, total == null ? 0 : total, pageNum, pageSize);
    }

    /** GET /knowledge/:id — 缺省 {}（mock 语义） */
    public Map<String, Object> getById(String id) {
        List<Map<String, Object>> rows = jdbc.query("SELECT * FROM biz_knowledge WHERE id = :id",
                new org.springframework.jdbc.core.namedparam.MapSqlParameterSource("id", id),
                (rs, i) -> mapRow(rs));
        return rows.isEmpty() ? new LinkedHashMap<>() : rows.get(0);
    }

    /** POST /knowledge → {id} */
    public Map<String, Object> create(Map<String, Object> body) {
        String title = (String) body.get("title");
        if (title == null || title.isBlank()) {
            throw new BusinessException(ErrorCode.PARAM_VALIDATION, "标题必填");
        }
        String id = "kn-" + UUID.randomUUID().toString().substring(0, 8);
        final String insert = """
                INSERT INTO biz_knowledge (id, tenant_id, title, category, content, tags, published)
                VALUES (?, ?, ?, ?, ?, ?::text[], TRUE)
                """;
        jdbc.getJdbcTemplate().execute((Connection con) -> {
            try (PreparedStatement ps = con.prepareStatement(insert)) {
                ps.setString(1, id);
                ps.setString(2, TenantContext.getTenantId());
                ps.setString(3, title);
                ps.setString(4, str(body.get("category")));
                ps.setString(5, strOr(body.get("content"), ""));
                PgArrays.bind(ps, 6, con, body.get("tags"));
                ps.executeUpdate();
            }
            return null;
        });
        return Map.of("id", id);
    }

    /** PUT /knowledge/:id → {success:true} */
    public Map<String, Object> update(String id, Map<String, Object> body) {
        Map<String, Object> item = find(id);
        RoleGuard.requireSameTenant((String) item.get("tenantId"));
        jdbc.getJdbcTemplate().execute((Connection con) -> {
            try (PreparedStatement ps = con.prepareStatement("""
                    UPDATE biz_knowledge SET title = ?, category = ?, content = ?, tags = ?::text[], updated_at = now()
                    WHERE id = ?
                    """)) {
                ps.setString(1, strOr(body.get("title"), (String) item.get("title")));
                ps.setString(2, str(body.get("category")));
                ps.setString(3, strOr(body.get("content"), (String) item.get("content")));
                PgArrays.bind(ps, 4, con, body.get("tags"));
                ps.setString(5, id);
                ps.executeUpdate();
            }
            return null;
        });
        return Map.of("success", true);
    }

    /** PATCH /knowledge/:id/publish → {success:true} */
    public Map<String, Object> publish(String id, Map<String, Object> body) {
        Map<String, Object> item = find(id);
        RoleGuard.requireSameTenant((String) item.get("tenantId"));
        boolean target = body != null && body.get("published") != null
                ? Boolean.TRUE.equals(body.get("published"))
                : !Boolean.TRUE.equals(item.get("published"));
        jdbc.getJdbcTemplate().update(
                "UPDATE biz_knowledge SET published = ?, updated_at = now() WHERE id = ?", target, id);
        return Map.of("success", true);
    }

    /** DELETE /knowledge/:id → {success:true} */
    public Map<String, Object> delete(String id) {
        Map<String, Object> item = find(id);
        if (!RoleGuard.isPlatformAdmin()) {
            RoleGuard.requireSameTenant((String) item.get("tenantId"));
        }
        jdbc.getJdbcTemplate().update("DELETE FROM biz_knowledge WHERE id = ?", id);
        return Map.of("success", true);
    }

    private Map<String, Object> find(String id) {
        List<Map<String, Object>> rows = jdbc.query("SELECT * FROM biz_knowledge WHERE id = :id",
                new org.springframework.jdbc.core.namedparam.MapSqlParameterSource("id", id),
                (rs, i) -> mapRow(rs));
        if (rows.isEmpty()) throw new BusinessException(ErrorCode.NOT_FOUND, "知识条目不存在");
        return rows.get(0);
    }

    private static String str(Object v) { return v == null ? null : String.valueOf(v); }

    private static String strOr(Object v, String def) {
        return v == null || String.valueOf(v).isBlank() ? def : String.valueOf(v);
    }
}
