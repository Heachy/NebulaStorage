package com.cy.ns.web.service.impl;

import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * @author Haechi
 */
@Service
public class DeleteFileQueService {

    private final RabbitTemplate rabbitTemplate;

    public DeleteFileQueService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     *  1: 定义交换机
     */
    private final String exchangeName = "ns_file_exchange";
    /**
     *  2: 路由key
     */
    private final String routeKey = "delete_file";

    public void putMsg(String msg, int time) {
        // 对消息设置过期时间
        MessagePostProcessor messagePostProcessor = message -> {
            message.getMessageProperties().setExpiration(String.valueOf(time));
            return message;
        };
        rabbitTemplate.convertAndSend(exchangeName,routeKey, msg,messagePostProcessor);
    }

    public Object getMsg(){
        return  rabbitTemplate.receiveAndConvert("pt_queue");
    }
}
