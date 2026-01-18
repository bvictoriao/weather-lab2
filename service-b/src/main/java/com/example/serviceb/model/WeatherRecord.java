package com.example.serviceb.model;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WeatherRecord {
    private LocalDateTime timestamp;
    private String city;
    private BigDecimal temperature;
    
    public WeatherRecord() {}
    public WeatherRecord(LocalDateTime timestamp, String city, BigDecimal temperature) {
        this.timestamp = timestamp;
        this.city = city;
        this.temperature = temperature;
    }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public BigDecimal getTemperature() { return temperature; }
    public void setTemperature(BigDecimal temperature) { this.temperature = temperature; }
}