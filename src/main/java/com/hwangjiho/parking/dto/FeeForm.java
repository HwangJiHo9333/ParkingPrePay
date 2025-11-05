package com.hwangjiho.parking.dto;

import lombok.Data;

@Data
public class FeeForm {
    private Long carId;

    // 문자열로 넘어오는 입고시각 (예: "2025-10-29 13:20")
    private String entryAt;

    private Integer useMinutes;
    private Integer freeMinutes;
    private Integer spendYen;

    private Integer rawFeeYen;       // 총 이용요금(무료전)
    private Integer discountFeeYen;  // 무료시간에 해당하는 요금
    private Integer cappedAtYen;     // 캡 요금
    private Integer finalFeeYen;     // 최종 결제금액

    private String paymentMethod;    // "cash" | "card"
    private String status;           // "PENDING" 로 옴
    private Integer amountYen;       // (호환용) 총액 필드가 따로 넘어올 수도 있어 유지
}
