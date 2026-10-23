package com.chuangrongtong.firstfuse.domain.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI 兼容大模型网关。
 * - 配置了 ai.base-url + ai.api-key 时调用 /chat/completions
 * - 未配置（开发演示环境）时返回 null，由调用方走本地演示生成
 */
@Slf4j
@Component
public class AiGateway {

    private final String baseUrl;
    private final String apiKey;
    private final String defaultModel;
    private final long timeoutMs;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public AiGateway(@Value("${ai.base-url:}") String baseUrl,
                     @Value("${ai.api-key:}") String apiKey,
                     @Value("${ai.model:gpt-4o-mini}") String defaultModel,
                     @Value("${ai.timeout-ms:15000}") long timeoutMs,
                     ObjectMapper objectMapper) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.defaultModel = defaultModel;
        this.timeoutMs = timeoutMs;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .build();
    }

    /** 是否已配置真实模型网关 */
    public boolean configured() {
        return !baseUrl.isBlank() && !apiKey.isBlank();
    }

    public String defaultModel() {
        return defaultModel;
    }

    /**
     * 发起一次对话补全。未配置或调用失败返回 null（调用方降级本地演示）。
     */
    public String chat(String model, String systemPrompt, String userPrompt,
                       double temperature, int maxTokens) {
        if (!configured()) return null;
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", model == null || model.isBlank() ? defaultModel : model);
            body.put("temperature", temperature);
            body.put("max_tokens", maxTokens);
            List<Map<String, String>> messages = new ArrayList<>();
            if (systemPrompt != null && !systemPrompt.isBlank()) {
                messages.add(Map.of("role", "system", "content", systemPrompt));
            }
            messages.add(Map.of("role", "user", "content", userPrompt));
            body.put("messages", messages);

            String url = baseUrl.replaceAll("/+$", "") + "/chat/completions";
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                log.warn("[AI网关] HTTP {} — {}", resp.statusCode(), abbreviate(resp.body()));
                return null;
            }
            JsonNode root = objectMapper.readTree(resp.body());
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            return content.isMissingNode() || content.isNull() ? null : content.asText();
        } catch (Exception e) {
            log.warn("[AI网关] 调用失败，降级本地演示：{}", e.getMessage());
            return null;
        }
    }

    private static String abbreviate(String s) {
        if (s == null) return "";
        return s.length() > 300 ? s.substring(0, 300) + "..." : s;
    }
}
