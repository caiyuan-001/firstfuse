package com.chuangrongtong.firstfuse.domain.tenant;

import com.chuangrongtong.firstfuse.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 租户管理端点，对齐 mock/modules/tenant.ts：
 *   GET  /tenants           列表（keyword/type/status/分页）
 *   GET  /tenants/:id       详情
 *   POST /tenants           开通
 *   PUT  /tenants/:id/status  变更状态
 */
@RestController
@RequestMapping("/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(tenantService.list(keyword, type, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getById(@PathVariable String id) {
        return ApiResponse.success(tenantService.getById(id));
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(tenantService.create(body));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Map<String, Object>> updateStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        return ApiResponse.success(tenantService.updateStatus(id, body.get("status")));
    }
}
