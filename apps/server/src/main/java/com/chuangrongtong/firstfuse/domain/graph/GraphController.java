package com.chuangrongtong.firstfuse.domain.graph;

import com.chuangrongtong.firstfuse.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 关系图谱查询接口，返回 shared GraphData 结构。
 * 传入 centerId 时只返回该节点 depth 度以内的邻域（depth 默认 1，最大 3）。
 */
@RestController
@RequestMapping("/graph")
@RequiredArgsConstructor
public class GraphController {

    private final GraphService graphService;

    @GetMapping
    public ApiResponse<Map<String, Object>> graph(
            @RequestParam(required = false) String centerId,
            @RequestParam(defaultValue = "1") int depth) {
        return ApiResponse.success(graphService.graph(centerId, depth));
    }
}
