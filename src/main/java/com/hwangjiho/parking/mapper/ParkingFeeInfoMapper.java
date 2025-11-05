package com.hwangjiho.parking.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.hwangjiho.parking.domain.ParkingFeeInfo;

@Mapper
public interface ParkingFeeInfoMapper {
	int insertPending(ParkingFeeInfo e);

	int markPaid(@Param("feeId") Long feeId, @Param("txnId") String txnId);

	ParkingFeeInfo selectById(@Param("feeId") Long feeId);
}
