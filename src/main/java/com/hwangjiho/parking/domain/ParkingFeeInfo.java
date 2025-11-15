package com.hwangjiho.parking.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ParkingFeeInfo {

	private Long feeId; // PK (SEQ_PARKING_FEE)
	private Long vehicleId; // 차량 ID

	private LocalDateTime entryAt; // 입차 시간
	private LocalDateTime exitAt; // ✅ 추가됨: 출차 시간 (Mapper XML과 일치)

	private Integer useMinutes; // 이용 시간 (분)
	private Integer freeMinutes; // 무료 적용 시간 (분)
	private Integer spendYen; // (옵션) 실제 이용비

	private Integer rawFeeYen; // 원 요금 (할인 전)
	private Integer discountFeeYen; // 할인액
	private Integer cappedAtYen; // 상한 금액
	private Integer finalFeeYen; // ✅ DB 컬럼 TOTAL_FEE와 매핑되는 최종 정산금액

	private String paymentMethod; // cash / card / free
	private String status; // PENDING / PAID / CANCEL

	private String txnId; // 거래 ID 또는 승인번호 (옵션)

	private LocalDateTime createdAt; // 생성일시
	private LocalDateTime updatedAt; // 수정일시
}
