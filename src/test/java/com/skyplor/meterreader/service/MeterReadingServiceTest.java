package com.skyplor.meterreader.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class MeterReadingServiceTest {

    @Autowired
    private MeterReadingService service;

    @Test
    void testDuplicatedNmiRecord() throws IOException, ExecutionException, InterruptedException {
        // Create a temp file with sample data
        Path tempFile = Files.createTempFile("test", ".csv");
        Files.write(tempFile, Arrays.asList(
            "200,NEM1201009,E1E2,1,E1,N1,01009,kWh,30,20050610",
            "300,20050301,0,0,0,0,0,0,0,0,0,0,0.461,0.810,0.568,1.234,1.353,1.507,1.344,1.773,0",
            "200,NEM1201009,E1E2,1,E1,N1,01009,kWh,30,20050610",  // Duplicate NMI
            "300,20050301,0,0,0,0,0,0,0,0,0,0,0.461,0.810,0.568,1.234,1.353,1.507,1.344,1.773,0"  // Duplicate record
        ));

        Future<String> future = service.processFileAsync(tempFile);

        ExecutionException executionException = assertThrows(ExecutionException.class, future::get);
        Throwable cause = executionException.getCause();
        assertTrue(cause instanceof IllegalArgumentException);
        assertEquals("Duplicate record found for NMI: NEM1201009, Timestamp: 2005-03-01T05:00, NmiSuffix: E1, IntervalNumber: 10", cause.getMessage());
    }

    @Test
    void testNmiRecordWithMultipleRegisters() throws IOException, ExecutionException, InterruptedException {
        // Create a temp file with sample data
        Path tempFile = Files.createTempFile("test", ".csv");
        Files.write(tempFile, Arrays.asList(
            "200,NEM1201009,E1E2,1,E1,N1,01009,kWh,30,20050610",
            "300,20050301,0,0,0,0,0,0,0,0,0,0,0.461,0.810,0.568,1.234,1.353,1.507,1.344,1.773,0",
            "200,NEM1201009,E1E2,2,E2,N1,01009,kWh,30,20050610",
            "300,20050301,0,0,0,0,0,0,0,0,0,0,0.461,0.810,0.568,1.234,1.353,1.507,1.344,1.773,0"
        ));

        String sqlStatement = service.processFileAsync(tempFile).get();

        assertEquals("INSERT INTO meter_readings (nmi, timestamp, consumption) VALUES ('NEM1201009', '2005-03-01T06:00', 1.136), ('NEM1201009', '2005-03-01T08:30', 3.546), ('NEM1201009', '2005-03-01T05:00', 0.922), ('NEM1201009', '2005-03-01T05:30', 1.620), ('NEM1201009', '2005-03-01T06:30', 2.468), ('NEM1201009', '2005-03-01T08:00', 2.688), ('NEM1201009', '2005-03-01T07:30', 3.014), ('NEM1201009', '2005-03-01T07:00', 2.706);", sqlStatement);
    }

    @Test
    void testGenerateBulkSqlStatement() throws IOException, ExecutionException, InterruptedException {
        // Create a temp file with sample data
        Path tempFile = Files.createTempFile("test", ".csv");
        Files.write(tempFile, Arrays.asList(
                "200,NEM1201009,E1E2,1,E1,N1,01009,kWh,30,20050610",
                "300,20050301,0,0,0,0,0,0,0,0,0,0,0,0,0.461,0.810,0.568,1.234,1.353,1.507,1.344,1.773,0"
        ));

        String sqlStatement = service.processFileAsync(tempFile).get();

        assertFalse(sqlStatement.isEmpty());
        assertEquals("INSERT INTO meter_readings (nmi, timestamp, consumption) VALUES ('NEM1201009', '2005-03-01T06:00', 0.461), ('NEM1201009', '2005-03-01T09:30', 1.773), ('NEM1201009', '2005-03-01T08:30', 1.507), ('NEM1201009', '2005-03-01T06:30', 0.810), ('NEM1201009', '2005-03-01T08:00', 1.353), ('NEM1201009', '2005-03-01T07:30', 1.234), ('NEM1201009', '2005-03-01T07:00', 0.568), ('NEM1201009', '2005-03-01T09:00', 1.344);", sqlStatement);
    }
}
