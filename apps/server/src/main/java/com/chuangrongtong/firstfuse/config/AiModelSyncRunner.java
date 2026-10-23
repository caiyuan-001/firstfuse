package com.chuangrongtong.firstfuse.config;

import com.chuangrongtong.firstfuse.domain.ai.AiGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 启动时把 sys_ai_config 里所有记录的模型字段同步为 AiGateway.defaultModel()，
 * 避免种子 SQL 写死的 gpt-4o/gpt-4o-mini 在换网关后直接 404。
 * 以后用户只需改 .env 的 AI_MODEL 重启即可。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiModelSyncRunner implements ApplicationRunner {

    private final JdbcTemplate jdbc;
    private final AiGateway gateway;

    @Override
    public void run(ApplicationArguments args) {
        if (!gateway.configured()) return;
        String model = gateway.defaultModel();
        int updated = jdbc.update("""
                UPDATE sys_ai_config
                SET primary_model = ?,
                    fallback_model = ?,
                    model = ?,
                    prompt_template = COALESCE(NULLIF(prompt_template, ''), prompt_template)
                WHERE enabled = TRUE
                """, model, model, model);
        log.info("[AI模型同步] 已将 {} 条 sys_ai_config 记录的模型字段统一为 {}", updated, model);
    }
}
