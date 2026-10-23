package com.chuangrongtong.firstfuse.domain.project;

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
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 项目域，对齐 mock/modules/project.ts：
 *   GET  /projects            列表（公开市场：published 全平台可见 + 本租户全部；平台管理员全部）
 *   GET  /projects/:id        详情
 *   POST /projects            创建（draft）→ {id}
 *   PUT  /projects/:id        更新 → {success:true}
 *   POST /projects/:id/submit 提交审核 draft→pending_review → {success:true}
 *   POST /projects/diagnosis  AI 诊断（demo 固定结果）
 */
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final NamedParameterJdbcTemplate jdbc;

    private static Map<String, Object> mapProject(ResultSet rs) throws SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", rs.getString("id"));
        m.put("tenantId", rs.getString("tenant_id"));
        m.put("ownerId", rs.getString("owner_id"));
        m.put("title", rs.getString("title"));
        m.put("subtitle", rs.getString("subtitle"));
        m.put("industry", rs.getString("industry"));
        m.put("subIndustry", rs.getString("sub_industry"));
        m.put("region", rs.getString("region"));
        m.put("stage", rs.getString("stage"));
        m.put("amountSought", rs.getDouble("amount_sought"));
        m.put("valuation", (Number) rs.getObject("valuation") == null ? null : rs.getDouble("valuation"));
        m.put("equityOffered", (Number) rs.getObject("equity_offered") == null ? null : rs.getDouble("equity_offered"));
        m.put("shortDescription", rs.getString("short_description"));
        m.put("longDescription", rs.getString("long_description"));
        m.put("businessModel", rs.getString("business_model"));
        m.put("competitiveAdvantage", rs.getString("competitive_advantage"));
        m.put("marketSize", rs.getString("market_size"));
        m.put("revenue", (Number) rs.getObject("revenue") == null ? null : rs.getDouble("revenue"));
        m.put("profit", (Number) rs.getObject("profit") == null ? null : rs.getDouble("profit"));
        m.put("teamSize", rs.getObject("team_size"));
        m.put("foundedAt", rs.getDate("founded_at") == null ? null : rs.getDate("founded_at").toString());
        m.put("website", rs.getString("website"));
        m.put("bpFileId", rs.getString("bp_file_id"));
        m.put("status", rs.getString("status"));
        m.put("viewCount", rs.getInt("view_count"));
        m.put("favoriteCount", rs.getInt("favorite_count"));
        m.put("tags", InstitutionService.toList(rs.getArray("tags")));
        m.put("createdAt", ts(rs, "created_at"));
        m.put("updatedAt", ts(rs, "updated_at"));
        return m;
    }

    private static Object ts(ResultSet rs, String col) throws SQLException {
        Timestamp t = rs.getTimestamp(col);
        return t == null ? null : t.toInstant();
    }

    /** GET /projects */
    public Map<String, Object> list(int pageNum, int pageSize) {
        boolean admin = RoleGuard.isPlatformAdmin();
        String where = admin ? "" : " WHERE status = 'published' OR tenant_id = :tid";
        String countSql = "SELECT COUNT(*) FROM biz_project" + where;
        org.springframework.jdbc.core.namedparam.MapSqlParameterSource p =
                new org.springframework.jdbc.core.namedparam.MapSqlParameterSource();
        if (!admin) p.addValue("tid", TenantContext.getTenantId());
        Long total = jdbc.queryForObject(countSql, p, Long.class);
        p.addValue("limit", pageSize).addValue("offset", (pageNum - 1) * pageSize);
        List<Map<String, Object>> rows = jdbc.query(
                "SELECT * FROM biz_project" + where + " ORDER BY created_at DESC LIMIT :limit OFFSET :offset",
                p, (rs, i) -> mapProject(rs));
        return PageResult.of(rows, total == null ? 0 : total, pageNum, pageSize);
    }

    /** GET /projects/:id */
    public Map<String, Object> getById(String id) {
        Map<String, Object> proj = queryById(id);
        if (proj == null) return null;
        if (!RoleGuard.isPlatformAdmin()) {
            boolean published = "published".equals(proj.get("status"));
            boolean sameTenant = TenantContext.getTenantId().equals(proj.get("tenantId"));
            if (!published && !sameTenant) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "无权查看该项目");
            }
        }
        return proj;
    }

    private Map<String, Object> queryById(String id) {
        List<Map<String, Object>> rows = jdbc.query(
                "SELECT * FROM biz_project WHERE id = :id",
                new org.springframework.jdbc.core.namedparam.MapSqlParameterSource("id", id),
                (rs, i) -> mapProject(rs));
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** POST /projects → {id} */
    public Map<String, Object> create(Map<String, Object> body) {
        String id = "p-" + UUID.randomUUID().toString().substring(0, 8);
        String title = (String) body.get("title");
        if (title == null || title.isBlank()) {
            throw new BusinessException(ErrorCode.PARAM_VALIDATION, "项目标题必填");
        }
        final String insert = """
                INSERT INTO biz_project (id, tenant_id, owner_id, title, subtitle, industry, sub_industry, region, stage,
                    amount_sought, valuation, equity_offered, short_description, long_description, business_model,
                    competitive_advantage, market_size, revenue, profit, team_size, founded_at, website, tags, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::date, ?, ?::text[], 'draft')
                """;
        jdbc.getJdbcTemplate().execute((Connection con) -> {
            try (PreparedStatement ps = con.prepareStatement(insert)) {
                int i = 1;
                ps.setString(i++, id);
                ps.setString(i++, TenantContext.getTenantId());
                ps.setString(i++, TenantContext.getUserId());
                ps.setString(i++, title);
                ps.setString(i++, str(body.get("subtitle")));
                ps.setString(i++, strOr(body.get("industry"), "other"));
                ps.setString(i++, str(body.get("subIndustry")));
                ps.setString(i++, strOr(body.get("region"), "未填写"));
                ps.setString(i++, strOr(body.get("stage"), "seed"));
                ps.setDouble(i++, numOr(body.get("amountSought"), 0));
                setNullableDouble(ps, i++, body.get("valuation"));
                setNullableDouble(ps, i++, body.get("equityOffered"));
                ps.setString(i++, str(body.get("shortDescription")));
                ps.setString(i++, str(body.get("longDescription")));
                ps.setString(i++, str(body.get("businessModel")));
                ps.setString(i++, str(body.get("competitiveAdvantage")));
                ps.setString(i++, str(body.get("marketSize")));
                setNullableDouble(ps, i++, body.get("revenue"));
                setNullableDouble(ps, i++, body.get("profit"));
                setNullableInt(ps, i++, body.get("teamSize"));
                setNullableDate(ps, i++, body.get("foundedAt"));
                ps.setString(i++, str(body.get("website")));
                PgArrays.bind(ps, i++, con, body.get("tags"));
                ps.executeUpdate();
            }
            return null;
        });
        return Map.of("id", id);
    }

    /** PUT /projects/:id → {success:true}（全字段覆盖，前端表单总是提交完整对象） */
    public Map<String, Object> update(String id, Map<String, Object> body) {
        Map<String, Object> proj = queryById(id);
        if (proj == null) throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        RoleGuard.requireSameTenant((String) proj.get("tenantId"));
        final String update = """
                UPDATE biz_project SET
                    title = ?, subtitle = ?, industry = ?, sub_industry = ?, region = ?, stage = ?,
                    amount_sought = ?, valuation = ?, equity_offered = ?, short_description = ?, long_description = ?,
                    business_model = ?, competitive_advantage = ?, market_size = ?, revenue = ?, profit = ?,
                    team_size = ?, founded_at = ?::date, website = ?, tags = ?::text[], updated_at = now()
                WHERE id = ?
                """;
        jdbc.getJdbcTemplate().execute((Connection con) -> {
            try (PreparedStatement ps = con.prepareStatement(update)) {
                int i = 1;
                ps.setString(i++, strOr(body.get("title"), (String) proj.get("title")));
                ps.setString(i++, str(body.get("subtitle")));
                ps.setString(i++, strOr(body.get("industry"), (String) proj.get("industry")));
                ps.setString(i++, str(body.get("subIndustry")));
                ps.setString(i++, strOr(body.get("region"), (String) proj.get("region")));
                ps.setString(i++, strOr(body.get("stage"), (String) proj.get("stage")));
                ps.setDouble(i++, numOr(body.get("amountSought"), toD(proj.get("amountSought"))));
                setNullableDouble(ps, i++, body.get("valuation"));
                setNullableDouble(ps, i++, body.get("equityOffered"));
                ps.setString(i++, str(body.get("shortDescription")));
                ps.setString(i++, str(body.get("longDescription")));
                ps.setString(i++, str(body.get("businessModel")));
                ps.setString(i++, str(body.get("competitiveAdvantage")));
                ps.setString(i++, str(body.get("marketSize")));
                setNullableDouble(ps, i++, body.get("revenue"));
                setNullableDouble(ps, i++, body.get("profit"));
                setNullableInt(ps, i++, body.get("teamSize"));
                setNullableDate(ps, i++, body.get("foundedAt"));
                ps.setString(i++, str(body.get("website")));
                PgArrays.bind(ps, i++, con, body.get("tags"));
                ps.setString(i++, id);
                ps.executeUpdate();
            }
            return null;
        });
        return Map.of("success", true);
    }

    /** POST /projects/:id/submit — 状态机 draft → pending_review */
    public Map<String, Object> submit(String id) {
        Map<String, Object> proj = queryById(id);
        if (proj == null) throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        RoleGuard.requireSameTenant((String) proj.get("tenantId"));
        if (!"draft".equals(proj.get("status"))) {
            throw new BusinessException(ErrorCode.PROJECT_STATUS_INVALID,
                    "仅草稿状态可提交审核，当前状态：" + proj.get("status"));
        }
        jdbc.getJdbcTemplate().update(
                "UPDATE biz_project SET status = 'pending_review', updated_at = now() WHERE id = ?", id);
        return Map.of("success", true);
    }

    /** POST /projects/diagnosis — demo 固定结果（M3 由 AiController 接模型网关替换） */
    public Map<String, Object> diagnosis() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("overallScore", 78);
        m.put("dimensions", List.of(
                Map.of("name", "商业模式", "score", 82, "fullMark", 100, "description", "清晰的 SaaS 订阅模式"),
                Map.of("name", "团队实力", "score", 75, "fullMark", 100, "description", "核心团队有行业经验"),
                Map.of("name", "市场空间", "score", 85, "fullMark", 100, "description", "市场规模大"),
                Map.of("name", "财务状况", "score", 68, "fullMark", 100, "description", "增长良好"),
                Map.of("name", "技术壁垒", "score", 80, "fullMark", 100, "description", "核心算法有一定壁垒")));
        return m;
    }

    // ========== 类型工具 ==========

    private static String str(Object v) { return v == null ? null : String.valueOf(v); }

    private static String strOr(Object v, String def) {
        return v == null || String.valueOf(v).isBlank() ? def : String.valueOf(v);
    }

    private static double numOr(Object v, double def) {
        if (v == null) return def;
        return v instanceof Number n ? n.doubleValue() : Double.parseDouble(String.valueOf(v));
    }

    private static Double toD(Object v) {
        return v instanceof Number n ? n.doubleValue() : null;
    }

    private static void setNullableDouble(PreparedStatement ps, int i, Object v) throws SQLException {
        if (v == null || String.valueOf(v).isBlank()) ps.setNull(i, java.sql.Types.NUMERIC);
        else ps.setDouble(i, numOr(v, 0));
    }

    private static void setNullableInt(PreparedStatement ps, int i, Object v) throws SQLException {
        if (v == null || String.valueOf(v).isBlank()) ps.setNull(i, java.sql.Types.INTEGER);
        else ps.setInt(i, (int) numOr(v, 0));
    }

    private static void setNullableDate(PreparedStatement ps, int i, Object v) throws SQLException {
        String s = str(v);
        if (s == null || s.isBlank()) { ps.setNull(i, java.sql.Types.DATE); return; }
        try {
            ps.setDate(i, java.sql.Date.valueOf(s.length() > 10 ? s.substring(0, 10) : s));
        } catch (IllegalArgumentException e) {
            // '2025-06' 这种 yyyy-MM 格式补 -01
            if (s.matches("\\d{4}-\\d{2}")) ps.setDate(i, java.sql.Date.valueOf(s + "-01"));
            else ps.setNull(i, java.sql.Types.DATE);
        }
    }
}
