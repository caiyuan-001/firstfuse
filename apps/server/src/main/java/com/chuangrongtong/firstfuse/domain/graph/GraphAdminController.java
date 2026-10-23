package com.chuangrongtong.firstfuse.domain.graph;

import com.chuangrongtong.firstfuse.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 图谱运维端点（platform_admin）：
 * POST /admin/graph/init — 创建 Nebula 空间/Schema 并全量同步 PG 图谱数据
 */
@RestController
@RequestMapping("/admin/graph")
@RequiredArgsConstructor
public class GraphAdminController {

    private final GraphSyncService graphSyncService;

    @PostMapping("/init")
    public ApiResponse<Map<String, Object>> init() {
        return ApiResponse.success(graphSyncService.init());
    }
}
