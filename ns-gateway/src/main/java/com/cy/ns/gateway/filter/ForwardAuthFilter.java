package com.cy.ns.gateway.filter;

import cn.dev33.satoken.reactor.context.SaReactorSyncHolder;
import cn.dev33.satoken.same.SaSameUtil;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequest.Builder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author Haechi
 * @date 2025/4/9
 * 全局过滤器，为请求添加 Same-Token
 */
@Component
public class ForwardAuthFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = SaSameUtil.getToken();
        Builder requestBuilder = exchange
                .getRequest()
                .mutate()
                // 为请求追加 Same-Token 参数
                .header( SaSameUtil.SAME_TOKEN, token );
        if (exchange.getRequest().getHeaders().get( StpUtil.getTokenName() )==null){
            String tokenValue;
            try {
                tokenValue = StpUtil.getTokenValue();
            } catch (Exception e) {
                SaReactorSyncHolder.setContext(exchange);
                tokenValue = StpUtil.getTokenValue();
            }
            requestBuilder.header( StpUtil.getTokenName(),tokenValue );
        }
        ServerHttpRequest newRequest = requestBuilder.build();
        ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();
        return chain.filter(newExchange);
    }
}
