package com.chesnake.config;

import com.chesnake.mq.MqConfirmCallback;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 队列/交换机/绑定 + JSON 序列化 + 生产者可靠性
 * - 所有业务队列配置死信交换机：处理失败（Nack 不重入队）进入死信队列
 * - RabbitTemplate 开启 Publisher Confirm / Returns：
 *   Confirm 回调更新本地消息表（mq_outbox），未确认由补偿任务重投
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "chesnake.exchange";

    public static final String ORDER_QUEUE = "chesnake.order.pay.queue";
    public static final String NOTIFY_QUEUE = "chesnake.notify.queue";
    public static final String COMMUNITY_QUEUE = "chesnake.community.event.queue";
    public static final String AUDIT_QUEUE = "chesnake.audit.log.queue";

    public static final String ORDER_ROUTING_KEY = "order.pay";
    public static final String NOTIFY_ROUTING_KEY = "notify.user";
    public static final String COMMUNITY_ROUTING_KEY = "community.event";
    public static final String AUDIT_ROUTING_KEY = "admin.audit";

    public static final String DEAD_EXCHANGE = "chesnake.dead.exchange";
    public static final String DEAD_QUEUE = "chesnake.dead.queue";
    public static final String DEAD_ROUTING_KEY = "dead";

    /** 死信重投次数上限（超过后落补偿表） */
    public static final int DEAD_RETRY_LIMIT = 3;
    /** 自定义重试计数消息头 */
    public static final String RETRY_COUNT_HEADER = "x-chesnake-retry";

    @Bean
    public DirectExchange chesnakeExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange deadExchange() {
        return new DirectExchange(DEAD_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable(ORDER_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue notifyQueue() {
        return QueueBuilder.durable(NOTIFY_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue communityQueue() {
        return QueueBuilder.durable(COMMUNITY_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue auditQueue() {
        return QueueBuilder.durable(AUDIT_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue deadQueue() {
        return QueueBuilder.durable(DEAD_QUEUE).build();
    }

    @Bean
    public Binding orderBinding(Queue orderQueue, DirectExchange chesnakeExchange) {
        return BindingBuilder.bind(orderQueue).to(chesnakeExchange).with(ORDER_ROUTING_KEY);
    }

    @Bean
    public Binding notifyBinding(Queue notifyQueue, DirectExchange chesnakeExchange) {
        return BindingBuilder.bind(notifyQueue).to(chesnakeExchange).with(NOTIFY_ROUTING_KEY);
    }

    @Bean
    public Binding communityBinding(Queue communityQueue, DirectExchange chesnakeExchange) {
        return BindingBuilder.bind(communityQueue).to(chesnakeExchange).with(COMMUNITY_ROUTING_KEY);
    }

    @Bean
    public Binding auditBinding(Queue auditQueue, DirectExchange chesnakeExchange) {
        return BindingBuilder.bind(auditQueue).to(chesnakeExchange).with(AUDIT_ROUTING_KEY);
    }

    @Bean
    public Binding deadBinding(Queue deadQueue, DirectExchange deadExchange) {
        return BindingBuilder.bind(deadQueue).to(deadExchange).with(DEAD_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 生产可靠性 RabbitTemplate：
     * 开启 Publisher Confirm（mandatory=true），
     * Confirm 回调更新 mq_outbox 状态，不可路由消息由 Returns 回调记录。
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter jacksonMessageConverter,
                                         MqConfirmCallback confirmCallback) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jacksonMessageConverter);
        template.setConfirmCallback(confirmCallback);
        template.setMandatory(true);
        template.setReturnsCallback(returned -> {
            // 不可路由：messageId 在发送时写入消息属性，据此更新本地消息表状态
            String msgId = returned.getMessage().getMessageProperties().getMessageId();
            if (msgId != null) {
                confirmCallback.confirm(new org.springframework.amqp.rabbit.connection.CorrelationData(msgId), false,
                        "不可路由: " + returned.getReplyText());
            }
        });
        return template;
    }
}
