package com.chuangrongtong.firstfuse.domain.roadshow;

import com.chuangrongtong.firstfuse.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 路演端点，对齐 mock/modules/roadshow.ts：
 *   GET  /roadshows           列表（keyword/organizer/status/分页）
 *   GET  /roadshows/:id       详情（缺省 {}）
 *   POST /roadshows           创建 → {id}
 *   PUT  /roadshows/:id       更新 → {success:true}
 *   POST /roadshows/:id/apply 报名 → {success:true}
 */
@RestController
@RequestMapping("/roadshows")
@RequiredArgsConstructor
public class RoadshowController {

    private final RoadshowService roadshowService;

    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String organizer,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "12") int pageSize) {
        return ApiResponse.success(roadshowService.list(keyword, organizer, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getById(@PathVariable String id) {
        return ApiResponse.success(roadshowService.getById(id));
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(roadshowService.create(body));
    }

    @PutMapping("/{id}")
    public ApiResponse<Map<String, Object>> update(@PathVariable String id, @RequestBody Map<String, Object> body) {
        return ApiResponse.success(roadshowService.update(id, body));
    }

    @PostMapping("/{id}/apply")
    public ApiResponse<Map<String, Object>> apply(@PathVariable String id, @RequestBody(required = false) Map<String, Object> body) {
        return ApiResponse.success(roadshowService.apply(id, body));
    }
}
