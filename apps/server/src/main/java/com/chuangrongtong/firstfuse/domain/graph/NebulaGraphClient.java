package com.chuangrongtong.firstfuse.domain.graph;

import com.vesoft.nebula.client.graph.NebulaPoolConfig;
import com.vesoft.nebula.client.graph.data.HostAddress;
import com.vesoft.nebula.client.graph.data.ResultSet;
import com.vesoft.nebula.client.graph.data.ValueWrapper;
import com.vesoft.nebula.client.graph.net.NebulaPool;
import com.vesoft.nebula.client.graph.net.Session;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * NebulaGraph 客户端（Thrift 协议，graphd 默认 9669，依赖 nebula-java client）。
 *
 * - nebula.enabled=false（默认）时不发起任何远程调用，图谱数据由 PostgreSQL 派生
 * - 启用前需已创建 `nebula.space` 与 Tag/Edge（见 resources/nebula/schema.ngql）
 * - Session 非线程安全，本类对执行串行化；会话失效时释放并下次重连
 */
@Slf4j
@Component
public class NebulaGraphClient {

    private final boolean enabled;
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String space;

    private NebulaPool pool;
    private Session session;
    private boolean spaceSelected;

    public NebulaGraphClient(@Value("${nebula.enabled:false}") boolean enabled,
                             @Value("${nebula.host:100.66.1.5}") String host,
                             @Value("${nebula.port:9669}") int port,
                             @Value("${nebula.username:root}") String username,
                             @Value("${nebula.password:nebula}") String password,
                             @Value("${nebula.space:firstfuse}") String space) {
        this.enabled = enabled;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.space = space;
    }

    public boolean enabled() {
        return enabled;
    }

    /**
     * 在指定空间内执行 nGQL；未启用、未连接或执行失败返回 null。
     */
    public synchronized List<Map<String, Object>> query(String nGql) {
        if (!enabled) return null;
        if (!ensureSession()) return null;
        if (!spaceSelected) {
            if (exec("USE `" + space + "`") == null) return null;
            spaceSelected = true;
        }
        return exec(nGql);
    }

    /**
     * 执行不依赖空间选择的 nGQL（如 SHOW SPACES / CREATE SPACE）。
     */
    public synchronized List<Map<String, Object>> executeRaw(String nGql) {
        if (!enabled) return null;
        if (!ensureSession()) return null;
        return exec(nGql);
    }

    private boolean ensureSession() {
        if (!ensurePool()) return false;
        if (session != null) return true;
        try {
            session = pool.getSession(username, password, false);
            spaceSelected = false;
            return session != null;
        } catch (Exception e) {
            log.warn("[Nebula] 获取会话失败：{}", e.getMessage());
            return false;
        }
    }

    private boolean ensurePool() {
        if (pool != null) return true;
        try {
            NebulaPoolConfig config = new NebulaPoolConfig();
            config.setMaxConnSize(5);
            NebulaPool p = new NebulaPool();
            p.init(List.of(new HostAddress(host, port)), config);
            pool = p;
            return true;
        } catch (Exception e) {
            log.warn("[Nebula] 连接池初始化失败（{}:{}）：{}", host, port, e.getMessage());
            return false;
        }
    }

    private List<Map<String, Object>> exec(String nGql) {
        try {
            ResultSet rs = session.execute(nGql);
            if (!rs.isSucceeded()) {
                log.warn("[Nebula] nGQL 失败：{}", rs.getErrorMessage());
                if (rs.getErrorMessage() != null && rs.getErrorMessage().contains("SpaceNotFound")) {
                    spaceSelected = false;
                }
                return null;
            }

            List<String> columns = rs.getColumnNames();
            List<Map<String, Object>> rows = new ArrayList<>();
            for (int i = 0; i < rs.rowsSize(); i++) {
                ResultSet.Record row = rs.rowValues(i);
                List<ValueWrapper> values = row.values();
                Map<String, Object> m = new LinkedHashMap<>();
                for (int c = 0; c < columns.size() && c < values.size(); c++) {
                    m.put(columns.get(c), toJava(values.get(c)));
                }
                rows.add(m);
            }
            return rows;
        } catch (Exception e) {
            log.warn("[Nebula] 执行异常，释放会话：{}", e.getMessage());
            releaseSession();
            return null;
        }
    }

    private static Object toJava(ValueWrapper v) {
        if (v == null || v.isNull()) return null;
        try {
            if (v.isString()) return v.asString();
            if (v.isLong()) return v.asLong();
            if (v.isDouble()) return v.asDouble();
            if (v.isBoolean()) return v.asBoolean();
            if (v.isDate()) return v.asDate().toString();
            if (v.isTime()) return v.asTime().toString();
            if (v.isDateTime()) return v.asDateTime().toString();
            return v.toString();
        } catch (Exception e) {
            return v.toString();
        }
    }

    private void releaseSession() {
        try {
            if (session != null) session.release();
        } catch (Exception ignored) {
            // 忽略释放异常
        }
        session = null;
        spaceSelected = false;
    }

    @PreDestroy
    public synchronized void close() {
        releaseSession();
        if (pool != null) {
            try {
                pool.close();
            } catch (Exception ignored) {
                // 忽略关闭异常
            }
            pool = null;
        }
    }
}
