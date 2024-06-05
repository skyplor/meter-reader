package com.skyplor.meterreader.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.skyplor.meterreader.model.MeterReading;

import jakarta.annotation.PreDestroy;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class MeterReadingService {

    private static final Logger logger = LoggerFactory.getLogger(MeterReadingService.class);

    private final ExecutorService executorService = Executors
            .newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
    }

    public Future<String> processFileAsync(Path tempFile) {
        return executorService.submit(() -> {
            try {
                String sqlStatement = processFile(tempFile.toString());
                Files.delete(tempFile);
                return sqlStatement;
            } catch (IOException e) {
                logger.error("Error processing file: " + e.getMessage());
                return "";
            }
        });
    }

    private String processFile(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            return processLines(lines);
        } catch (IOException e) {
            logger.error("Error reading file: {}", filePath, e);
            return "";
        }
    }

    private String processLines(List<String> lines) {
        Map<String, MeterReading> meterReadingMap = Maps.newHashMap();
        Set<String> duplicateCheckSet = Sets.newHashSet();
        String currentNmi = null;
        String currentNmiSuffix = null;
        int intervalLength = 0;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm");

        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts[0].equals("200")) {
                currentNmi = parts[1];
                currentNmiSuffix = parts[4];
                intervalLength = Integer.parseInt(parts[8]);
            } else if (parts[0].equals("300") && currentNmi != null) {
                String date = parts[1];
                for (int i = 2; i < parts.length; i++) {
                    try {
                        BigDecimal consumption = new BigDecimal(parts[i]);
                        if (consumption.compareTo(BigDecimal.ZERO) != 0) {
                            int intervalNumber = i - 2;
                            int hours = intervalNumber * intervalLength / 60;
                            int minutes = (intervalNumber * intervalLength) % 60;
                            LocalDateTime timestamp = LocalDateTime
                                    .parse(date + " " + String.format("%02d:%02d", hours, minutes), formatter);

                            String meterReadingMapKey = currentNmi + "-" + timestamp + "-" + intervalNumber;
                            String duplicateCheckKey = meterReadingMapKey + "-" + currentNmiSuffix;
                            if (duplicateCheckSet.contains(duplicateCheckKey)) {
                                throw new IllegalArgumentException("Duplicate record found for NMI: " + currentNmi + ", Timestamp: " + timestamp + ", NmiSuffix: " + currentNmiSuffix + ", IntervalNumber: " + intervalNumber);
                            }
                            duplicateCheckSet.add(duplicateCheckKey);
                            MeterReading meterReading = meterReadingMap.getOrDefault(meterReadingMapKey, new MeterReading());
                            if (meterReading.getNmi() == null) {
                                meterReading.setNmi(currentNmi);
                                meterReading.setTimestamp(timestamp);
                                meterReading.setConsumption(BigDecimal.ZERO);
                            }
                            meterReading.setConsumption(meterReading.getConsumption().add(consumption));
                            meterReadingMap.put(meterReadingMapKey, meterReading);
                        }
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid consumption value: {}", parts[i], e);
                    }
                }
            }
        }

        return generateInsertStatement(meterReadingMap);
    }

    private String generateInsertStatement(Map<String, MeterReading> meterReadingMap) {
        if (!meterReadingMap.isEmpty()) {
            StringBuilder sqlBuilder = new StringBuilder("INSERT INTO meter_readings (nmi, timestamp, consumption) VALUES ");
            boolean first = true;
            for (MeterReading meterReading : meterReadingMap.values()) {
                if (!first) {
                    sqlBuilder.append(", ");
                } else {
                    first = false;
                }
                sqlBuilder.append(String.format(
                        "('%s', '%s', %s)",
                        meterReading.getNmi(),
                        meterReading.getTimestamp().toString(),
                        meterReading.getConsumption().toString()
                ));
            }
            sqlBuilder.append(";");
            return sqlBuilder.toString();
        }
        return "";
    }
}