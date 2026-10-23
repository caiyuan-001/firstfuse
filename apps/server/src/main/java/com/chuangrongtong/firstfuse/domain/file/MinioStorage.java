package com.chuangrongtong.firstfuse.domain.file;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * MinIO 对象存储客户端（懒加载，首次上传时确保 bucket 存在）。
 * 连接信息由 .env 注入（MINIO_ENDPOINT / MINIO_ACCESS_KEY / MINIO_SECRET_KEY / MINIO_BUCKET）。
 */
@Slf4j
@Component
public class MinioStorage {

    private final String endpoint;
    private final String accessKey;
    private final String secretKey;
    private final String bucket;

    private MinioClient client;
    private boolean bucketReady;

    public MinioStorage(@Value("${minio.endpoint}") String endpoint,
                        @Value("${minio.access-key}") String accessKey,
                        @Value("${minio.secret-key}") String secretKey,
                        @Value("${minio.bucket}") String bucket) {
        this.endpoint = endpoint;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.bucket = bucket;
    }

    public String bucket() {
        return bucket;
    }

    /** 上传对象；objectKey 形如 tenantId/202610/uuid-name */
    public synchronized void put(String objectKey, InputStream in, long size, String contentType) {
        try {
            ensureBucket();
            client.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(in, size, -1)
                    .contentType(contentType == null || contentType.isBlank()
                            ? "application/octet-stream" : contentType)
                    .build());
        } catch (Exception e) {
            log.error("[MinIO] 上传失败 {}: {}", objectKey, e.getMessage());
            throw new IllegalStateException("文件存储服务异常：" + e.getMessage(), e);
        }
    }

    /** 下载对象为字节数组（演示规模文件 < 20MB，直接内存缓冲） */
    public synchronized byte[] get(String objectKey) {
        try {
            ensureBucket();
            InputStream in = client.getObject(GetObjectArgs.builder()
                    .bucket(bucket).object(objectKey).build());
            try {
                return in.readAllBytes();
            } finally {
                in.close();
            }
        } catch (Exception e) {
            log.error("[MinIO] 下载失败 {}: {}", objectKey, e.getMessage());
            throw new IllegalStateException("文件存储服务异常：" + e.getMessage(), e);
        }
    }

    private void ensureBucket() throws Exception {
        if (client == null) {
            client = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();
        }
        if (!bucketReady) {
            boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("[MinIO] 创建 bucket {}", bucket);
            }
            bucketReady = true;
        }
    }
}
