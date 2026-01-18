package com.example.serviceb.filter;

import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(1)
public class RequestLoggingFilter implements WebFilter {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        System.out.println("=== Сервис B: Входящий запрос ===");
        System.out.println("Метод: " + request.getMethod());
        System.out.println("URL: " + request.getURI());
        System.out.println("Параметры: " + request.getQueryParams());
        System.out.println("=================================");
        
        return chain.filter(exchange);
    }
}