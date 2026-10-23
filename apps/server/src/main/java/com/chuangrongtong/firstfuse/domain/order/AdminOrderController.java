package com.chuangrongtong.firstfuse.domain.order;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 管理端订单（M4）：
 *   GET /admin/orders/export?status=&tenant=  结算导出 CSV（UTF-8 BOM，Excel 可直接打开）
 */
@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    private static final String[] HEADERS = {
            "订单号", "租户", "类型", "标题", "金额", "币种", "状态", "创建时间", "支付时间"
    };

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) String status,
                                         @RequestParam(required = false) String tenant) {
        List<Map<String, Object>> rows = orderService.exportRows(status, tenant);

        StringBuilder sb = new StringBuilder("﻿");
        sb.append(String.join(",", HEADERS)).append("\r\n");
        for (Map<String, Object> r : rows) {
            sb.append(csv(r.get("orderNo"))).append(',')
                    .append(csv(r.get("tenantName"))).append(',')
                    .append(csv(r.get("type"))).append(',')
                    .append(csv(r.get("title"))).append(',')
                    .append(csv(r.get("amount"))).append(',')
                    .append(csv(r.get("currency"))).append(',')
                    .append(csv(r.get("status"))).append(',')
                    .append(csv(r.get("createdAt"))).append(',')
                    .append(csv(r.get("paidAt"))).append("\r\n");
        }

        String filename = "orders-" + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .body(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    /** CSV 单元格转义：含逗号/引号/换行时加双引号包裹 */
    private static String csv(Object v) {
        if (v == null) return "";
        String s = String.valueOf(v);
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return '"' + s.replace("\"", "\"\"") + '"';
        }
        return s;
    }
}
