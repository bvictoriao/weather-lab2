package com.example.serviceb.service;

import com.example.serviceb.model.WeatherRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class WeatherService {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final String csvFilePath = "weather_data.csv";
    
    public WeatherService() {
        createCSVFileIfNotExists();
        checkCSVFileContent(); // Проверка CSV файла
    }
    
    private void createCSVFileIfNotExists() {
        try {
            Path path = Paths.get(csvFilePath);
            if (!Files.exists(path)) {
                List<String> lines = new ArrayList<>();
                LocalDateTime startDate = LocalDateTime.of(2025, 1, 1, 0, 0);
                
                lines.add("timestamp,city,temperature");
                
                for (int i = 0; i < 2500; i++) {
                    for (String city : new String[]{"Moscow", "London", "Paris", "Berlin"}) {
                        BigDecimal temp = BigDecimal.valueOf(15 + Math.sin(i) * 10)
                            .setScale(2, RoundingMode.HALF_UP);
                        String timestamp = startDate.plusHours(i).format(formatter);
                        lines.add(timestamp + "," + city + "," + temp);
                    }
                }
                
                Files.write(path, lines);
                System.out.println("Создан CSV файл с 10000 записей: " + csvFilePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Проверка CSV файла
    private void checkCSVFileContent() {
        try {
            System.out.println("=== ПРОВЕРКА CSV ФАЙЛА ===");
            System.out.println("Путь: " + Paths.get(csvFilePath).toAbsolutePath());
            
            BufferedReader reader;
            try {
                reader = new BufferedReader(new InputStreamReader(
                    Files.newInputStream(Paths.get(csvFilePath))));
                System.out.println("Источник: файловая система");
            } catch (Exception e) {
                reader = new BufferedReader(new InputStreamReader(
                    new ClassPathResource(csvFilePath).getInputStream()));
                System.out.println("Источник: ресурсы");
            }
            
            String line;
            int lineCount = 0;
            List<String> firstFive = new ArrayList<>();
            List<String> moscowRecords = new ArrayList<>();
            
            while ((line = reader.readLine()) != null) {
                lineCount++;
                
                if (lineCount == 1) {
                    System.out.println("Заголовок: " + line);
                    continue;
                }
                
                if (lineCount <= 6) {
                    firstFive.add(line);
                }
                
                if (lineCount > 10000 - 5 && lineCount <= 10000) {
                    // Последние 5 записей
                }
                
                // Проверяем записи Москвы за 2025-01-15
                if (line.contains("Moscow") && line.contains("2025-01-15")) {
                    moscowRecords.add(line);
                }
            }
            reader.close();
            
            System.out.println("\nПервые 5 записей:");
            for (int i = 0; i < Math.min(5, firstFive.size()); i++) {
                System.out.println("  " + (i+1) + ": " + firstFive.get(i));
            }
            
            System.out.println("\nЗаписи Москвы за 2025-01-15:");
            if (moscowRecords.isEmpty()) {
                System.out.println("  Не найдено записей!");
            } else {
                for (int i = 0; i < moscowRecords.size(); i++) {
                    System.out.println("  " + (i+1) + ": " + moscowRecords.get(i));
                }
            }
            
            System.out.println("\nСтатистика:");
            System.out.println("  Всего строк в CSV: " + lineCount);
            System.out.println("  Записей данных: " + (lineCount - 1));
            System.out.println("  Записей Москвы 2025-01-15: " + moscowRecords.size());
            System.out.println("  Ожидается: 24 записи (0-23 часа)");
            System.out.println("======================================");
            
        } catch (Exception e) {
            System.err.println("Ошибка при проверке CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Неоптимальная функция: вычисляет среднее, но не использует его
    private BigDecimal calculateUselessAverage(List<WeatherRecord> records) {
        if (records.isEmpty()) return BigDecimal.ZERO;
        
        BigDecimal sum = BigDecimal.ZERO;
        for (WeatherRecord record : records) {
            sum = sum.add(record.getTemperature());
        }
        
        BigDecimal average = sum.divide(
            BigDecimal.valueOf(records.size()), 
            2, 
            RoundingMode.HALF_UP
        );
        
        System.out.println("Вычислено среднее (не используется): " + average);
        return average;
    }
    
    // Главный неоптимальный метод: читает ВЕСЬ CSV при каждом запросе
    public Flux<WeatherRecord> getWeatherByDateAndCity(String date, String city) {
        long startTime = System.currentTimeMillis();
        List<WeatherRecord> allRecords = new ArrayList<>();
        List<WeatherRecord> filteredRecords = new ArrayList<>();
        
        try {
            // Отладочная информация
            System.out.println("\n=== ОБРАБОТКА ЗАПРОСА ===");
            System.out.println("Дата: " + date + ", Город: " + city);
            
            LocalDateTime targetDate = LocalDateTime.parse(date + "T00:00:00");
            LocalDateTime nextDay = targetDate.plusDays(1);
            
            System.out.println("Диапазон поиска: от " + targetDate + " до " + nextDay);
            
            // 1. ЧТЕНИЕ ВСЕГО CSV ФАЙЛА (неоптимально - при каждом запросе)
            BufferedReader reader;
            try {
                reader = new BufferedReader(new InputStreamReader(
                    Files.newInputStream(Paths.get(csvFilePath))));
                System.out.println("Чтение CSV из файловой системы: " + Paths.get(csvFilePath).toAbsolutePath());
            } catch (Exception e) {
                reader = new BufferedReader(new InputStreamReader(
                    new ClassPathResource(csvFilePath).getInputStream()));
                System.out.println("Чтение CSV из ресурсов");
            }
            
            String line;
            boolean firstLine = true;
            int recordsRead = 0;
            
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                
                recordsRead++;
                
                // 2. ПАРСИНГ КАЖДОЙ СТРОКИ (O(n))
                String[] parts = line.split(",");
                if (parts.length != 3) continue;
                
                LocalDateTime timestamp = LocalDateTime.parse(parts[0], formatter);
                String recordCity = parts[1];
                BigDecimal temperature = new BigDecimal(parts[2]); // BigDecimal - неоптимально
                
                WeatherRecord record = new WeatherRecord(timestamp, recordCity, temperature);
                allRecords.add(record);
                
                // 3. ФИЛЬТРАЦИЯ ПРИ ПАРСИНГЕ (исправленная логика)
                if (recordCity.equalsIgnoreCase(city)) {
                    // ВКЛЮЧАЕМ записи, начиная с 00:00:00 запрошенного дня
                    if (!timestamp.isBefore(targetDate) && // timestamp >= targetDate
                        timestamp.isBefore(nextDay)) {     // timestamp < nextDay
                        
                        filteredRecords.add(record);
                       // System.out.println("  Найдена запись: " + timestamp + " - " + temperature);
                    }
                }
            }
            reader.close();

            System.out.println("Начало обработки: " + System.currentTimeMillis());
// ... обработка ...
System.out.println("Конец обработки: " + System.currentTimeMillis());
            
            // 4. ДУБЛИРУЮЩИЙ РАСЧЁТ СРЕДНЕГО (вызывается 2 раза)
            calculateUselessAverage(allRecords);
            calculateUselessAverage(allRecords); // Дублирующий вызов
            
            // 5. НЕНУЖНАЯ СОРТИРОВКА (данные уже отсортированы в CSV)
            filteredRecords.sort(Comparator.comparing(WeatherRecord::getTimestamp));
            
            long endTime = System.currentTimeMillis();
            System.out.println("Итоги обработки:");
            System.out.println("  Время обработки: " + (endTime - startTime) + " мс");
            System.out.println("  Прочитано записей из CSV: " + recordsRead);
            System.out.println("  Найдено записей после фильтрации: " + filteredRecords.size());
            System.out.println("=== ЗАВЕРШЕНИЕ ОБРАБОТКИ ===\n");
            
        } catch (Exception e) {
            e.printStackTrace();
            return Flux.error(e);
        }
        
        return Flux.fromIterable(filteredRecords);
    }
}