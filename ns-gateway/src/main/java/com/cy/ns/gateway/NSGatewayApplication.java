package com.cy.ns.gateway;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author Haechi
 * @date 2025/4/6
 */
@SpringBootApplication(scanBasePackages = "com.cy.ns.*")
@EnableDiscoveryClient
@MapperScan(basePackages = "com.cy.ns.*.mapper")
public class NSGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(NSGatewayApplication.class, args);
    }
}
