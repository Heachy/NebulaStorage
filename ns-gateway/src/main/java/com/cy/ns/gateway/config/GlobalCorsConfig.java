package com.cy.ns.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource; // ✅ 使用 WebFlux 的类
import org.springframework.web.cors.CorsConfiguration;

@Configuration
public class GlobalCorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.addAllowedOriginPattern("*");    // ✅ 使用 allowedOriginPatterns
        corsConfig.addAllowedMethod("*");
        corsConfig.addAllowedHeader("*");
        corsConfig.setAllowCredentials(true);
        corsConfig.setMaxAge(3600L);
        corsConfig.addExposedHeader( "content-disposition" );

        // ✅ 使用 WebFlux 的 UrlBasedCorsConfigurationSource
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source); // ✅ 无需类型转换
    }
}