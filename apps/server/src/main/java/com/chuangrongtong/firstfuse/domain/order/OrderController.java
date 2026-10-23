package com.chuangrongtong.firstfuse.domain.order;

import com.chuangrongtong.firstfuse.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 订单端点，对齐 mock/modules/order.ts：
 *   GET /orders        列表（orderNo/tenant/status/分页）
 *   GET /orders/:id    详情（:id = orderNo，缺省 {}）
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String tenant,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(orderService.list(orderNo, tenant, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getById(@PathVariable String id) {
        return ApiResponse.success(orderService.getById(id));
    }
}
