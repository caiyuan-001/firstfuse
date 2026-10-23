package com.chuangrongtong.firstfuse.domain.support;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

/**
 * PostgreSQL TEXT[] 数组参数绑定 helper（NamedParameterJdbcTemplate 对数组支持弱，走 PreparedStatement）。
 */
public final class PgArrays {

    private PgArrays() {}

    @SuppressWarnings("unchecked")
    public static void bind(PreparedStatement ps, int idx, Connection con, Object value) throws SQLException {
        if (value == null) {
            ps.setNull(idx, Types.ARRAY);
            return;
        }
        List<String> list = ((List<?>) value).stream().map(String::valueOf).toList();
        ps.setArray(idx, con.createArrayOf("text", list.toArray(new String[0])));
    }
}
