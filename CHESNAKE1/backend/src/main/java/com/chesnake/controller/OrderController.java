package com.chesnake.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chesnake.common.R;
import com.chesnake.config.SentinelConfig;
import com.chesnake.dto.PayDTO;
import com.chesnake.entity.OrderVO;
import com.chesnake.security.SecurityUtil;
import com.chesnake.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /** 提交支付：异步生成订单；QPS 20 限流 + 慢调用熔断 */
    @SentinelResource(value = SentinelConfig.RES_ORDER_PAY, blockHandler = "payBlockHandler")
    @PostMapping("/pay")
    public R<String> pay(@Valid @RequestBody PayDTO dto) {
        return R.ok("已提交，订单异步处理中", orderService.pay(SecurityUtil.currentEmail(), dto));
    }

    /** Sentinel 限流/熔断兜底 */
    public R<String> payBlockHandler(PayDTO dto, BlockException e) {
        return R.fail(429, "下单人数过多，系统繁忙，请稍后再试");
    }

    @GetMapping
    public R<Page<OrderVO>> list(@RequestParam(defaultValue = "1") long page,
                                 @RequestParam(defaultValue = "20") long size,
                                 @RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) String startDate,
                                 @RequestParam(required = false) String endDate,
                                 @RequestParam(required = false) Integer evaluated) {
        return R.ok(orderService.list(SecurityUtil.currentEmail(), page, size, keyword, startDate, endDate, evaluated));
    }
}
