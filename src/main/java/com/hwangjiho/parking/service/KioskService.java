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

    private final VehicleEntryMapper vehicleEntryMapper;   // 기존 후보 조회용
    private final ParkingFeeInfoMapper feeInfoMapper;      // 정산 저장/조회용

    // ----------------------------
    // 1) Step1 후보 조회 로직 (기존 그대로)
    // ----------------------------
    // step1에서 넘어온 완전 문자열(예: "品川 300 あ 12-34")을 그대로 먼저 시도,
    // 없으면 마지막 4자리로 fallback
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

    // ----------------------------
    // 2) Step3 → Step4: PENDING 저장
    // ----------------------------
    public ParkingFeeInfo savePendingFee(FeeForm f) {
        ParkingFeeInfo e = new ParkingFeeInfo();
        e.setVehicleId(f.getCarId());
        e.setEntryAt(parseToLdt(f.getEntryAt()));

        e.setUseMinutes(nz(f.getUseMinutes()));
        e.setFreeMinutes(nz(f.getFreeMinutes()));
        e.setSpendYen(nz(f.getSpendYen()));

        e.setRawFeeYen(nz(f.getRawFeeYen()));
        e.setDiscountFeeYen(nz(f.getDiscountFeeYen()));
        e.setCappedAtYen(nz(f.getCappedAtYen()));

        // 최종 결제금액: finalFeeYen 우선, 없으면 amountYen(호환)
        Integer finalFee = f.getFinalFeeYen() != null ? f.getFinalFeeYen() : f.getAmountYen();
        e.setFinalFeeYen(nz(finalFee));

        e.setPaymentMethod(f.getPaymentMethod()); // "cash" | "card" | "free"
        e.setStatus("PENDING");

        feeInfoMapper.insertPending(e); // Oracle selectKey로 feeId 채워짐
        return e;
    }

    // ----------------------------
    // 3) Step4 → Step5: 결제 완료 처리
    // ----------------------------
    public void markPaid(Long feeId, String txnId) {
        feeInfoMapper.markPaid(feeId, txnId);
    }

    // ----------------------------
    // 4) 정산 단건 조회 (step4/5 화면 표시 등)
    // ----------------------------
    public ParkingFeeInfo getFee(Long feeId) {
        return feeInfoMapper.selectById(feeId);
    }

    // ----------------------------
    // helpers
    // ----------------------------
    private LocalDateTime parseToLdt(String s) {
        if (s == null || s.isBlank()) return null;
        DateTimeFormatter[] fmts = new DateTimeFormatter[] {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        };
        for (DateTimeFormatter fmt : fmts) {
            try { return LocalDateTime.parse(s, fmt); }
            catch (DateTimeParseException ignore) {}
        }
        // 포맷이 맞지 않으면 현재시각으로 대체 (필요 시 예외로 변경)
        return LocalDateTime.now();
    }

    private int nz(Integer i){ return i == null ? 0 : i; }
}
