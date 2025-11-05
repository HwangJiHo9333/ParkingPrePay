package com.hwangjiho.parking.service.impl;

import com.hwangjiho.parking.mapper.ParkingFeeMapper;
import com.hwangjiho.parking.service.ParkingFeeService;
import com.hwangjiho.parking.service.dto.ParkingFeeRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParkingFeeServiceImpl implements ParkingFeeService {

    private final ParkingFeeMapper parkingFeeMapper;

    @Transactional
    @Override
    public void ensureEntryRowIfAbsent(Long carId) {
        parkingFeeMapper.insertEntryIfAbsent(carId);
    }

    @Transactional
    @Override
    public void updateExitAndUseMinutes(Long carId) {
        int updated = parkingFeeMapper.updateExitAndUseMinutes(carId);
        if (updated == 0) {
            // 오픈 건이 없으면 생성 후 다시 한 번 시도(방어 로직)
            parkingFeeMapper.insertEntryIfAbsent(carId);
            parkingFeeMapper.updateExitAndUseMinutes(carId);
        }
    }

    @Override
    public ParkingFeeRow getLatestOpenOrLastRow(Long carId) {
        return parkingFeeMapper.selectLatestRow(carId);
    }

    @Transactional
    @Override
    public void updateFreeMinutes(Long carId, Integer freeMinutes) {
        parkingFeeMapper.updateFreeMinutes(carId, freeMinutes == null ? 0 : freeMinutes);
    }

    @Transactional
    @Override
    public void updateTotalFee(Long carId, Integer totalFee) {
        parkingFeeMapper.updateTotalFee(carId, totalFee == null ? 0 : totalFee);
    }
}