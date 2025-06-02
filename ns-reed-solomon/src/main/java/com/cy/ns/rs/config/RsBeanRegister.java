package com.cy.ns.rs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author Haechi
 */
@Component
public class RsBeanRegister {
    @Bean
    @ConfigurationProperties(prefix = "jsr.coding")
    public CodingConfig getCodingConfig() {
        return new CodingConfig();
    }
}
