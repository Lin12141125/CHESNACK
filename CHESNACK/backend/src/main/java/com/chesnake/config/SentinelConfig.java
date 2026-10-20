package com.chesnake.config;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel 流量治理（优化9）：
 * - QPS 限流：商品详情 100 QPS / 下单支付 20 QPS（保护 DB 与 Redis）
 * - 慢调用熔断：下单接口 1s 慢调用比例超 50% 熔断 10s
 * - 规则为静态演示配置；生产可接入 Sentinel 控制台动态规则
 */
@Slf4j
@Configuration
public class SentinelConfig {

    public static final String RES_GOODS_DETAIL = "goods-detail";
    public static final String RES_ORDER_PAY = "order-pay";

    @Value("${chesnake.sentinel.api-port:8719}")
    private int apiPort;

    @PostConstruct
    public void init() {
        // 暴露 HTTP 端点（本地调试/控制台接入）
        System.setProperty("csp.sentinel.api.port", String.valueOf(apiPort));
        initFlowRules();
        initDegradeRules();
        log.info("[Sentinel] 限流熔断规则初始化完成 (api-port={})", apiPort);
    }

    private void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        FlowRule goodsDetail = new FlowRule(RES_GOODS_DETAIL);
        goodsDetail.setGrade(RuleConstant.FLOW_GRADE_QPS);
        goodsDetail.setCount(100);

        FlowRule orderPay = new FlowRule(RES_ORDER_PAY);
        orderPay.setGrade(RuleConstant.FLOW_GRADE_QPS);
        orderPay.setCount(20);

        rules.add(goodsDetail);
        rules.add(orderPay);
        FlowRuleManager.loadRules(rules);
    }

    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        // 下单接口：1s 内慢调用（>1000ms）比例超过 50% 且最小请求数 5 → 熔断 10s
        DegradeRule payDegrade = new DegradeRule(RES_ORDER_PAY);
        payDegrade.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        payDegrade.setCount(1000);
        payDegrade.setTimeWindow(10);
        payDegrade.setMinRequestAmount(5);
        payDegrade.setSlowRatioThreshold(0.5);
        rules.add(payDegrade);

        DegradeRuleManager.loadRules(rules);
    }

    /** 启用 @SentinelResource 注解拦截 */
    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }
}
