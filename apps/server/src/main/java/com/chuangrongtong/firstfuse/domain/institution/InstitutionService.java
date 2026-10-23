package com.chuangrongtong.firstfuse.domain.institution;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 投资机构目录，对齐 mock/modules/institution.ts（2 端点，公开读）。
 */
@Service
@RequiredArgsConstructor
public class InstitutionService {

    private final NamedParameterJdbcTemplate jdbc;

    static Map<String, Object> mapInstitution(ResultSet rs) throws SQLException {
        Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("id", rs.getString("id"));
        m.put("tenantId", rs.getString("tenant_id"));
        m.put("name", rs.getString("name"));
        m.put("logo", rs.getString("logo"));
        m.put("website", rs.getString("website"));
        m.put("region", rs.getString("region"));
        m.put("focusIndustries", toList(rs.getArray("focus_industries")));
        m.put("preferredStages", toList(rs.getArray("preferred_stages")));
        m.put("minInvestment", rs.getObject("min_investment"));
        m.put("maxInvestment", rs.getObject("max_investment"));
        m.put("totalAum", rs.getObject("total_aum"));
        m.put("foundedAt", rs.getObject("founded_at") == null ? null : rs.getDate("founded_at").toString());
        m.put("description", rs.getString("description"));
        m.put("createdAt", rs.getTimestamp("created_at").toInstant());
        m.put("updatedAt", rs.getTimestamp("updated_at").toInstant());
        return m;
    }

    public static List<String> toList(Array arr) throws SQLException {
        if (arr == null) return null;
        Object[] raw = (Object[]) arr.getArray();
        return Arrays.stream(raw).map(String::valueOf).toList();
    }

    /** GET /institutions — 分页（无过滤参数，与 mock 一致） */
    public Map<String, Object> list(int pageNum, int pageSize) {
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM biz_institution", new MapSqlParameterSource(), Long.class);
        List<Map<String, Object>> rows = jdbc.query(
                "SELECT * FROM biz_institution ORDER BY created_at LIMIT :limit OFFSET :offset",
                new MapSqlParameterSource()
                        .addValue("limit", pageSize)
                        .addValue("offset", (pageNum - 1) * pageSize),
                (rs, i) -> mapInstitution(rs));
        return com.chuangrongtong.firstfuse.common.PageResult.of(rows, total == null ? 0 : total, pageNum, pageSize);
    }

    /** GET /institutions/:id */
    public Map<String, Object> getById(String id) {
        List<Map<String, Object>> rows = jdbc.query(
                "SELECT * FROM biz_institution WHERE id = :id", new MapSqlParameterSource("id", id),
                (rs, i) -> mapInstitution(rs));
        return rows.isEmpty() ? null : rows.get(0);
    }
}
