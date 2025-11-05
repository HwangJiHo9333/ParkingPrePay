package com.hwangjiho.parking.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ParkingFeeInfo {
    private Long feeId;
    private Long vehicleId;

    private LocalDateTime entryAt;
    private Integer useMinutes;
    private Integer freeMinutes;
    private Integer spendYen;

    private Integer rawFeeYen;
    private Integer discountFeeYen;
    private Integer cappedAtYen;
    private Integer finalFeeYen;

    private String paymentMethod;  // cash/card/free
    private String status;         // PENDING/PAID/CANCEL

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String txnId;          // (옵션) 카드 승인번호 등
}
