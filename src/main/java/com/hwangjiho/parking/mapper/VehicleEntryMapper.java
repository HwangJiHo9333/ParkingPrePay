package com.hwangjiho.parking.mapper;

import com.hwangjiho.parking.dto.Candidate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface VehicleEntryMapper {

    // 완전 일치 우선 검색 (상위 limit)
    List<Candidate> findByFullPlate(
            @Param("plateFull") String plateFull,
            @Param("limit") int limit
    );

    // 마지막 4자리 숫자 기준 검색 (상위 limit)
    List<Candidate> findCandidatesByLast4(
            @Param("digits") String digits,
            @Param("limit") int limit
    );

    // ✅ STEP5 영수증 표시용 (번호판 조회)
    String findPlateById(@Param("carId") Long carId);
}
