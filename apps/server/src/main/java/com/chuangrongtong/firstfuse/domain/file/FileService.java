package com.chuangrongtong.firstfuse.domain.file;

import com.chuangrongtong.firstfuse.common.BusinessException;
import com.chuangrongtong.firstfuse.common.ErrorCode;
import com.chuangrongtong.firstfuse.security.RoleGuard;
import com.chuangrongtong.firstfuse.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传/下载闭环：MinIO 存对象，sys_file 存元数据。
 * 权限：上传需登录；下载仅本租户或 platform_admin。
 */
@Service
@RequiredArgsConstructor
public class FileService {

    private final NamedParameterJdbcTemplate jdbc;
    private final MinioStorage storage;

    /** POST /files — 上传，返回文件元数据（url 为下载端点） */
    public Map<String, Object> upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件不能为空");
        }
        String id = "f-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String tenantId = TenantContext.getTenantId();
        String original = file.getOriginalFilename() == null ? "unnamed" : file.getOriginalFilename();
        String safeName = original.replaceAll("[\\\\/:*?\"<>|]", "_");
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String objectKey = tenantId + "/" + datePath + "/" + id + "-" + safeName;

        try (InputStream in = file.getInputStream()) {
            storage.put(objectKey, in, file.getSize(), file.getContentType());
        } catch (IllegalStateException e) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_FAIL, e.getMessage());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "文件读取失败");
        }

        jdbc.update("""
                INSERT INTO sys_file (id, tenant_id, uploader_id, file_name, content_type, size_bytes, bucket, object_key)
                VALUES (:id, :tid, :uid, :name, :ct, :size, :bucket, :key)
                """, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("tid", tenantId)
                .addValue("uid", TenantContext.getUserId())
                .addValue("name", original)
                .addValue("ct", file.getContentType())
                .addValue("size", file.getSize())
                .addValue("bucket", storage.bucket())
                .addValue("key", objectKey));

        return toDto(id, tenantId, original, file.getContentType(), file.getSize());
    }

    /** GET /files/{id} — 元数据 + 内容字节；越权抛 403，不存在抛 404 */
    public Download download(String id) {
        Map<String, Object> meta = find(id);
        if (meta == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文件不存在");
        }
        RoleGuard.requireSameTenant((String) meta.get("tenant_id"));
        byte[] bytes;
        try {
            bytes = storage.get((String) meta.get("object_key"));
        } catch (IllegalStateException e) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_FAIL, e.getMessage());
        }
        return new Download(
                (String) meta.get("file_name"),
                (String) meta.get("content_type"),
                bytes);
    }

    /** GET /files/{id}/info — 仅元数据 */
    public Map<String, Object> info(String id) {
        Map<String, Object> meta = find(id);
        if (meta == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文件不存在");
        }
        RoleGuard.requireSameTenant((String) meta.get("tenant_id"));
        return toDto((String) meta.get("id"), (String) meta.get("tenant_id"),
                (String) meta.get("file_name"), (String) meta.get("content_type"),
                ((Number) meta.get("size_bytes")).longValue());
    }

    private Map<String, Object> find(String id) {
        List<Map<String, Object>> rows = jdbc.query(
                "SELECT * FROM sys_file WHERE id = :id",
                new MapSqlParameterSource("id", id),
                (rs, i) -> mapMeta(rs));
        return rows.isEmpty() ? null : rows.get(0);
    }

    private static Map<String, Object> mapMeta(ResultSet rs) throws SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", rs.getString("id"));
        m.put("tenant_id", rs.getString("tenant_id"));
        m.put("file_name", rs.getString("file_name"));
        m.put("content_type", rs.getString("content_type"));
        m.put("size_bytes", rs.getLong("size_bytes"));
        m.put("object_key", rs.getString("object_key"));
        return m;
    }

    private static Map<String, Object> toDto(String id, String tenantId, String fileName,
                                             String contentType, long size) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("tenantId", tenantId);
        m.put("fileName", fileName);
        m.put("contentType", contentType);
        m.put("size", size);
        m.put("url", "/files/" + id);
        return m;
    }

    public record Download(String fileName, String contentType, byte[] bytes) {
    }
}
