package com.hwangjiho.parking.dto;

import java.time.LocalDateTime;
import lombok.*;   // ← Lombok 어노테이션 사용을 위한 import

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Candidate {
    private String id;            // NUMBER → 문자열로 받아도 무방 (MyBatis가 자동 변환)
    private String plate;         // PLATE_FULL
    private LocalDateTime entryAt;// ENTRY_AT
    private String imageUrl;      // IMAGE_URL
}
