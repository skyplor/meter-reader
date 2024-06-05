package com.skyplor.meterreader.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeterReading {
    private UUID id;
    private String nmi;
    private LocalDateTime timestamp;
    private BigDecimal consumption;
}
