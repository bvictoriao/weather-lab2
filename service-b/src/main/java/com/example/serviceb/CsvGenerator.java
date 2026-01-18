package com.example.serviceb;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class CsvGenerator implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        File csvFile = new File("weather_data.csv");
        if (csvFile.exists()) return;
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startDate = LocalDateTime.of(2025, 1, 1, 0, 0);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {
            writer.write("timestamp,city,temperature\n");
            
            for (int i = 0; i < 2500; i++) {
                for (String city : new String[]{"Moscow", "London", "Paris", "Berlin"}) {
                    BigDecimal temp = BigDecimal.valueOf(15 + Math.sin(i) * 10)
                        .setScale(2, RoundingMode.HALF_UP);
                    String timestamp = startDate.plusHours(i).format(formatter);
                    writer.write(String.format("%s,%s,%s\n", timestamp, city, temp));
                }
            }
            System.out.println("Создан CSV файл с 10000 записей: weather_data.csv");
        }
    }
}