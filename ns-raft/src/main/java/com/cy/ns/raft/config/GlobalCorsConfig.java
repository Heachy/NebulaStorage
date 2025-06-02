//package com.cy.ns.raft.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//import org.springframework.web.filter.CorsFilter;
//
///**
// * 网关处已做跨域处理
// */
//@Configuration
//public class GlobalCorsConfig {
////    @Bean
////    public CorsFilter corsFilter() {
////        CorsConfiguration config = new CorsConfiguration();
////        config.setAllowCredentials(true);
////        // 使用 allowedOriginPatterns 代替 allowedOrigins
////        config.addAllowedOriginPattern("*"); // 允许所有来源（生产环境应指定具体域名）
////        config.addAllowedMethod("*");        // 允许所有方法
////        config.addAllowedHeader("*");        // 允许所有头
////
////        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
////        configSource.registerCorsConfiguration("/**", config);
////        return new CorsFilter(configSource);
////    }
//
//}