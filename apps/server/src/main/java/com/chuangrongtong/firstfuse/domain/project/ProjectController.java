package com.chuangrongtong.firstfuse.domain.project;

import com.chuangrongtong.firstfuse.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 项目端点，对齐 mock/modules/project.ts（创业者/投资人侧 6 端点）。
 */
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(projectService.list(pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getById(@PathVariable String id) {
        return ApiResponse.success(projectService.getById(id));
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(projectService.create(body));
    }

    @PutMapping("/{id}")
    public ApiResponse<Map<String, Object>> update(@PathVariable String id, @RequestBody Map<String, Object> body) {
        return ApiResponse.success(projectService.update(id, body));
    }

    @PostMapping("/{id}/submit")
    public ApiResponse<Map<String, Object>> submit(@PathVariable String id) {
        return ApiResponse.success(projectService.submit(id));
    }

    @PostMapping("/diagnosis")
    public ApiResponse<Map<String, Object>> diagnosis() {
        return ApiResponse.success(projectService.diagnosis());
    }
}
