package com.example.servicea.service;

import com.example.servicea.model.WeatherRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
public class WeatherClientService {
    
    @Autowired
    private WebClient weatherWebClient;
    
    public Flux<WeatherRecord> getWeatherHistory(String date, String city) {
        System.out.println("=== Сервис A: Начало запроса ===");
        System.out.println("Дата: " + date + ", Город: " + city);
        
        return weatherWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/weather/history")
                        .queryParam("date", date)
                        .queryParam("city", city)
                        .build())
                .retrieve()
                .bodyToFlux(WeatherRecord.class)
                .timeout(Duration.ofSeconds(5))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                    .filter(throwable -> {
                        System.out.println("Повторная попытка из-за: " + 
                            throwable.getClass().getSimpleName());
                        // Повторяем для ВСЕХ исключений при запросе
                        return true;
                    })
                    .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                        System.out.println("Все 3 попытки исчерпаны");
                        return retrySignal.failure();
                    }))
                .doOnError(error -> {
                    System.err.println("=== Сервис A: Финальная ошибка ===");
                    System.err.println("Тип: " + error.getClass().getSimpleName());
                    System.err.println("Сообщение: " + error.getMessage());
                })
                .onErrorResume(WebClientResponseException.class, e -> {
                    System.err.println("HTTP ошибка: " + e.getStatusCode());
                    return Flux.empty();
                })
                .onErrorResume(Exception.class, e -> {
                    System.err.println("Общая ошибка, возвращаем пустой результат");
                    return Flux.empty();
                })
                .doOnComplete(() -> {
                    System.out.println("=== Сервис A: Запрос завершен успешно ===");
                });
    }
}