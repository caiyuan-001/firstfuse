package com.chuangrongtong.firstfuse.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具：签发 + 解析 access token。
 * 演示环境用对称 HMAC-SHA。
 */
@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expire-seconds:7200}")
    private long accessTokenExpireSeconds;

    private SecretKey key() {
        // 32+ 字符足够 HMAC-SHA256
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(bytes);
    }

    /**
     * 签发 access token。
     * 包含用户 id / role / tenantId，其余信息由服务端解析后查 DB。
     */
    public String issueAccessToken(String userId, String role, String tenantId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", userId);
        claims.put("role", role);
        claims.put("tid", tenantId);

        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(claims)
                .subject(userId)
                .issuedAt(new Date(now))
                .expiration(new Date(now + accessTokenExpireSeconds * 1000))
                .signWith(key())
                .compact();
    }

    /**
     * 解析 token。签名或过期校验失败抛 IllegalArgumentException。
     */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 快速提取 userId */
    public String getUserId(String token) {
        return parse(token).get("uid", String.class);
    }

    /** 快速提取 role */
    public String getRole(String token) {
        return parse(token).get("role", String.class);
    }

    /** 快速提取 tenantId */
    public String getTenantId(String token) {
        return parse(token).get("tid", String.class);
    }

    /** 是否过期 */
    public boolean isExpired(String token) {
        try {
            return parse(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
