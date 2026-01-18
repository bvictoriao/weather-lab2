package com.example.serviceb.controller;

import com.example.serviceb.model.WeatherRecord;
import com.example.serviceb.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class WeatherController {
    
    @Autowired
    private WeatherService weatherService;
    
    @GetMapping("/weather/history")
    public Flux<WeatherRecord> getWeatherHistory(
            @RequestParam String date,
            @RequestParam String city) {
        return weatherService.getWeatherByDateAndCity(date, city);
    }
}