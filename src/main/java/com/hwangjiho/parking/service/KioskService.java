// com.hwangjiho.parking.service.KioskService
package com.hwangjiho.parking.service;

import com.hwangjiho.parking.domain.ParkingFeeInfo;
import com.hwangjiho.parking.dto.Candidate;
import com.hwangjiho.parking.dto.FeeForm;
import com.hwangjiho.parking.mapper.ParkingFeeInfoMapper;
import com.hwangjiho.parking.mapper.VehicleEntryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KioskService {

    private final VehicleEntryMapper vehicleEntryMapper;
    private final ParkingFeeInfoMapper feeInfoMapper;

    /* step1 후보 */
    public List<Candidate> getCandidates(String plateFromStep1, int limit) {
        List<Candidate> full = vehicleEntryMapper.findByFullPlate(plateFromStep1, limit);
        if (!full.isEmpty()) return full;

        String last4 = extractLast4Digits(plateFromStep1);
        if (last4.isEmpty()) return List.of();
        return vehicleEntryMapper.findCandidatesByLast4(last4, limit);
    }

    public String extractLast4Digits(String plate) {
        if (plate == null) return "";
        String digitsOnly = plate.replaceAll("[^0-9]", "");
        if (digitsOnly.length() < 4) return digitsOnly;
        return digitsOnly.substring(digitsOnly.length() - 4);
    }

    /* step3 → step4 : PENDING 저장 */
    public ParkingFeeInfo savePendingFee(FeeForm f) {
        ParkingFeeInfo e = new ParkingFeeInfo();
        e.setVehicleId(f.getCarId());
        e.setEntryAt(parseToLdt(f.getEntryAt()));

        // ★ DB insert 시 null 저장을 허용
        e.setExitAt(null);

        e.setUseMinutes(nz(f.getUseMinutes()));
        e.setFreeMinutes(nz(f.getFreeMinutes()));

        // TOTAL_FEE 컬럼 = finalFeeYen
        Integer finalFee = (f.getFinalFeeYen() != null ? f.getFinalFeeYen() : f.getAmountYen());
        e.setFinalFeeYen(nz(finalFee));

        // ★ 결제수단 보존 (DB 컬럼이 없어도 컨트롤러에서 복구에 사용됨)
        e.setPaymentMethod(f.getPaymentMethod());

        feeInfoMapper.insertPending(e); // SEQ_PARKING_FEE로 feeId 생성
        return e;
    }

    /* 단건 조회 (step4, step5용) */
    public ParkingFeeInfo getFee(Long feeId) {
        return feeInfoMapper.selectById(feeId);
    }

    /* 뒤로가기 복구용 */
    public ParkingFeeInfo findPendingById(Long id) {
        ParkingFeeInfo r = feeInfoMapper.selectPendingById(id);
        return (r != null) ? r : feeInfoMapper.selectById(id);
    }

    /* 최신 1건 조회 (Oracle 11g ROWNUM 사용) */
    public ParkingFeeInfo findLatestPendingByCarId(Long carId) {
        return feeInfoMapper.selectLatestPendingByCarId(carId);
    }

    /* ===== helpers ===== */
    private LocalDateTime parseToLdt(String s) {
        if (s == null || s.isBlank()) return null;
        DateTimeFormatter[] fmts = new DateTimeFormatter[]{
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        };
        for (DateTimeFormatter fmt : fmts) {
            try { return LocalDateTime.parse(s, fmt); }
            catch (DateTimeParseException ignore) {}
        }
        return LocalDateTime.now();
    }

    private int nz(Integer i){
        return (i == null ? 0 : i);
    }
}
