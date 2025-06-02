package com.cy.ns.rabbitmq.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Haechi
 */
@Configuration
public class RabbitMqConfig {

    /**
     * 文件队列
     */
    @Bean
    public Queue fileQueue() {
        return new Queue("ns_file_queue",true,false,false,deadQueueArgs());
    }

    /**
     * 文件死信队列
     */
    @Bean
    public Queue fileDeadQueue() {
        return new Queue("ns_file_dead_queue", true, false, false);
    }


    /**
     * 文件任务交换机
     */
    @Bean
    DirectExchange fileExchange() {
        return new DirectExchange("ns_file_exchange", true, false);
    }

    /**
     * 文件死信交换机
     */
    @Bean
    DirectExchange fileDeadExchange() {
        return new DirectExchange("ns_file_dead_exchange", true, false);
    }

    /**
     * 绑定 文件队列 到 正常交换机
     */
    @Bean
    public Binding fileBinding() {
        return BindingBuilder.bind(fileQueue()).to(fileExchange()).with("delete_file");
    }

    /**
     * 绑定 文件死信队列 到 死信交换机
     */
    @Bean
    Binding deadRouteBinding() {
        return BindingBuilder.bind(fileDeadQueue())
                .to(fileDeadExchange())
                .with("dead_file");
    }

    /**
     * 转发到 死信队列，配置参数
     */
    private Map<String, Object> deadQueueArgs() {
        Map<String, Object> map = new HashMap<>(2);
        // 绑定该队列到死信交换机
        map.put("x-dead-letter-exchange", "ns_file_dead_exchange");
        map.put("x-dead-letter-routing-key", "dead_file");
        return map;
    }

}
