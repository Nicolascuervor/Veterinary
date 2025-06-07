package com.gateway.apigateway;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class GlobalRequestLoggingFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("🌐 Gateway interceptó la solicitud");

        ServerHttpRequest request = exchange.getRequest();

        request.getHeaders().forEach((name, values) -> {
            values.forEach(value -> {
                System.out.println("🧾 Header: " + name + " = " + value);
            });
        });

        return chain.filter(exchange);
    }


    @Bean
    public GlobalFilter logRequestHeadersFilter() {
        return (exchange, chain) -> {
            System.out.println("🌐 Gateway interceptó la solicitud");
            exchange.getRequest().getHeaders().forEach((key, value) ->
                    System.out.println("🧾 Header: " + key + " = " + value));
            return chain.filter(exchange);
        };
    }

}
