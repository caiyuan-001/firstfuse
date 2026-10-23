package com.chuangrongtong.firstfuse.domain.graph;

import com.chuangrongtong.firstfuse.domain.institution.InstitutionService;
import com.chuangrongtong.firstfuse.security.RoleGuard;
import com.chuangrongtong.firstfuse.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 关系图谱（机构端 RelationGraphView 供数）。
 * 数据结构对齐 shared GraphData：{nodes:[{id,type,name,properties}],edges:[{id,source,target,type,properties}]}
 *
 * 数据来源：
 * - nebula.enabled=true 且 space/Tag 已建：优先读 NebulaGraph
 * - 否则从 PostgreSQL 业务表派生（institution / project / 成员 / 投资 / 申请）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GraphService {

    private final NamedParameterJdbcTemplate jdbc;
    private final NebulaGraphClient nebula;

    public Map<String, Object> graph(String centerId, int depth) {
        Map<String, Object> data = nebula.enabled() ? fromNebula() : null;
        if (data == null) data = postgresGraph();

        if (centerId == null || centerId.isBlank()) return data;
        return neighborhood(data, centerId, Math.max(1, Math.min(3, depth)));
    }

    // ========== PostgreSQL 派生 ==========

    @SuppressWarnings("unchecked")
    Map<String, Object> postgresGraph() {
        boolean admin = RoleGuard.isPlatformAdmin();
        String tid = TenantContext.getTenantId();
        String instScope = admin ? "" : " WHERE i.tenant_id = :tid";
        String projScope = admin ? "" : " WHERE (p.status = 'published' OR p.tenant_id = :tid)";
        MapSqlParameterSource params = new MapSqlParameterSource("tid", tid);

        Map<String, Map<String, Object>> nodes = new LinkedHashMap<>();

        // 机构
        for (Map<String, Object> row : jdbc.query("""
                SELECT i.id, i.tenant_id, i.name, i.region, i.focus_industries, i.preferred_stages, i.total_aum
                FROM biz_institution i""" + instScope, params, (rs, i) -> mapInstitution(rs))) {
            nodes.put((String) row.get("id"), node(row.get("id"), "institution", row.get("name"), props(
                    "tenantId", row.get("tenantId"),
                    "region", row.get("region"),
                    "focusIndustries", row.get("focusIndustries"),
                    "preferredStages", row.get("preferredStages"),
                    "totalAum", row.get("totalAum"))));
        }

        // 项目
        for (Map<String, Object> row : jdbc.query("""
                SELECT p.id, p.tenant_id, p.title, p.industry, p.stage, p.amount_sought, p.status
                FROM biz_project p""" + projScope + " ORDER BY p.view_count DESC LIMIT 500",
                params, (rs, i) -> mapProject(rs))) {
            nodes.put((String) row.get("id"), node(row.get("id"), "project", row.get("title"), props(
                    "tenantId", row.get("tenantId"),
                    "industry", row.get("industry"),
                    "stage", row.get("stage"),
                    "amountSought", row.get("amountSought"),
                    "status", row.get("status"))));
        }

        // 机构成员
        for (Map<String, Object> row : jdbc.query("""
                SELECT m.id, m.name, m.position, m.institution_id, i.tenant_id
                FROM biz_institution_member m JOIN biz_institution i ON i.id = m.institution_id""" + instScope,
                params, (rs, i) -> mapMember(rs))) {
            nodes.put((String) row.get("id"), node(row.get("id"), "person", row.get("name"),
                    props("tenantId", row.get("tenantId"), "position", row.get("position"))));
        }

        Map<String, Map<String, Object>> edges = new LinkedHashMap<>();

        // 投资关系：机构 → 项目
        for (Map<String, Object> row : jdbc.query("""
                SELECT pf.id, pf.institution_id, pf.project_id, pf.investment_amount, pf.investment_date
                FROM biz_portfolio_project pf JOIN biz_institution i ON i.id = pf.institution_id""" + instScope,
                params, (rs, i) -> mapPortfolio(rs))) {
            addEdge(edges, nodes, "edge-inv-" + row.get("id"), row.get("institutionId"),
                    row.get("projectId"), "invested_in",
                    props("amount", row.get("amount"), "investmentDate", row.get("investmentDate")));
        }

        // 任职关系：人物 → 机构
        for (Map<String, Object> row : jdbc.query("""
                SELECT m.id, m.institution_id FROM biz_institution_member m
                JOIN biz_institution i ON i.id = m.institution_id""" + instScope,
                params, (rs, i) -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", rs.getString("id"));
                    m.put("institutionId", rs.getString("institution_id"));
                    return m;
                })) {
            addEdge(edges, nodes, "edge-wa-" + row.get("id"), row.get("id"),
                    row.get("institutionId"), "works_at", Map.of());
        }

        // 申请关系：机构所在租户 → 项目
        for (Map<String, Object> row : jdbc.query("""
                SELECT a.id, a.project_id, a.investor_tenant_id, a.status, i.id AS institution_id
                FROM biz_project_application a
                JOIN biz_institution i ON i.tenant_id = a.investor_tenant_id
                JOIN biz_project p ON p.id = a.project_id""" + projScope,
                params, (rs, i) -> mapApplication(rs))) {
            addEdge(edges, nodes, "edge-ap-" + row.get("id"), row.get("institutionId"),
                    row.get("projectId"), "applies", props("status", row.get("status")));
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("nodes", new ArrayList<>(nodes.values()));
        out.put("edges", new ArrayList<>(edges.values()));
        return out;
    }

    private static void addEdge(Map<String, Map<String, Object>> edges,
                                Map<String, Map<String, Object>> nodes,
                                String id, Object source, Object target, String type,
                                Map<String, Object> properties) {
        if (source == null || target == null) return;
        if (!nodes.containsKey(source) || !nodes.containsKey(target)) return;
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("id", id);
        e.put("source", source);
        e.put("target", target);
        e.put("type", type);
        e.put("properties", properties);
        edges.put(id, e);
    }

    private static Map<String, Object> node(Object id, String type, Object name, Map<String, Object> properties) {
        Map<String, Object> n = new LinkedHashMap<>();
        n.put("id", id);
        n.put("type", type);
        n.put("name", name);
        n.put("properties", properties);
        return n;
    }

    private static Map<String, Object> props(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) m.put(String.valueOf(kv[i]), kv[i + 1]);
        return m;
    }

    private static Map<String, Object> mapInstitution(ResultSet rs) throws SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", rs.getString("id"));
        m.put("tenantId", rs.getString("tenant_id"));
        m.put("name", rs.getString("name"));
        m.put("region", rs.getString("region"));
        m.put("focusIndustries", InstitutionService.toList(rs.getArray("focus_industries")));
        m.put("preferredStages", InstitutionService.toList(rs.getArray("preferred_stages")));
        m.put("totalAum", (Number) rs.getObject("total_aum") == null ? null : rs.getDouble("total_aum"));
        return m;
    }

    private static Map<String, Object> mapProject(ResultSet rs) throws SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", rs.getString("id"));
        m.put("tenantId", rs.getString("tenant_id"));
        m.put("title", rs.getString("title"));
        m.put("industry", rs.getString("industry"));
        m.put("stage", rs.getString("stage"));
        m.put("amountSought", rs.getObject("amount_sought") == null ? null : rs.getDouble("amount_sought"));
        m.put("status", rs.getString("status"));
        return m;
    }

    private static Map<String, Object> mapMember(ResultSet rs) throws SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", rs.getString("id"));
        m.put("tenantId", rs.getString("tenant_id"));
        m.put("name", rs.getString("name"));
        m.put("position", rs.getString("position"));
        m.put("institutionId", rs.getString("institution_id"));
        return m;
    }

    private static Map<String, Object> mapPortfolio(ResultSet rs) throws SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", rs.getString("id"));
        m.put("institutionId", rs.getString("institution_id"));
        m.put("projectId", rs.getString("project_id"));
        m.put("amount", rs.getObject("investment_amount") == null ? null : rs.getDouble("investment_amount"));
        m.put("investmentDate", rs.getDate("investment_date") == null ? null : rs.getDate("investment_date").toString());
        return m;
    }

    private static Map<String, Object> mapApplication(ResultSet rs) throws SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", rs.getString("id"));
        m.put("projectId", rs.getString("project_id"));
        m.put("institutionId", rs.getString("institution_id"));
        m.put("status", rs.getString("status"));
        return m;
    }

    // ========== NebulaGraph 读取 ==========

    private Map<String, Object> fromNebula() {
        String tid = TenantContext.getTenantId();
        boolean admin = RoleGuard.isPlatformAdmin();
        String instWhere = admin ? "" : " WHERE v.institution.tenant_id == '" + tid + "'";
        String projWhere = admin ? "" : " WHERE v.project.tenant_id == '" + tid + "' OR v.project.status == 'published'";
        String personWhere = admin ? "" : " WHERE v.person.tenant_id == '" + tid + "'";

        List<Map<String, Object>> instRows = nebula.query(
                "MATCH (v:institution)" + instWhere + " RETURN id(v) AS id, v.institution.name AS name, "
                        + "v.institution.region AS region LIMIT 1000");
        List<Map<String, Object>> projRows = nebula.query(
                "MATCH (v:project)" + projWhere + " RETURN id(v) AS id, v.project.title AS name, "
                        + "v.project.industry AS industry, v.project.stage AS stage LIMIT 1000");
        List<Map<String, Object>> personRows = nebula.query(
                "MATCH (v:person)" + personWhere + " RETURN id(v) AS id, v.person.name AS name, "
                        + "v.person.position AS position LIMIT 1000");
        if (instRows == null || projRows == null || personRows == null) {
            log.info("[图谱] Nebula 不可用，回退 PostgreSQL 派生");
            return null;
        }

        Map<String, Map<String, Object>> nodes = new LinkedHashMap<>();
        for (Map<String, Object> r : instRows) {
            nodes.put(str(r.get("id")), node(r.get("id"), "institution", r.get("name"),
                    props("region", r.get("region"))));
        }
        for (Map<String, Object> r : projRows) {
            nodes.put(str(r.get("id")), node(r.get("id"), "project", r.get("name"),
                    props("industry", r.get("industry"), "stage", r.get("stage"))));
        }
        for (Map<String, Object> r : personRows) {
            nodes.put(str(r.get("id")), node(r.get("id"), "person", r.get("name"),
                    props("position", r.get("position"))));
        }

        Map<String, Map<String, Object>> edges = new LinkedHashMap<>();
        addNebulaEdges(edges, nodes, nebula.query(
                "MATCH (a)-[e:invested_in]->(b) RETURN id(a) AS source, id(b) AS target, "
                        + "e.invested_in.amount AS amount LIMIT 2000"), "invested_in", "amount");
        addNebulaEdges(edges, nodes, nebula.query(
                "MATCH (a)-[e:works_at]->(b) RETURN id(a) AS source, id(b) AS target LIMIT 2000"),
                "works_at", null);
        addNebulaEdges(edges, nodes, nebula.query(
                "MATCH (a)-[e:applies]->(b) RETURN id(a) AS source, id(b) AS target, "
                        + "e.applies.status AS status LIMIT 2000"), "applies", "status");

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("nodes", new ArrayList<>(nodes.values()));
        out.put("edges", new ArrayList<>(edges.values()));
        return out;
    }

    private static void addNebulaEdges(Map<String, Map<String, Object>> edges,
                                       Map<String, Map<String, Object>> nodes,
                                       List<Map<String, Object>> rows,
                                       String type, String propKey) {
        if (rows == null) return;
        for (Map<String, Object> r : rows) {
            String source = str(r.get("source"));
            String target = str(r.get("target"));
            if (!nodes.containsKey(source) || !nodes.containsKey(target)) continue;
            String id = "edge-" + type + "-" + source + "-" + target;
            Map<String, Object> edge = new LinkedHashMap<>();
            edge.put("id", id);
            edge.put("source", source);
            edge.put("target", target);
            edge.put("type", type);
            edge.put("properties", propKey == null ? Map.of() : props(propKey, r.get(propKey)));
            edges.put(id, edge);
        }
    }

    // ========== 中心节点邻域 ==========

    @SuppressWarnings("unchecked")
    private Map<String, Object> neighborhood(Map<String, Object> data, String centerId, int depth) {
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) data.get("nodes");
        List<Map<String, Object>> edges = (List<Map<String, Object>>) data.get("edges");
        Map<String, Map<String, Object>> nodeById = new HashMap<>();
        for (Map<String, Object> n : nodes) nodeById.put((String) n.get("id"), n);

        Set<String> visited = new LinkedHashSet<>();
        if (!nodeById.containsKey(centerId)) return data;
        visited.add(centerId);

        Deque<String> queue = new ArrayDeque<>();
        queue.add(centerId);
        Set<String> keptEdgeIds = new LinkedHashSet<>();
        Map<String, Integer> levels = new HashMap<>();
        levels.put(centerId, 0);

        while (!queue.isEmpty()) {
            String cur = queue.poll();
            int level = levels.getOrDefault(cur, depth);
            if (level >= depth) continue;
            for (Map<String, Object> e : edges) {
                String s = (String) e.get("source");
                String t = (String) e.get("target");
                if (!cur.equals(s) && !cur.equals(t)) continue;
                String other = cur.equals(s) ? t : s;
                keptEdgeIds.add((String) e.get("id"));
                visited.add(other);
                if (!levels.containsKey(other)) {
                    levels.put(other, level + 1);
                    queue.add(other);
                }
            }
        }

        List<Map<String, Object>> outNodes = new ArrayList<>();
        for (String id : visited) {
            Map<String, Object> n = nodeById.get(id);
            if (n != null) outNodes.add(n);
        }
        List<Map<String, Object>> outEdges = new ArrayList<>();
        for (Map<String, Object> e : edges) {
            if (keptEdgeIds.contains((String) e.get("id"))) outEdges.add(e);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("nodes", outNodes);
        out.put("edges", outEdges);
        return out;
    }

    private static String str(Object v) {
        return v == null ? null : String.valueOf(v);
    }
}
