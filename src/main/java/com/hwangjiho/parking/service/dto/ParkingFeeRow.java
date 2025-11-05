package com.hwangjiho.parking.service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ParkingFeeRow {
    private Long feeId;
    private Long vehicleId;
    private LocalDateTime entryAt;
    private LocalDateTime exitAt;
    private Integer useMinutes;
    private Integer freeMinutes;
    private Integer totalFee;
}
