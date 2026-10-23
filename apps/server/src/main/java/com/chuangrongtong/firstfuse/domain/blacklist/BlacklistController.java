package com.chuangrongtong.firstfuse.domain.blacklist;

import com.chuangrongtong.firstfuse.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 黑名单端点，对齐 mock/modules/blacklist.ts：
 *   GET    /blacklist       列表（keyword/type/分页）
 *   POST   /blacklist       新增 → {id}
 *   DELETE /blacklist/:id   移除 → {success:true}
 */
@RestController
@RequestMapping("/blacklist")
@RequiredArgsConstructor
public class BlacklistController {

    private final BlacklistService blacklistService;

    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(blacklistService.list(keyword, type, pageNum, pageSize));
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(blacklistService.create(body));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Map<String, Object>> delete(@PathVariable String id) {
        return ApiResponse.success(blacklistService.delete(id));
    }
}
