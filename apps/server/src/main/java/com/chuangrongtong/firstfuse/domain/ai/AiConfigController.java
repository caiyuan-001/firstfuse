package com.chuangrongtong.firstfuse.domain.ai;

import com.chuangrongtong.firstfuse.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AIConfig 端点，对齐 mock/modules/admin.ts（7 端点，仅平台管理员）。
 */
@RestController
@RequestMapping("/admin/ai")
@RequiredArgsConstructor
public class AiConfigController {

    private final AiConfigService aiConfigService;

    @GetMapping("/prompts")
    public ApiResponse<List<Map<String, Object>>> listPrompts() {
        return ApiResponse.success(aiConfigService.listPrompts());
    }

    @PostMapping("/prompts")
    public ApiResponse<Map<String, Object>> createPrompt(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(aiConfigService.createPrompt(body));
    }

    @PutMapping("/prompts/{id}")
    public ApiResponse<Map<String, Object>> updatePrompt(@PathVariable String id, @RequestBody Map<String, Object> body) {
        return ApiResponse.success(aiConfigService.updatePrompt(id, body));
    }

    @DeleteMapping("/prompts/{id}")
    public ApiResponse<Map<String, Object>> deletePrompt(@PathVariable String id) {
        return ApiResponse.success(aiConfigService.deletePrompt(id));
    }

    @GetMapping("/models")
    public ApiResponse<List<Map<String, Object>>> listRoutes() {
        return ApiResponse.success(aiConfigService.listRoutes());
    }

    @PostMapping("/models")
    public ApiResponse<Map<String, Object>> saveRoute(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(aiConfigService.saveRoute(body));
    }

    @PutMapping("/models/{id}")
    public ApiResponse<Map<String, Object>> updateRoute(@PathVariable String id, @RequestBody Map<String, Object> body) {
        return ApiResponse.success(aiConfigService.updateRoute(id, body));
    }
}
