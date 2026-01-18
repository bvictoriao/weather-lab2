package com.example.servicea.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class WebClientConfig {
    
    @Bean
    public WebClient weatherWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8081")  // Сервис B
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }
    
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            System.out.println("\n=== Сервис A: Исходящий запрос ===");
            System.out.println("Время: " + java.time.LocalTime.now());
            System.out.println("Метод: " + clientRequest.method());
            System.out.println("URL: " + clientRequest.url());
            System.out.println("Заголовки: " + clientRequest.headers());
            System.out.println("===================================");
            return Mono.just(clientRequest);
        });
    }
    
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            System.out.println("\n=== Сервис A: Получен ответ ===");
            System.out.println("Время: " + java.time.LocalTime.now());
            System.out.println("Статус: " + clientResponse.statusCode());
            System.out.println("Заголовки: " + clientResponse.headers().asHttpHeaders());
            System.out.println("===============================");
            return Mono.just(clientResponse);
        });
    }
}