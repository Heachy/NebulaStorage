package com.cy.ns.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author Haechi
 * @date 2025/4/5
 */
@SpringBootApplication(scanBasePackages = "com.cy.ns.*" )
@MapperScan(basePackages = "com.cy.ns.*.mapper" )
@EnableDiscoveryClient
public class NebulaStorageApplication {
    public static void main(String[] args) {
        SpringApplication.run(NebulaStorageApplication.class, args);
    }
}
