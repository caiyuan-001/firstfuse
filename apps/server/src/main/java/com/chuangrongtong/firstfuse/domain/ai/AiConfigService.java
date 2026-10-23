package com.chuangrongtong.firstfuse.domain.ai;

import com.chuangrongtong.firstfuse.common.BusinessException;
import com.chuangrongtong.firstfuse.common.ErrorCode;
import com.chuangrongtong.firstfuse.security.RoleGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * AIConfig 管理（管理后台），对齐 mock/modules/admin.ts：
 *   GET    /admin/ai/prompts       数组（非分页）
 *   POST   /admin/ai/prompts       → {id}
 *   PUT    /admin/ai/prompts/:id   → {success:true}
 *   DELETE /admin/ai/prompts/:id   → {success:true}
 *   GET    /admin/ai/models        数组
 *   POST   /admin/ai/models        → {success:true}
 *   PUT    /admin/ai/models/:id    → {success:true}（:id = routeKey）
 */
@Service
@RequiredArgsConstructor
public class AiConfigService {

    private final NamedParameterJdbcTemplate jdbc;

    // ========== Prompt 模板 ==========

    private static Map<String, Object> mapPrompt(ResultSet rs) throws SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", rs.getString("id"));
        m.put("name", rs.getString("name"));
        m.put("scene", rs.getString("scene"));
        m.put("model", rs.getString("model"));
        m.put("promptTemplate", rs.getString("prompt_template"));
        m.put("enabled", rs.getBoolean("enabled"));
        m.put("usageCount", rs.getInt("usage_count"));
        m.put("avgScore", rs.getBigDecimal("avg_score"));
        m.put("isDefault", rs.getBoolean("is_default"));
        return m;
    }

    public List<Map<String, Object>> listPrompts() {
        RoleGuard.requirePlatformAdmin();
        return jdbc.query("SELECT * FROM sys_ai_prompt ORDER BY id", new MapSqlParameterSource(),
                (rs, i) -> mapPrompt(rs));
    }

    public Map<String, Object> createPrompt(Map<String, Object> body) {
        RoleGuard.requirePlatformAdmin();
        String id = "pf-" + UUID.randomUUID().toString().substring(0, 8);
        jdbc.update("""
                INSERT INTO sys_ai_prompt (id, name, scene, model, prompt_template, enabled, is_default)
                VALUES (:id, :name, :scene, :model, :tpl, :enabled, :isDefault)
                """, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("name", strOr(body.get("name"), "未命名模板"))
                .addValue("scene", strOr(body.get("scene"), "chat"))
                .addValue("model", strOr(body.get("model"), "gpt-4o-mini"))
                .addValue("tpl", strOr(body.get("promptTemplate"), ""))
                .addValue("enabled", body.get("enabled") == null || Boolean.TRUE.equals(body.get("enabled")))
                .addValue("isDefault", Boolean.TRUE.equals(body.get("isDefault"))));
        return Map.of("id", id);
    }

    public Map<String, Object> updatePrompt(String id, Map<String, Object> body) {
        RoleGuard.requirePlatformAdmin();
        Map<String, Object> cur = findPrompt(id);
        jdbc.update("""
                UPDATE sys_ai_prompt SET name = :name, scene = :scene, model = :model, prompt_template = :tpl,
                    enabled = :enabled, is_default = :isDefault, updated_at = now()
                WHERE id = :id
                """, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("name", strOr(body.get("name"), (String) cur.get("name")))
                .addValue("scene", strOr(body.get("scene"), (String) cur.get("scene")))
                .addValue("model", strOr(body.get("model"), (String) cur.get("model")))
                .addValue("tpl", strOr(body.get("promptTemplate"), (String) cur.get("promptTemplate")))
                .addValue("enabled", body.get("enabled") == null ? cur.get("enabled") : Boolean.TRUE.equals(body.get("enabled")))
                .addValue("isDefault", body.get("isDefault") == null ? cur.get("isDefault") : Boolean.TRUE.equals(body.get("isDefault"))));
        return Map.of("success", true);
    }

    public Map<String, Object> deletePrompt(String id) {
        RoleGuard.requirePlatformAdmin();
        jdbc.update("DELETE FROM sys_ai_prompt WHERE id = :id", new MapSqlParameterSource("id", id));
        return Map.of("success", true);
    }

    private Map<String, Object> findPrompt(String id) {
        List<Map<String, Object>> rows = jdbc.query("SELECT * FROM sys_ai_prompt WHERE id = :id",
                new MapSqlParameterSource("id", id), (rs, i) -> mapPrompt(rs));
        if (rows.isEmpty()) throw new BusinessException(ErrorCode.NOT_FOUND, "Prompt 模板不存在");
        return rows.get(0);
    }

    // ========== 模型路由 ==========

    private static Map<String, Object> mapRoute(ResultSet rs) throws SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("routeKey", rs.getString("route_key"));
        m.put("scene", rs.getString("scene"));
        m.put("primaryModel", rs.getString("primary_model"));
        m.put("fallbackModel", rs.getString("fallback_model") == null ? "" : rs.getString("fallback_model"));
        m.put("temperature", rs.getBigDecimal("temperature"));
        m.put("maxTokens", rs.getObject("max_tokens"));
        m.put("enabled", rs.getBoolean("enabled"));
        return m;
    }

    public List<Map<String, Object>> listRoutes() {
        RoleGuard.requirePlatformAdmin();
        return jdbc.query("SELECT * FROM sys_ai_config ORDER BY route_key", new MapSqlParameterSource(),
                (rs, i) -> mapRoute(rs));
    }

    public Map<String, Object> saveRoute(Map<String, Object> body) {
        RoleGuard.requirePlatformAdmin();
        String routeKey = strOr(body.get("routeKey"), null);
        if (routeKey == null) {
            throw new BusinessException(ErrorCode.PARAM_VALIDATION, "routeKey 必填");
        }
        String primary = strOr(body.get("primaryModel"), "gpt-4o-mini");
        jdbc.update("""
                INSERT INTO sys_ai_config (id, route_key, scene, primary_model, fallback_model, model,
                    prompt_template, temperature, max_tokens, enabled)
                VALUES (:id, :routeKey, :scene, :primaryModel, :fallbackModel, :primaryModel, '', :temperature, :maxTokens, :enabled)
                ON CONFLICT (route_key) DO UPDATE SET
                    scene = EXCLUDED.scene, primary_model = EXCLUDED.primary_model,
                    fallback_model = EXCLUDED.fallback_model, model = EXCLUDED.model,
                    temperature = EXCLUDED.temperature, max_tokens = EXCLUDED.max_tokens,
                    enabled = EXCLUDED.enabled, updated_at = now()
                """, new MapSqlParameterSource()
                .addValue("id", "rt-" + UUID.randomUUID().toString().substring(0, 8))
                .addValue("routeKey", routeKey)
                .addValue("scene", strOr(body.get("scene"), routeKey))
                .addValue("primaryModel", primary)
                .addValue("fallbackModel", strOr(body.get("fallbackModel"), ""))
                .addValue("temperature", body.get("temperature") == null ? 0.7 : body.get("temperature"))
                .addValue("maxTokens", body.get("maxTokens") == null ? 2048 : body.get("maxTokens"))
                .addValue("enabled", body.get("enabled") == null || Boolean.TRUE.equals(body.get("enabled"))));
        return Map.of("success", true);
    }

    public Map<String, Object> updateRoute(String routeKey, Map<String, Object> body) {
        RoleGuard.requirePlatformAdmin();
        int n = jdbc.update("""
                UPDATE sys_ai_config SET scene = COALESCE(:scene, scene), primary_model = COALESCE(:primaryModel, primary_model),
                    fallback_model = COALESCE(:fallbackModel, fallback_model), model = COALESCE(:primaryModel, model),
                    temperature = COALESCE(:temperature, temperature), max_tokens = COALESCE(:maxTokens, max_tokens),
                    enabled = COALESCE(:enabled, enabled), updated_at = now()
                WHERE route_key = :routeKey
                """, new MapSqlParameterSource()
                .addValue("routeKey", routeKey)
                .addValue("scene", strOr(body.get("scene"), null))
                .addValue("primaryModel", strOr(body.get("primaryModel"), null))
                .addValue("fallbackModel", body.get("fallbackModel") == null ? null : strOr(body.get("fallbackModel"), ""))
                .addValue("temperature", body.get("temperature"))
                .addValue("maxTokens", body.get("maxTokens"))
                .addValue("enabled", body.get("enabled") == null ? null : Boolean.TRUE.equals(body.get("enabled"))));
        if (n == 0) throw new BusinessException(ErrorCode.NOT_FOUND, "模型路由不存在");
        return Map.of("success", true);
    }

    private static String strOr(Object v, String def) {
        return v == null || String.valueOf(v).isBlank() ? def : String.valueOf(v);
    }
}
