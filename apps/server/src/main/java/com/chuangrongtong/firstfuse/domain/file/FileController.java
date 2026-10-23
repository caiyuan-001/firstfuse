package com.chuangrongtong.firstfuse.domain.file;

import com.chuangrongtong.firstfuse.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 文件端点（M4）：
 *   POST /files          上传（multipart/form-data，字段名 file）
 *   GET  /files/{id}     下载（流式返回，Content-Disposition attachment）
 *   GET  /files/{id}/info 元数据
 */
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping
    public ApiResponse<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(fileService.upload(file));
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> download(@PathVariable String id) {
        FileService.Download d = fileService.download(id);
        String encoded = URLEncoder.encode(d.fileName(), StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(d.contentType() == null || d.contentType().isBlank()
                        ? MediaType.APPLICATION_OCTET_STREAM
                        : MediaType.parseMediaType(d.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encoded)
                .body(d.bytes());
    }

    @GetMapping("/{id}/info")
    public ApiResponse<Map<String, Object>> info(@PathVariable String id) {
        return ApiResponse.success(fileService.info(id));
    }
}
