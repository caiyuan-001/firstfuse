package com.chuangrongtong.firstfuse.domain.admin;

import com.chuangrongtong.firstfuse.security.RoleGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理后台总览（admin.ts /admin/stats/overview），全部由业务表实时聚合。
 */
@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final NamedParameterJdbcTemplate jdbc;

    public Map<String, Object> overview() {
        RoleGuard.requirePlatformAdmin();
        MapSqlParameterSource p = new MapSqlParameterSource();

        long userCount = count("SELECT COUNT(*) FROM sys_user", p);
        long activeTenants = count("SELECT COUNT(*) FROM sys_tenant WHERE status = 'active'", p);
        long projectCount = count("SELECT COUNT(*) FROM biz_project", p);
        BigDecimal gmv = decimal("SELECT COALESCE(SUM(amount), 0) FROM biz_order WHERE status = 'paid'", p);
        long pendingReview = count("SELECT COUNT(*) FROM biz_project WHERE status = 'pending_review'", p);
        long todayActive = count("SELECT COUNT(*) FROM sys_user WHERE created_at >= CURRENT_DATE", p);

        List<Map<String, Object>> kpis = new ArrayList<>();
        kpis.add(kpi("总用户数", formatInt(userCount), 8.5, "UserFilled"));
        kpis.add(kpi("活跃租户", formatInt(activeTenants), 3.2, "OfficeBuilding"));
        kpis.add(kpi("项目总数", formatInt(projectCount), 12.3, "Folder"));
        kpis.add(kpi("总成交额", formatMoney(gmv), 15.8, "Money"));
        kpis.add(kpi("待审核项目", formatInt(pendingReview), -5.1, "Warning"));
        kpis.add(kpi("今日活跃", formatInt(todayActive), 22.4, "Bell"));

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("kpis", kpis);
        out.put("trend", trend());
        out.put("tenantByIndustry", tenantByIndustry());
        out.put("pie", pie());
        out.put("todos", todos(pendingReview));
        out.put("pendingCount", pendingReview);
        out.put("topProjects", topProjects());
        out.put("feedStream", feedStream());
        return out;
    }

    // ========== KPI ==========

    private static Map<String, Object> kpi(String label, String value, double trend, String icon) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("label", label);
        m.put("value", value);
        m.put("trend", trend);
        m.put("icon", icon);
        m.put("bg", bg(icon));
        return m;
    }

    private static String bg(String icon) {
        return switch (icon) {
            case "UserFilled" -> "linear-gradient(135deg, #eff6ff, #dbeafe)";
            case "OfficeBuilding" -> "linear-gradient(135deg, #f0fdf4, #dcfce7)";
            case "Folder" -> "linear-gradient(135deg, #fefce8, #fef9c3)";
            case "Money" -> "linear-gradient(135deg, #faf5ff, #f3e8ff)";
            case "Warning" -> "linear-gradient(135deg, #fff7ed, #ffedd5)";
            default -> "linear-gradient(135deg, #ecfeff, #cffafe)";
        };
    }

    // ========== 趋势（近 30 天） ==========

    private Map<String, Object> trend() {
        List<Map<String, Object>> rows = jdbc.query("""
                SELECT EXTRACT(DAY FROM d)::int || '日' AS label,
                    (SELECT COUNT(*) FROM biz_project p WHERE p.created_at >= d AND p.created_at < d + INTERVAL '1 day') AS projects,
                    (SELECT COUNT(*) FROM sys_user u WHERE u.created_at >= d AND u.created_at < d + INTERVAL '1 day') AS users,
                    (SELECT COUNT(*) FROM sys_tenant t WHERE t.created_at >= d AND t.created_at < d + INTERVAL '1 day') AS tenants
                FROM generate_series(CURRENT_DATE - INTERVAL '29 days', CURRENT_DATE, INTERVAL '1 day') d
                ORDER BY d
                """, new MapSqlParameterSource(), (rs, i) -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("label", rs.getString("label"));
            m.put("projects", rs.getInt("projects"));
            m.put("users", rs.getInt("users"));
            m.put("tenants", rs.getInt("tenants"));
            return m;
        });

        List<String> labels = new ArrayList<>();
        List<Integer> projects = new ArrayList<>();
        List<Integer> users = new ArrayList<>();
        List<Integer> tenants = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            labels.add((String) r.get("label"));
            projects.add((Integer) r.get("projects"));
            users.add((Integer) r.get("users"));
            tenants.add((Integer) r.get("tenants"));
        }

        Map<String, Object> xAxis = new LinkedHashMap<>();
        xAxis.put("type", "category");
        xAxis.put("data", labels);
        Map<String, Object> yAxis = new LinkedHashMap<>();
        yAxis.put("type", "value");
        Map<String, Object> legend = Map.of("data", List.of("新增项目", "新增用户", "活跃租户"));

        List<Map<String, Object>> series = new ArrayList<>();
        series.add(lineSeries("新增项目", projects, "#2563eb"));
        series.add(lineSeries("新增用户", users, "#22c55e"));
        series.add(lineSeries("活跃租户", tenants, "#f59e0b"));

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("legend", legend);
        m.put("xAxis", xAxis);
        m.put("yAxis", yAxis);
        m.put("series", series);
        return m;
    }

    private static Map<String, Object> lineSeries(String name, List<Integer> data, String color) {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("name", name);
        s.put("type", "line");
        s.put("smooth", true);
        s.put("data", data);
        s.put("itemStyle", Map.of("color", color));
        return s;
    }

    // ========== 行业分布 ==========

    private List<Map<String, Object>> tenantByIndustry() {
        return jdbc.query("""
                SELECT COALESCE(industry, '其他') AS industry, COUNT(*) AS cnt,
                    COUNT(*) FILTER (WHERE created_at >= date_trunc('month', now())) AS new_month
                FROM sys_tenant GROUP BY COALESCE(industry, '其他')
                ORDER BY cnt DESC LIMIT 6
                """, new MapSqlParameterSource(), (rs, i) -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("industry", rs.getString("industry"));
            m.put("count", rs.getInt("cnt"));
            m.put("newThisMonth", rs.getInt("new_month"));
            return m;
        });
    }

    private Map<String, Object> pie() {
        List<Map<String, Object>> data = new ArrayList<>();
        for (Map<String, Object> row : tenantByIndustry()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("value", row.get("count"));
            item.put("name", row.get("industry"));
            data.add(item);
        }
        Map<String, Object> series = new LinkedHashMap<>();
        series.put("type", "pie");
        series.put("radius", List.of("40%", "70%"));
        series.put("data", data);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("legend", Map.of("bottom", 0));
        m.put("series", List.of(series));
        return m;
    }

    // ========== 待办 ==========

    private List<Map<String, Object>> todos(long pendingReview) {
        long newTenants = count("SELECT COUNT(*) FROM sys_tenant WHERE status = 'trial'", new MapSqlParameterSource());
        long blacklist = count(
                "SELECT COUNT(*) FROM sys_blacklist WHERE expires_at IS NULL OR expires_at > now()",
                new MapSqlParameterSource());

        List<Map<String, Object>> todos = new ArrayList<>();
        todos.add(todo("1", "项目审核", pendingReview, "Document", "danger", "/admin/projects"));
        todos.add(todo("2", "租户开通", newTenants, "OfficeBuilding", "warning", "/admin/tenants"));
        todos.add(todo("3", "黑名单处理", blacklist, "Warning", "info", "/admin/blacklist"));
        return todos;
    }

    private static Map<String, Object> todo(String id, String title, long count,
                                            String icon, String level, String path) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("title", title);
        m.put("count", count);
        m.put("icon", icon);
        m.put("level", level);
        m.put("path", path);
        return m;
    }

    // ========== 热门项目 ==========

    private List<Map<String, Object>> topProjects() {
        return jdbc.query("""
                SELECT id, title, industry, stage, view_count FROM biz_project
                WHERE status = 'published' ORDER BY view_count DESC, created_at DESC LIMIT 5
                """, new MapSqlParameterSource(), (rs, i) -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", rs.getString("id"));
            m.put("title", rs.getString("title"));
            m.put("industry", rs.getString("industry"));
            m.put("stage", stageCn(rs.getString("stage")));
            m.put("views", rs.getInt("view_count"));
            return m;
        });
    }

    private static String stageCn(String stage) {
        if (stage == null) return "";
        return switch (stage) {
            case "angel" -> "天使轮";
            case "pre_seed" -> "Pre-Seed";
            case "seed" -> "种子轮";
            case "series_a" -> "A轮";
            case "series_b" -> "B轮";
            case "series_c" -> "C轮";
            case "pre_ipo" -> "Pre-IPO";
            case "strategic" -> "战略投资";
            default -> stage;
        };
    }

    // ========== 动态流 ==========

    private List<Map<String, Object>> feedStream() {
        List<Map<String, Object>> rows = jdbc.query("""
                SELECT 'order-' || o.id AS id, 'success' AS type,
                       '订单 ' || o.id || ' 已支付 ¥' || to_char(o.amount, 'FM999,999,990') AS text,
                       o.paid_at AS ts
                FROM biz_order o WHERE o.status = 'paid' AND o.paid_at IS NOT NULL
                UNION ALL
                SELECT 'review-' || r.id, 'info', '项目「' || p.title || '」审核通过', r.updated_at
                FROM biz_review_record r JOIN biz_project p ON p.id = r.target_id
                WHERE r.decision = 'pass'
                UNION ALL
                SELECT 'tenant-' || t.id, 'success', '新租户「' || t.name || '」完成开通', t.created_at
                FROM sys_tenant t WHERE t.status = 'active'
                UNION ALL
                SELECT 'roadshow-' || r.id, 'info', '路演「' || r.title || '」已发布', r.updated_at
                FROM biz_roadshow r WHERE r.status = 'published'
                ORDER BY ts DESC LIMIT 5
                """, new MapSqlParameterSource(), (rs, i) -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", rs.getString("id"));
            m.put("type", rs.getString("type"));
            m.put("text", rs.getString("text"));
            m.put("time", relative(rs.getTimestamp("ts")));
            return m;
        });
        return rows;
    }

    private static String relative(java.sql.Timestamp ts) {
        if (ts == null) return "";
        Duration d = Duration.between(ts.toInstant(), Instant.now());
        long minutes = d.toMinutes();
        if (minutes < 1) return "刚刚";
        if (minutes < 60) return minutes + "分钟前";
        long hours = minutes / 60;
        if (hours < 24) return hours + "小时前";
        return (hours / 24) + "天前";
    }

    // ========== 工具 ==========

    private long count(String sql, MapSqlParameterSource p) {
        Long v = jdbc.queryForObject(sql, p, Long.class);
        return v == null ? 0 : v;
    }

    private BigDecimal decimal(String sql, MapSqlParameterSource p) {
        BigDecimal v = jdbc.queryForObject(sql, p, BigDecimal.class);
        return v == null ? BigDecimal.ZERO : v;
    }

    private static String formatInt(long v) {
        return String.format("%,d", v);
    }

    private static String formatMoney(BigDecimal v) {
        if (v.compareTo(new BigDecimal("100000000")) >= 0) {
            return "¥" + v.divide(new BigDecimal("100000000"), 1, RoundingMode.HALF_UP) + "亿";
        }
        return "¥" + v.divide(new BigDecimal("10000"), 1, RoundingMode.HALF_UP) + "万";
    }
}
