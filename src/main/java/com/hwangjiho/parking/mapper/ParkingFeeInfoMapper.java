// com.hwangjiho.parking.mapper.ParkingFeeInfoMapper
package com.hwangjiho.parking.mapper;

import com.hwangjiho.parking.domain.ParkingFeeInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ParkingFeeInfoMapper {

	void insertPending(ParkingFeeInfo e);

	ParkingFeeInfo selectById(@Param("feeId") Long feeId);

	// STATUS 컬럼이 없으므로 사실상 selectById와 동일 동작(호환용)
	ParkingFeeInfo selectPendingById(@Param("feeId") Long feeId);

	// 차량의 최신 1건(11g: ROWNUM)
	ParkingFeeInfo selectLatestPendingByCarId(@Param("carId") Long carId);

	// STATUS/TXN_ID 컬럼 없으면 주석 처리 또는 미구현
	// void markPaid(@Param("id") Long id, @Param("txnId") String txnId);
}
