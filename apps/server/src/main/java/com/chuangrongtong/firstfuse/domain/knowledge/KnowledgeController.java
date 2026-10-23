package com.chuangrongtong.firstfuse.domain.knowledge;

import com.chuangrongtong.firstfuse.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 知识库端点，对齐 mock/modules/knowledge.ts：
 *   GET    /knowledge                 列表（keyword/category/分页）
 *   GET    /knowledge/:id             详情（缺省 {}）
 *   POST   /knowledge                 新增 → {id}
 *   PUT    /knowledge/:id             更新 → {success:true}
 *   PATCH  /knowledge/:id/publish     发布切换 → {success:true}
 *   DELETE /knowledge/:id             删除 → {success:true}
 */
@RestController
@RequestMapping("/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(knowledgeService.list(keyword, category, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getById(@PathVariable String id) {
        return ApiResponse.success(knowledgeService.getById(id));
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(knowledgeService.create(body));
    }

    @PutMapping("/{id}")
    public ApiResponse<Map<String, Object>> update(@PathVariable String id, @RequestBody Map<String, Object> body) {
        return ApiResponse.success(knowledgeService.update(id, body));
    }

    @PatchMapping("/{id}/publish")
    public ApiResponse<Map<String, Object>> publish(@PathVariable String id, @RequestBody(required = false) Map<String, Object> body) {
        return ApiResponse.success(knowledgeService.publish(id, body));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Map<String, Object>> delete(@PathVariable String id) {
        return ApiResponse.success(knowledgeService.delete(id));
    }
}
