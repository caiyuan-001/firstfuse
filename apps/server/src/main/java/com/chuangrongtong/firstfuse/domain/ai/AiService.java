package com.chuangrongtong.firstfuse.domain.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * AI 业务端点，对齐 mock/modules/ai.ts：
 *   POST /ai/project-filter → {results:[{projectId,matchScore,dimensions}]}
 *   POST /ai/review-brief    → {id,projectId,overallScore,sections,overallAssessment,riskPoints,highlights}
 *   POST /ai/chat            → {reply,sessionId}
 *
 * 已配置真实模型网关（ai.base-url + ai.api-key）时走网关；否则走本地确定性演示生成，
 * 保证开发环境无外部依赖也能返回结构完整的数据。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final NamedParameterJdbcTemplate jdbc;
    private final AiGateway gateway;

    // ========== 项目筛选 ==========

    public Map<String, Object> projectFilter(Map<String, Object> body) {
        String wantIndustry = str(body.get("industry"));
        String wantStage = str(body.get("stage"));
        Double wantAmount = num(body.get("amount"));
        int limit = Math.max(1, Math.min(20, intOr(body.get("limit"), 5)));

        List<Map<String, Object>> projects = jdbc.query("""
                SELECT id, title, industry, stage, amount_sought, short_description, team_size
                FROM biz_project WHERE status = 'published'
                ORDER BY view_count DESC, created_at DESC LIMIT 100
                """, new MapSqlParameterSource(), (rs, i) -> mapProjectBrief(rs));

        List<Map<String, Object>> results = new ArrayList<>();
        for (Map<String, Object> p : projects) {
            int industryScore = industryScore(wantIndustry, str(p.get("industry")));
            int stageScore = stageScore(wantStage, str(p.get("stage")));
            int amountScore = amountScore(wantAmount, num(p.get("amountSought")));
            int matchScore = (int) Math.round(industryScore * 0.45 + stageScore * 0.30 + amountScore * 0.25);

            Map<String, Object> dimensions = new LinkedHashMap<>();
            dimensions.put("行业匹配", industryScore);
            dimensions.put("阶段匹配", stageScore);
            dimensions.put("金额匹配", amountScore);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("projectId", p.get("id"));
            item.put("title", p.get("title"));
            item.put("matchScore", Math.max(0, Math.min(100, matchScore)));
            item.put("dimensions", dimensions);
            results.add(item);
        }
        results.sort(Comparator.comparingInt((Map<String, Object> m) -> -((Integer) m.get("matchScore"))));
        if (results.size() > limit) results = new ArrayList<>(results.subList(0, limit));
        return Map.of("results", results);
    }

    private static int industryScore(String want, String actual) {
        if (want == null || want.isBlank()) return 70;
        if (actual == null) return 50;
        if (want.equals(actual)) return 95;
        if (want.contains(actual) || actual.contains(want)) return 82;
        return 55;
    }

    private static int stageScore(String want, String actual) {
        if (want == null || want.isBlank()) return 70;
        if (actual == null) return 50;
        if (want.equals(actual)) return 100;
        // 相邻轮次（早期/中期/后期）给部分分
        if (earlyStage(want) && earlyStage(actual)) return 85;
        if (lateStage(want) && lateStage(actual)) return 85;
        return 60;
    }

    private static boolean earlyStage(String s) {
        return "angel".equals(s) || "pre_seed".equals(s) || "seed".equals(s);
    }

    private static boolean lateStage(String s) {
        return "series_c".equals(s) || "pre_ipo".equals(s) || "strategic".equals(s);
    }

    private static int amountScore(Double want, Double actual) {
        if (want == null || want <= 0 || actual == null || actual <= 0) return 75;
        double ratio = actual / want;
        if (ratio >= 0.5 && ratio <= 2.0) return 90;
        if (ratio >= 0.25 && ratio <= 4.0) return 75;
        return 50;
    }

    private static Map<String, Object> mapProjectBrief(ResultSet rs) throws SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", rs.getString("id"));
        m.put("title", rs.getString("title"));
        m.put("industry", rs.getString("industry"));
        m.put("stage", rs.getString("stage"));
        m.put("amountSought", rs.getObject("amount_sought") == null ? null : rs.getDouble("amount_sought"));
        m.put("shortDescription", rs.getString("short_description"));
        m.put("teamSize", rs.getObject("team_size"));
        return m;
    }

    // ========== 评审简报 ==========

    public Map<String, Object> reviewBrief(Map<String, Object> body) {
        String projectId = str(body.get("projectId"));
        Map<String, Object> project = projectId == null ? null : findProject(projectId);
        if (project == null) return demoReviewBrief(projectId);

        int teamScore = score(62 + intOr(project.get("teamSize"), 0) * 2, 95);
        int techScore = score(70 + (hasText(project.get("competitiveAdvantage")) ? 12 : 0), 92);
        int marketScore = score(68 + (hasText(project.get("marketSize")) ? 12 : 0), 90);
        int financeScore = score(60 + (positive(project.get("revenue")) ? 12 : 0) + (positive(project.get("profit")) ? 8 : 0), 88);
        int overallScore = (int) Math.round(teamScore * 0.3 + techScore * 0.25 + marketScore * 0.25 + financeScore * 0.2);

        List<Map<String, Object>> sections = new ArrayList<>();
        sections.add(section("团队背景分析",
                textOr(project.get("longDescription"), textOr(project.get("shortDescription"), "团队信息待补充。")),
                teamScore));
        sections.add(section("技术与产品",
                textOr(project.get("competitiveAdvantage"), "技术壁垒待进一步验证。"),
                techScore));
        sections.add(section("市场与竞争",
                textOr(project.get("marketSize"), "市场规模信息待补充。"),
                marketScore));
        sections.add(section("商业模式与财务",
                financialSummary(project),
                financeScore));

        Map<String, Object> route = route("review_brief");
        String aiAssessment = gateway.chat(
                str(route.get("model")),
                strOr(route.get("promptTemplate"), "你是一位资深投资人，请给出一句话评审结论。"),
                "项目名称：" + project.get("title") + "\n行业：" + project.get("industry")
                        + "\n阶段：" + project.get("stage") + "\n简介：" + textOr(project.get("shortDescription"), "无")
                        + "\n请用一句话给出总体评审结论（不超过 60 字）。",
                dblOr(route.get("temperature"), 0.4),
                intOr(route.get("maxTokens"), 512));

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", "brief-" + UUID.randomUUID().toString().substring(0, 8));
        m.put("projectId", projectId);
        m.put("overallScore", overallScore);
        m.put("sections", sections);
        m.put("overallAssessment", aiAssessment == null || aiAssessment.isBlank()
                ? (overallScore >= 80 ? "值得关注，建议推进。" : "建议补充关键材料后再评估。")
                : aiAssessment.trim());
        m.put("riskPoints", riskPoints(project));
        m.put("highlights", highlights(project));
        return m;
    }

    private Map<String, Object> findProject(String id) {
        List<Map<String, Object>> rows = jdbc.query("SELECT * FROM biz_project WHERE id = :id",
                new MapSqlParameterSource("id", id), (rs, i) -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", rs.getString("id"));
                    m.put("title", rs.getString("title"));
                    m.put("industry", rs.getString("industry"));
                    m.put("stage", rs.getString("stage"));
                    m.put("amountSought", rs.getObject("amount_sought") == null ? null : rs.getDouble("amount_sought"));
                    m.put("shortDescription", rs.getString("short_description"));
                    m.put("longDescription", rs.getString("long_description"));
                    m.put("businessModel", rs.getString("business_model"));
                    m.put("competitiveAdvantage", rs.getString("competitive_advantage"));
                    m.put("marketSize", rs.getString("market_size"));
                    m.put("revenue", rs.getObject("revenue") == null ? null : rs.getDouble("revenue"));
                    m.put("profit", rs.getObject("profit") == null ? null : rs.getDouble("profit"));
                    m.put("teamSize", rs.getObject("team_size"));
                    return m;
                });
        return rows.isEmpty() ? null : rows.get(0);
    }

    private static Map<String, Object> section(String title, String content, int score) {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("title", title);
        s.put("content", content);
        s.put("score", score);
        return s;
    }

    private static String financialSummary(Map<String, Object> p) {
        StringBuilder sb = new StringBuilder();
        sb.append(textOr(p.get("businessModel"), "商业模式待补充"));
        if (p.get("revenue") != null) sb.append("；年营收约 ").append(p.get("revenue")).append(" 万元");
        if (p.get("profit") != null) sb.append("；年利润约 ").append(p.get("profit")).append(" 万元");
        sb.append("；本轮拟融资 ").append(p.get("amountSought")).append(" 万元。");
        return sb.toString();
    }

    private static List<String> riskPoints(Map<String, Object> p) {
        List<String> risks = new ArrayList<>();
        if (!positive(p.get("revenue"))) risks.add("尚未产生规模化营收");
        if (!positive(p.get("profit"))) risks.add("盈利模式仍需验证");
        if (intOr(p.get("teamSize"), 0) < 10) risks.add("团队规模较小");
        risks.add("行业竞争压力");
        if (risks.size() > 3) risks = new ArrayList<>(risks.subList(0, 3));
        return risks;
    }

    private static List<String> highlights(Map<String, Object> p) {
        List<String> points = new ArrayList<>();
        if (hasText(p.get("competitiveAdvantage"))) points.add("技术/产品具备差异化优势");
        if (hasText(p.get("marketSize"))) points.add("目标市场空间明确");
        if (intOr(p.get("teamSize"), 0) >= 10) points.add("团队配置相对完整");
        if (points.isEmpty()) points.add("项目定位清晰");
        return points;
    }

    private Map<String, Object> demoReviewBrief(String projectId) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", "brief-" + UUID.randomUUID().toString().substring(0, 8));
        m.put("projectId", projectId == null ? "p-001" : projectId);
        m.put("overallScore", 82);
        List<Map<String, Object>> sections = new ArrayList<>();
        sections.add(section("团队背景分析", "核心团队均来自一线科技公司，经验丰富。", 85));
        sections.add(section("技术与产品", "自研多 Agent 编排框架，技术有壁垒。", 88));
        sections.add(section("市场与竞争", "市场空间大，定位清晰。", 80));
        sections.add(section("商业模式与财务", "SaaS 订阅制，ARR 约 500 万。", 72));
        m.put("sections", sections);
        m.put("overallAssessment", "值得关注，建议推进。");
        m.put("riskPoints", List.of("营收体量较小", "巨头竞争压力"));
        m.put("highlights", List.of("团队背景优秀", "技术壁垒明确"));
        return m;
    }

    // ========== AI 对话 ==========

    public Map<String, Object> chat(Map<String, Object> body) {
        String prompt = strOr(body.get("prompt"), strOr(body.get("message"), "你好"));
        String sessionId = strOr(body.get("sessionId"), "chat-" + UUID.randomUUID().toString().substring(0, 8));

        Map<String, Object> route = route("chat");
        String reply = gateway.chat(
                str(route.get("model")),
                strOr(route.get("promptTemplate"), "你是创融通的融资助手，请用中文简洁回答用户的问题。"),
                prompt,
                dblOr(route.get("temperature"), 0.8),
                intOr(route.get("maxTokens"), 2000));

        if (reply == null || reply.isBlank()) {
            reply = "关于「" + prompt + "」，我建议重点关注以下几点：1）明确目标投资人的行业与阶段偏好；"
                    + "2）用数据量化项目的增长与壁垒；3）准备 3 分钟与 10 分钟两个版本的融资陈述。";
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("reply", reply);
        m.put("sessionId", sessionId);
        return m;
    }

    /**
     * 读取模型路由配置；路由不存在时返回兜底配置，保证演示环境可用。
     */
    private Map<String, Object> route(String routeKey) {
        List<Map<String, Object>> rows = jdbc.query("""
                SELECT prompt_template, primary_model, model, temperature, max_tokens
                FROM sys_ai_config WHERE route_key = :key AND enabled = TRUE
                """, new MapSqlParameterSource("key", routeKey), (rs, i) -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("promptTemplate", rs.getString("prompt_template"));
                    m.put("model", rs.getString("primary_model") != null ? rs.getString("primary_model") : rs.getString("model"));
                    m.put("temperature", rs.getObject("temperature") == null ? 0.7 : rs.getDouble("temperature"));
                    m.put("maxTokens", rs.getObject("max_tokens") == null ? 2048 : rs.getInt("max_tokens"));
                    return m;
                });
        if (!rows.isEmpty()) return rows.get(0);
        return Map.of(
                "promptTemplate", "",
                "model", gateway.defaultModel(),
                "temperature", 0.7,
                "maxTokens", 2048);
    }

    // ========== 类型工具 ==========

    private static String str(Object v) { return v == null ? null : String.valueOf(v); }

    private static String strOr(Object v, String def) {
        return v == null || String.valueOf(v).isBlank() ? def : String.valueOf(v);
    }

    private static String textOr(Object v, String def) {
        String s = str(v);
        return s == null || s.isBlank() ? def : s;
    }

    private static boolean hasText(Object v) {
        return v != null && !String.valueOf(v).isBlank();
    }

    private static int intOr(Object v, int def) {
        if (v == null || String.valueOf(v).isBlank()) return def;
        if (v instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(v)); } catch (NumberFormatException e) { return def; }
    }

    private static double dblOr(Object v, double def) {
        if (v == null || String.valueOf(v).isBlank()) return def;
        if (v instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(String.valueOf(v)); } catch (NumberFormatException e) { return def; }
    }

    private static Double num(Object v) {
        if (v == null || String.valueOf(v).isBlank()) return null;
        if (v instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(String.valueOf(v)); } catch (NumberFormatException e) { return null; }
    }

    private static boolean positive(Object v) {
        Double d = num(v);
        return d != null && d > 0;
    }

    private static int score(int value, int max) {
        return Math.max(0, Math.min(max, value));
    }
}
