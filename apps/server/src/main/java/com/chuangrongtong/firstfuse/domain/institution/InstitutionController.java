package com.chuangrongtong.firstfuse.domain.institution;

import com.chuangrongtong.firstfuse.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 机构端点，对齐 mock/modules/institution.ts：
 *   GET /institutions       列表
 *   GET /institutions/:id   详情
 */
@RestController
@RequestMapping("/institutions")
@RequiredArgsConstructor
public class InstitutionController {

    private final InstitutionService institutionService;

    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "12") int pageSize) {
        return ApiResponse.success(institutionService.list(pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getById(@PathVariable String id) {
        return ApiResponse.success(institutionService.getById(id));
    }
}
