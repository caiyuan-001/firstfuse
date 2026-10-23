package com.chuangrongtong.firstfuse.common;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一分页结构，对齐前端 shared types 的 PageResult<T>：
 * { list, total, pageNum, pageSize, totalPages }
 */
public final class PageResult {

    private PageResult() {}

    public static <T> Map<String, Object> of(List<T> list, long total, int pageNum, int pageSize) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("list", list);
        m.put("total", total);
        m.put("pageNum", pageNum);
        m.put("pageSize", pageSize);
        m.put("totalPages", pageSize <= 0 ? 0 : (int) Math.ceil((double) total / pageSize));
        return m;
    }
}
