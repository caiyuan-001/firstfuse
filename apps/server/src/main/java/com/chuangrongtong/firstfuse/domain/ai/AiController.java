package com.chuangrongtong.firstfuse.domain.ai;

import com.chuangrongtong.firstfuse.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * AI 业务端点，对齐 mock/modules/ai.ts（3 端点，登录用户可用）。
 */
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    /** 项目筛选：按行业/阶段/金额偏好对已发布项目打分 */
    @PostMapping("/project-filter")
    public ApiResponse<Map<String, Object>> projectFilter(@RequestBody(required = false) Map<String, Object> body) {
        return ApiResponse.success(aiService.projectFilter(body == null ? Map.of() : body));
    }

    /** 生成投资人评审简报 */
    @PostMapping("/review-brief")
    public ApiResponse<Map<String, Object>> reviewBrief(@RequestBody(required = false) Map<String, Object> body) {
        return ApiResponse.success(aiService.reviewBrief(body == null ? Map.of() : body));
    }

    /** AI 对话 */
    @PostMapping("/chat")
    public ApiResponse<Map<String, Object>> chat(@RequestBody(required = false) Map<String, Object> body) {
        return ApiResponse.success(aiService.chat(body == null ? Map.of() : body));
    }
}
