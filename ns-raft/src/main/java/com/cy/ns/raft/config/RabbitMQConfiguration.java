package com.cy.ns.raft.config;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.context.Lifecycle;
import org.springframework.stereotype.Component;

/**
 * @author Haechi
 * @date 2025/4/29
 */
@Component
@Slf4j
public class RabbitMQConfiguration {



    @Resource
    private RabbitListenerEndpointRegistry registry;


    public void stopListeners() {
        registry.getListenerContainers().forEach( Lifecycle::stop );
        log.info( "RabbitMQ listeners stopped" );
    }

    public void startListeners() {
        registry.getListenerContainers().forEach( Lifecycle::start );
        log.info( "RabbitMQ listeners started" );
    }

}
