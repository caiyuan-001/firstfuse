package com.chuangrongtong.firstfuse.domain.roadshow;

import com.chuangrongtong.firstfuse.common.BusinessException;
import com.chuangrongtong.firstfuse.common.ErrorCode;
import com.chuangrongtong.firstfuse.common.PageResult;
import com.chuangrongtong.firstfuse.domain.institution.InstitutionService;
import com.chuangrongtong.firstfuse.domain.support.PgArrays;
import com.chuangrongtong.firstfuse.security.RoleGuard;
import com.chuangrongtong.firstfuse.security.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 路演域，对齐 mock/modules/roadshow.ts（5 端点）。
 * 出参含冗余展示字段：organizer(租户名)、organizerId、projectCount、participants、statusLabel、isOfficial。
 */
@Service
@RequiredArgsConstructor
public class RoadshowService {

    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    private static final Map<String, String> STATUS_CN = Map.of(
            "draft", "草稿", "published", "已发布", "ongoing", "进行中",
            "completed", "已结束", "cancelled", "已取消");

    private static Map<String, Object> mapRow(ResultSet rs, ObjectMapper om) throws SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", rs.getString("id"));
        m.put("tenantId", rs.getString("tenant_id"));
        m.put("organizerId", rs.getString("organizer_id"));
        m.put("organizer", rs.getString("organizer"));
        m.put("title", rs.getString("title"));
        m.put("description", rs.getString("description"));
        m.put("type", rs.getString("type"));
        m.put("isClosedDoor", rs.getBoolean("is_closed_door"));
        m.put("startTime", ts(rs, "start_time"));
        m.put("endTime", ts(rs, "end_time"));
        m.put("location", rs.getString("location"));
        m.put("maxParticipants", rs.getObject("max_participants"));
        m.put("participants", rs.getInt("current_participants"));
        m.put("coverImage", rs.getString("cover_image"));
        String status = rs.getString("status");
        m.put("status", status);
        m.put("statusLabel", STATUS_CN.getOrDefault(status, status));
        m.put("projectCount", rs.getObject("project_count"));
        m.put("isOfficial", "t-platform".equals(rs.getString("tenant_id")));
        String agenda = rs.getString("agenda");
        if (agenda != null) {
            try { m.put("agenda", om.readValue(agenda, List.class)); } catch (Exception e) { m.put("agenda", null); }
        } else {
            m.put("agenda", null);
        }
        m.put("projects", InstitutionService.toList(rs.getArray("projects")));
        m.put("createdAt", ts(rs, "created_at"));
        m.put("updatedAt", ts(rs, "updated_at"));
        return m;
    }

    private static Object ts(ResultSet rs, String col) throws SQLException {
        Timestamp t = rs.getTimestamp(col);
        return t == null ? null : t.toInstant();
    }

    private static final String SELECT_SQL = """
            SELECT r.*, t.name AS organizer,
                   COALESCE(array_length(r.projects, 1), 0) AS project_count
            FROM biz_roadshow r JOIN sys_tenant t ON t.id = r.tenant_id
            """;

    /** GET /roadshows */
    public Map<String, Object> list(String keyword, String organizer, String status, int pageNum, int pageSize) {
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        org.springframework.jdbc.core.namedparam.MapSqlParameterSource p =
                new org.springframework.jdbc.core.namedparam.MapSqlParameterSource();
        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND r.title LIKE :kw");
            p.addValue("kw", "%" + keyword + "%");
        }
        if (organizer != null && !organizer.isBlank()) {
            where.append(" AND t.name LIKE :org");
            p.addValue("org", "%" + organizer + "%");
        }
        if (status != null && !status.isBlank()) {
            where.append(" AND r.status = :status");
            p.addValue("status", status);
        }
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM biz_roadshow r JOIN sys_tenant t ON t.id = r.tenant_id" + where, p, Long.class);
        p.addValue("limit", pageSize).addValue("offset", (pageNum - 1) * pageSize);
        List<Map<String, Object>> rows = jdbc.query(SELECT_SQL + where + " ORDER BY r.start_time",
                p, (rs, i) -> mapRow(rs, objectMapper));
        return PageResult.of(rows, total == null ? 0 : total, pageNum, pageSize);
    }

    /** GET /roadshows/:id — 缺省返回 {}（mock 语义） */
    public Map<String, Object> getById(String id) {
        List<Map<String, Object>> rows = jdbc.query(SELECT_SQL + " WHERE r.id = :id",
                new org.springframework.jdbc.core.namedparam.MapSqlParameterSource("id", id),
                (rs, i) -> mapRow(rs, objectMapper));
        return rows.isEmpty() ? new LinkedHashMap<>() : rows.get(0);
    }

    /** POST /roadshows → {id}，默认 draft */
    public Map<String, Object> create(Map<String, Object> body) {
        String title = (String) body.get("title");
        if (title == null || title.isBlank()) {
            throw new BusinessException(ErrorCode.PARAM_VALIDATION, "路演标题必填");
        }
        if (strOr(body.get("startTime"), null) == null || strOr(body.get("endTime"), null) == null) {
            throw new BusinessException(ErrorCode.PARAM_VALIDATION, "startTime/endTime 必填");
        }
        String id = "r-" + UUID.randomUUID().toString().substring(0, 8);
        final String insert = """
                INSERT INTO biz_roadshow (id, tenant_id, organizer_id, title, description, type, is_closed_door,
                    start_time, end_time, location, max_participants, cover_image, status, agenda, projects)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?::timestamptz, ?::timestamptz, ?, ?, ?, 'draft', ?::jsonb, ?::text[])
                """;
        jdbc.getJdbcTemplate().execute((Connection con) -> {
            try (PreparedStatement ps = con.prepareStatement(insert)) {
                int i = 1;
                ps.setString(i++, id);
                ps.setString(i++, TenantContext.getTenantId());
                ps.setString(i++, TenantContext.getUserId());
                ps.setString(i++, title);
                ps.setString(i++, str(body.get("description")));
                ps.setString(i++, strOr(body.get("type"), "online"));
                ps.setBoolean(i++, Boolean.TRUE.equals(body.get("isClosedDoor")));
                ps.setString(i++, dtOr(body.get("startTime")));
                ps.setString(i++, dtOr(body.get("endTime")));
                ps.setString(i++, str(body.get("location")));
                setNullableInt(ps, i++, body.get("maxParticipants"));
                ps.setString(i++, str(body.get("coverImage")));
                ps.setString(i++, agendaJson(body.get("agenda")));
                PgArrays.bind(ps, i++, con, body.get("projects"));
                ps.executeUpdate();
            }
            return null;
        });
        return Map.of("id", id);
    }

    /** PUT /roadshows/:id → {success:true}（全字段覆盖） */
    public Map<String, Object> update(String id, Map<String, Object> body) {
        Map<String, Object> item = jdbc.query(SELECT_SQL + " WHERE r.id = :id",
                new org.springframework.jdbc.core.namedparam.MapSqlParameterSource("id", id),
                (rs, i) -> mapRow(rs, objectMapper)).stream().findFirst().orElse(null);
        if (item == null) throw new BusinessException(ErrorCode.NOT_FOUND, "路演不存在");
        RoleGuard.requireSameTenant((String) item.get("tenantId"));

        final String update = """
                UPDATE biz_roadshow SET
                    title = ?, description = ?, type = ?, is_closed_door = ?,
                    start_time = ?::timestamptz, end_time = ?::timestamptz, location = ?,
                    max_participants = ?, status = ?, agenda = ?::jsonb, projects = ?::text[], updated_at = now()
                WHERE id = ?
                """;
        // startTime/endTime 为 NOT NULL 列：body 未携带时回退原值
        String newStart = strOr(body.get("startTime"), String.valueOf(item.get("startTime")));
        String newEnd   = strOr(body.get("endTime"),   String.valueOf(item.get("endTime")));
        jdbc.getJdbcTemplate().execute((Connection con) -> {
            try (PreparedStatement ps = con.prepareStatement(update)) {
                int i = 1;
                ps.setString(i++, strOr(body.get("title"), (String) item.get("title")));
                ps.setString(i++, str(body.get("description")));
                ps.setString(i++, strOr(body.get("type"), (String) item.get("type")));
                Object closed = body.get("isClosedDoor");
                ps.setBoolean(i++, closed != null ? Boolean.TRUE.equals(closed) : Boolean.TRUE.equals(item.get("isClosedDoor")));
                ps.setString(i++, newStart.replace("T", " "));
                ps.setString(i++, newEnd.replace("T", " "));
                ps.setString(i++, str(body.get("location")));
                setNullableInt(ps, i++, body.get("maxParticipants"));
                ps.setString(i++, strOr(body.get("status"), (String) item.get("status")));
                ps.setString(i++, agendaJson(body.get("agenda")));
                PgArrays.bind(ps, i++, con, body.get("projects"));
                ps.setString(i++, id);
                ps.executeUpdate();
            }
            return null;
        });
        return Map.of("success", true);
    }

    /** POST /roadshows/:id/apply — 写报名记录（pending） */
    public Map<String, Object> apply(String roadshowId, Map<String, Object> body) {
        String projectId = str(body == null ? null : body.get("projectId"));
        if (projectId == null || projectId.isBlank()) {
            List<String> own = jdbc.getJdbcTemplate().queryForList(
                    "SELECT id FROM biz_project WHERE owner_id = ? LIMIT 1", String.class, TenantContext.getUserId());
            if (own.isEmpty()) throw new BusinessException(ErrorCode.PARAM_VALIDATION, "无可报名的项目，请先创建项目");
            projectId = own.get(0);
        }
        jdbc.getJdbcTemplate().update("""
                INSERT INTO biz_roadshow_application (id, tenant_id, roadshow_id, project_id, status)
                VALUES (?, ?, ?, ?, 'pending')
                """, "rsa-" + UUID.randomUUID().toString().substring(0, 8),
                TenantContext.getTenantId(), roadshowId, projectId);
        return Map.of("success", true);
    }

    // ========== 工具 ==========

    private String agendaJson(Object agenda) {
        if (agenda == null) return null;
        try { return objectMapper.writeValueAsString(agenda); }
        catch (Exception e) { return null; }
    }

    private static String str(Object v) { return v == null ? null : String.valueOf(v); }

    private static String strOr(Object v, String def) {
        return v == null || String.valueOf(v).isBlank() ? def : String.valueOf(v);
    }

    private static String dtOr(Object v) {
        String s = str(v);
        return s == null || s.isBlank() ? null : s.replace("T", " ");
    }

    private static void setNullableInt(PreparedStatement ps, int i, Object v) throws SQLException {
        if (v == null || String.valueOf(v).isBlank()) ps.setNull(i, Types.INTEGER);
        else ps.setInt(i, v instanceof Number n ? n.intValue() : Integer.parseInt(String.valueOf(v)));
    }
}
