package com.chuangrongtong.firstfuse.domain.project;

import com.chuangrongtong.firstfuse.common.ApiResponse;
import com.chuangrongtong.firstfuse.common.BusinessException;
import com.chuangrongtong.firstfuse.common.ErrorCode;
import com.chuangrongtong.firstfuse.common.PageResult;
import com.chuangrongtong.firstfuse.security.RoleGuard;
import com.chuangrongtong.firstfuse.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 平台项目审核端点，对齐 mock/modules/project.ts 的 admin 审核部分：
 *   GET /admin/projects/reviews       审核队列（status tab：pending/pass/reject/all）
 *   PUT /admin/projects/reviews/:id   审核 decision: pass/reject + reason
 */
@RestController
@RequestMapping("/admin/projects/reviews")
@RequiredArgsConstructor
public class ProjectAdminController {

    private final NamedParameterJdbcTemplate jdbc;

    private static final Map<String, String> INDUSTRY_CN = Map.of(
            "ai", "人工智能", "new_energy", "新能源", "biotech", "医疗健康",
            "semiconductor", "半导体", "robotics", "机器人", "enterprise_software", "企业软件");

    private static final Map<String, String> STAGE_CN = Map.of(
            "angel", "天使轮", "pre_seed", "Pre-Seed", "seed", "种子轮", "series_a", "A轮",
            "series_b", "B轮", "series_c", "C轮", "pre_ipo", "Pre-IPO", "strategic", "战略投资");

    /** GET /admin/projects/reviews — 宽 DTO 对齐 mock（industry/stage 中文标签 + AI 预审占位字段） */
    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "50") int pageSize) {
        RoleGuard.requirePlatformAdmin();
        String mapped = switch (status) {
            case "pending" -> "pending_review";
            case "pass" -> "review_pass";
            case "reject" -> "review_reject";
            case "all" -> null;
            default -> status;
        };
        StringBuilder where = new StringBuilder();
        MapSqlParameterSource p = new MapSqlParameterSource();
        if (mapped != null) {
            where.append(" WHERE p.status = :status");
            p.addValue("status", mapped);
        }
        Long total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM biz_project p" + where, p, Long.class);
        List<Map<String, Object>> rows = jdbc.query("""
                SELECT p.id, p.title, p.industry, p.stage, p.amount_sought, p.region,
                       p.valuation, p.equity_offered, p.founded_at, p.status,
                       p.short_description, p.long_description, p.updated_at, u.nickname AS owner_name
                FROM biz_project p LEFT JOIN sys_user u ON u.id = p.owner_id
                """ + where + " ORDER BY p.updated_at DESC LIMIT :limit OFFSET :offset",
                p.addValue("limit", pageSize).addValue("offset", (pageNum - 1) * pageSize),
                (rs, i) -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    String id = rs.getString("id");
                    m.put("id", id);
                    m.put("title", rs.getString("title"));
                    m.put("industry", INDUSTRY_CN.getOrDefault(rs.getString("industry"), rs.getString("industry")));
                    m.put("stageLabel", STAGE_CN.getOrDefault(rs.getString("stage"), rs.getString("stage")));
                    m.put("amount", rs.getDouble("amount_sought"));
                    m.put("ownerName", rs.getString("owner_name"));
                    m.put("submitTime", rs.getTimestamp("updated_at") == null ? null
                            : rs.getTimestamp("updated_at").toInstant().toString());
                    m.put("shortDesc", rs.getString("short_description"));
                    m.put("longDesc", rs.getString("long_description"));
                    m.put("region", rs.getString("region"));
                    m.put("valuation", rs.getObject("valuation") == null ? null : rs.getDouble("valuation"));
                    m.put("equityOffered", rs.getObject("equity_offered") == null ? null : rs.getDouble("equity_offered"));
                    m.put("foundedAt", rs.getDate("founded_at") == null ? null : rs.getDate("founded_at").toString());
                    // AI 预审占位：demo 按 id 稳定伪值（M3 接真实模型后替换）
                    int aiScore = 55 + Math.floorMod(id.hashCode(), 40);
                    m.put("aiScore", aiScore);
                    m.put("aiComment", aiScore >= 75 ? "项目结构完整，建议通过。"
                            : aiScore >= 60 ? "部分信息待补充，建议人工复核。" : "信息不完整，建议退回补充。");
                    m.put("aiSuspicious", aiScore < 60);
                    m.put("status", rs.getString("status"));
                    return m;
                });
        return ApiResponse.success(PageResult.of(rows, total == null ? 0 : total, pageNum, pageSize));
    }

    /** PUT /admin/projects/reviews/:id — 审核决策 + 写 biz_review_record */
    @PutMapping("/{id}")
    public ApiResponse<Map<String, Object>> review(
            @PathVariable String id, @RequestBody Map<String, Object> body) {
        RoleGuard.requirePlatformAdmin();
        String decision = String.valueOf(body.get("decision"));
        if (!List.of("pass", "reject").contains(decision)) {
            throw new BusinessException(ErrorCode.PARAM_VALIDATION, "decision 必须是 pass/reject");
        }
        String newStatus = "pass".equals(decision) ? "review_pass" : "review_reject";
        int n = jdbc.getJdbcTemplate().update(
                "UPDATE biz_project SET status = ?, updated_at = now() WHERE id = ?", newStatus, id);
        if (n == 0) throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);

        String reason = body.get("reason") == null ? null : String.valueOf(body.get("reason"));
        jdbc.getJdbcTemplate().update("""
                INSERT INTO biz_review_record (id, tenant_id, target_type, target_id, reviewer_id, decision, comment)
                VALUES (?, ?, 'project', ?, ?, ?, ?)
                """, "rvr-" + UUID.randomUUID().toString().substring(0, 8),
                TenantContext.getTenantId(), id, TenantContext.getUserId(), decision, reason);
        return ApiResponse.success(Map.of("success", true));
    }
}
