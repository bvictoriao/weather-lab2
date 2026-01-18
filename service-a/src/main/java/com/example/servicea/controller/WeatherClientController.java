package com.example.servicea.controller;

import com.example.servicea.model.WeatherRecord;
import com.example.servicea.service.WeatherClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class WeatherClientController {
    
    @Autowired
    private WeatherClientService weatherClientService;
    
    @GetMapping("/api/weather")
    public Flux<WeatherRecord> getWeather(
            @RequestParam String date,
            @RequestParam String city) {
        
        System.out.println("\n=== Сервис A: Контроллер вызван ===");
        System.out.println("Параметры: date=" + date + ", city=" + city);
        
        return weatherClientService.getWeatherHistory(date, city);
               
    }
}