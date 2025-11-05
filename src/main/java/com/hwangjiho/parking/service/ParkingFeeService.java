package com.hwangjiho.parking.service;

import com.hwangjiho.parking.service.dto.ParkingFeeRow;

public interface ParkingFeeService {
    /** VEHICLE_ENTRY 기준 입고행 없으면 INSERT */
    void ensureEntryRowIfAbsent(Long carId);

    /** 출고시간=현재, 이용분 계산(미정산 건만) */
    void updateExitAndUseMinutes(Long carId);

    /** 최신 행(방금 선택한 건) 조회 */
    ParkingFeeRow getLatestOpenOrLastRow(Long carId);

    /** 무료분 업데이트 */
    void updateFreeMinutes(Long carId, Integer freeMinutes);

    /** 최종요금 업데이트 */
    void updateTotalFee(Long carId, Integer totalFee);
}
