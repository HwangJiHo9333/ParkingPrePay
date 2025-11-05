package com.hwangjiho.parking.mapper;

import com.hwangjiho.parking.service.dto.ParkingFeeRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ParkingFeeMapper {

    /** VEHICLE_ENTRY에 존재 & 미정산(Exit NULL)건이 없으면 입고행 생성 */
    int insertEntryIfAbsent(@Param("carId") Long carId);

    /** 출고시간=NOW, 이용시간(분)= (NOW-ENTRY)*24*60 (미정산건만) */
    int updateExitAndUseMinutes(@Param("carId") Long carId);

    /** 최신 행(해당 차량의 가장 최근 FEE_ID) 조회 */
    ParkingFeeRow selectLatestRow(@Param("carId") Long carId);

    /** 무료분 업데이트 */
    int updateFreeMinutes(@Param("carId") Long carId, @Param("freeMinutes") Integer freeMinutes);

    /** 최종요금 업데이트 */
    int updateTotalFee(@Param("carId") Long carId, @Param("totalFee") Integer totalFee);
}
