package com.chuangrongtong.firstfuse.domain.graph;

import com.chuangrongtong.firstfuse.common.BusinessException;
import com.chuangrongtong.firstfuse.common.ErrorCode;
import com.chuangrongtong.firstfuse.security.RoleGuard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * NebulaGraph 一次性初始化与数据同步：
 * 1) CREATE SPACE firstfuse
 * 2) 等待空间心跳可用
 * 3) CREATE TAG/EDGE（幂等）
 * 4) 将 PostgreSQL 业务图谱全量同步为 Nebula 顶点与边
 *
 * 仅 platform_admin 可调用，入口：POST /admin/graph/init
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GraphSyncService {

    private final GraphService graphService;
    private final NebulaGraphClient nebula;

    private static final String[] DDL = {
            "CREATE TAG IF NOT EXISTS institution (tenant_id string, name string, region string)",
            "CREATE TAG IF NOT EXISTS project (tenant_id string, title string, industry string, stage string, status string)",
            "CREATE TAG IF NOT EXISTS person (tenant_id string, name string, position string)",
            "CREATE EDGE IF NOT EXISTS invested_in (amount double, investment_date string)",
            "CREATE EDGE IF NOT EXISTS works_at (position string)",
            "CREATE EDGE IF NOT EXISTS applies (status string)"
    };

    // Nebula 3.3+ 正确索引语法（必须带 TAG/EDGE 关键字 + 索引名）
    // CREATE TAG INDEX [IF NOT EXISTS] idx_name ON tag_name(prop)
    // CREATE EDGE INDEX [IF NOT EXISTS] idx_name ON edge_name(prop)
    // 注意：string 类型属性建索引需指定前缀长度，如 prop(64)
    private static final String[] INDEX_DDL = {
            "CREATE TAG INDEX IF NOT EXISTS idx_inst_tenant ON institution(tenant_id(64))",
            "CREATE TAG INDEX IF NOT EXISTS idx_proj_tenant ON project(tenant_id(64))",
            "CREATE TAG INDEX IF NOT EXISTS idx_proj_status ON project(status(32))",
            "CREATE TAG INDEX IF NOT EXISTS idx_person_tenant ON person(tenant_id(64))",
            "CREATE EDGE INDEX IF NOT EXISTS idx_works_position ON works_at(position(128))"
    };

    public Map<String, Object> init() {
        RoleGuard.requirePlatformAdmin();
        if (!nebula.enabled()) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                    "Nebula 未启用：请在 .env 设置 NEBULA_ENABLED=true 并重启服务");
        }

        List<String> steps = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // 0. 先看集群主机：CREATE SPACE 要求 ONLINE 存储节点数 >= replica_factor
        List<Map<String, Object>> hosts = nebula.executeRaw("SHOW HOSTS");
        List<Map<String, Object>> hostList = hosts == null ? List.of() : hosts;
        List<Map<String, Object>> metaHosts = nebula.executeRaw("SHOW HOSTS META");
        List<Map<String, Object>> spaces = nebula.executeRaw("SHOW SPACES");
        steps.add("SHOW HOSTS(存储) " + hostList.size() + " 行，SHOW HOSTS META "
                + (metaHosts == null ? -1 : metaHosts.size()) + " 行，SHOW SPACES "
                + (spaces == null ? -1 : spaces.size()) + " 行");
        if (hostList.isEmpty()) {
            errors.add("SHOW HOSTS 为空：Nebula 集群无可识别存储主机（storaged 未注册或离线），无法建空间");
        }

        // 1. 建空间（已存在则幂等返回）
        if (nebula.executeRaw("CREATE SPACE IF NOT EXISTS firstfuse "
                + "(partition_num = 10, replica_factor = 1, vid_type = FIXED_STRING(64))") == null) {
            errors.add("CREATE SPACE firstfuse 失败");
        } else {
            steps.add("CREATE SPACE firstfuse");
        }

        // 2. 等待空间可用（首次建空间需等心跳，最长约 30s）
        boolean usable = false;
        for (int i = 0; i < 15; i++) {
            if (nebula.query("SHOW TAGS") != null) {
                usable = true;
                break;
            }
            sleep(2000);
        }
        if (!usable) errors.add("空间 firstfuse 暂不可用（心跳未完成，可稍后重试）");
        else steps.add("SPACE 可用");

        // 3. 建 Tag / Edge
        int ddlOk = 0;
        for (String s : DDL) {
            if (nebula.query(s) == null) errors.add("DDL 失败: " + s);
            else ddlOk++;
        }
        steps.add("TAG/EDGE 成功 " + ddlOk + "/" + DDL.length);

        // 3b. 建索引（Nebula 对属性/边查询需显式索引，IF NOT EXISTS 幂等）
        int idxOk = 0;
        for (String s : INDEX_DDL) {
            if (nebula.query(s) == null) errors.add("索引 DDL 失败: " + s);
            else idxOk++;
        }
        steps.add("INDEX 成功 " + idxOk + "/" + INDEX_DDL.length);

        // 4. 同步 PG 图谱数据
        int nodeOk = 0, edgeOk = 0;
        Map<String, Object> pgGraph = graphService.postgresGraph();
        List<Map<String, Object>> nodes = castList(pgGraph.get("nodes"));
        List<Map<String, Object>> edges = castList(pgGraph.get("edges"));
        for (Map<String, Object> n : nodes) {
            String stmt = vertexStatement(n);
            if (stmt == null) continue;
            if (nebula.query(stmt) == null) errors.add("顶点同步失败: " + n.get("id"));
            else nodeOk++;
        }
        for (Map<String, Object> e : edges) {
            String stmt = edgeStatement(e);
            if (stmt == null) continue;
            if (nebula.query(stmt) == null) errors.add("边同步失败: " + e.get("id"));
            else edgeOk++;
        }
        steps.add("同步顶点 " + nodeOk + "/" + nodes.size() + "，边 " + edgeOk + "/" + edges.size());

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("space", "firstfuse");
        out.put("nodesSynced", nodeOk);
        out.put("edgesSynced", edgeOk);
        out.put("hosts", hostList);
        out.put("metaHosts", metaHosts);
        out.put("spaces", spaces);
        out.put("steps", steps);
        out.put("errors", errors);
        return out;
    }

    // ========== nGQL 拼装 ==========

    @SuppressWarnings("unchecked")
    private static String vertexStatement(Map<String, Object> node) {
        String id = str(node.get("id"));
        String type = str(node.get("type"));
        Map<String, Object> p = (Map<String, Object>) node.getOrDefault("properties", Map.of());
        return switch (type) {
            case "institution" -> "INSERT VERTEX institution(tenant_id, name, region) VALUES " + q(id)
                    + ":(" + q(p.get("tenantId")) + "," + q(node.get("name")) + "," + q(p.get("region")) + ")";
            case "project" -> "INSERT VERTEX project(tenant_id, title, industry, stage, status) VALUES " + q(id)
                    + ":(" + q(p.get("tenantId")) + "," + q(node.get("name")) + ","
                    + q(p.get("industry")) + "," + q(p.get("stage")) + "," + q(p.get("status")) + ")";
            case "person" -> "INSERT VERTEX person(tenant_id, name, position) VALUES " + q(id)
                    + ":(" + q(p.get("tenantId")) + "," + q(node.get("name")) + "," + q(p.get("position")) + ")";
            default -> null;
        };
    }

    @SuppressWarnings("unchecked")
    private static String edgeStatement(Map<String, Object> edge) {
        String source = q(edge.get("source"));
        String target = q(edge.get("target"));
        String type = str(edge.get("type"));
        Map<String, Object> p = (Map<String, Object>) edge.getOrDefault("properties", Map.of());
        return switch (type) {
            case "invested_in" -> "INSERT EDGE invested_in(amount, investment_date) VALUES "
                    + source + "->" + target + ":(" + numeric(p.get("amount")) + "," + q(p.get("investmentDate")) + ")";
            case "works_at" -> "INSERT EDGE works_at(position) VALUES "
                    + source + "->" + target + ":(" + q(p.get("position")) + ")";
            case "applies" -> "INSERT EDGE applies(status) VALUES "
                    + source + "->" + target + ":(" + q(p.get("status")) + ")";
            default -> null;
        };
    }

    private static String q(Object v) {
        if (v == null) return "''";
        String s = String.valueOf(v).replace("\\", "\\\\").replace("'", "\\'");
        return "'" + s + "'";
    }

    private static String numeric(Object v) {
        return v instanceof Number n ? String.valueOf(n.doubleValue()) : "0.0";
    }

    private static String str(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> castList(Object v) {
        return v instanceof List<?> list ? (List<Map<String, Object>>) list : List.of();
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
