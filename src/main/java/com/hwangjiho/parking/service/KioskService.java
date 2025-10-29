package com.hwangjiho.parking.service;

import com.hwangjiho.parking.dto.Candidate;
import com.hwangjiho.parking.mapper.VehicleEntryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KioskService {

    private final VehicleEntryMapper mapper;

    // step1에서 넘어온 완전 문자열(예: "品川 300 あ 12-34")을 그대로 먼저 시도,
    // 없으면 마지막 4자리로 fallback
    public List<Candidate> getCandidates(String plateFromStep1, int limit) {
        List<Candidate> full = mapper.findByFullPlate(plateFromStep1, limit);
        if (!full.isEmpty()) return full;

        String last4 = extractLast4Digits(plateFromStep1);
        if (last4.isEmpty()) return List.of();
        return mapper.findCandidatesByLast4(last4, limit);
    }

    public String extractLast4Digits(String plate) {
        if (plate == null) return "";
        String digitsOnly = plate.replaceAll("[^0-9]", "");
        if (digitsOnly.length() < 4) return digitsOnly;
        return digitsOnly.substring(digitsOnly.length() - 4);
    }
}
