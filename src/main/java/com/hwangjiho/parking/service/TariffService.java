package com.hwangjiho.parking.service;

import org.springframework.stereotype.Service;

/**
 * 주차 요금 계산 전담 서비스.
 *
 * <p>기본 요금 정책:
 * <ul>
 *   <li>0분 이하: 0엔</li>
 *   <li>0 ~ 30분: 200엔</li>
 *   <li>31 ~ 60분: 400엔</li>
 *   <li>60분 초과: 400엔 + ceil((분-60)/30) * 150엔</li>
 * </ul>
 *
 * <p>상한 요금(캡)은 "이용시간(useMinutes)" 기준으로 적용:
 * <ul>
 *   <li>1일(≤ 1,440분): 1,500엔</li>
 *   <li>1주(≤ 10,080분): 4,500엔</li>
 *   <li>1달(≤ 43,200분): 7,500엔 (초과해도 7,500엔 유지)</li>
 * </ul>
 *
 * <p>주의: 무료시간은 외부(컨트롤러/서비스)에서 빼고 들어온
 * “과금 대상 분(billableMinutes)” 기준으로 계산합니다.
 */
@Service
public class TariffService {

    /* ===== 기본 요금 규칙 ===== */
    private static final int FEE_UP_TO_30_MIN      = 200;  // ~30분
    private static final int FEE_UP_TO_60_MIN      = 400;  // ~60분
    private static final int BLOCK_MINUTES_OVER_60 = 30;   // 60분 초과부터 30분 단위 과금
    private static final int FEE_PER_BLOCK_OVER_60 = 150;  // 30분 블록당 150엔

    /* ===== 상한(캡) 규칙 ===== */
    private static final int DAY_MIN   = 24 * 60;       // 1,440
    private static final int WEEK_MIN  = 7 * DAY_MIN;   // 10,080
    private static final int MONTH_MIN = 30 * DAY_MIN;  // 43,200 (정책상 30일 기준)

    private static final int CAP_DAY   = 1500;
    private static final int CAP_WEEK  = 4500;
    private static final int CAP_MONTH = 7500;          // 1달 초과해도 7,500엔 유지

    /**
     * 권장 메서드: 과금분과 이용시간을 함께 받아 상한을 이용시간 기준으로 적용.
     *
     * @param billableMinutes 무료시간 적용 이후의 과금 대상 분
     * @param useMinutes      전체 이용시간(입고~출고)
     * @return 최종 요금(상한 적용됨)
     */
    public int calculateFee(int billableMinutes, int useMinutes) {
        int bill = Math.max(0, billableMinutes);
        int use  = Math.max(0, useMinutes);

        int fee = rawFee(bill);
        int cap = capByUse(use);
        return Math.min(fee, cap);
    }

    /**
     * 호환용: 기존 단일 인자 버전(상한 미적용).
     * 기존 코드가 calculateFee(billMin)만 호출하던 경우를 위한 백워드 호환.
     * 가능하면 위의 이중 인자 버전으로 교체하세요.
     */
    public int calculateFee(int billableMinutes) {
        return rawFee(Math.max(0, billableMinutes));
    }

    /** 상한(캡): 이용시간 기준으로 결정. */
    private int capByUse(int useMinutes) {
        if (useMinutes <= DAY_MIN)  return CAP_DAY;
        if (useMinutes <= WEEK_MIN) return CAP_WEEK;
        // 프로젝트 정책상 1달을 초과해도 월 상한 유지
        return CAP_MONTH;
    }

    /** 상한 적용 전 기본 요금 계산. */
    private int rawFee(int minutes) {
        if (minutes <= 0)  return 0;
        if (minutes <= 30) return FEE_UP_TO_30_MIN;
        if (minutes <= 60) return FEE_UP_TO_60_MIN;

        int over   = minutes - 60;
        int blocks = ceilDiv(over, BLOCK_MINUTES_OVER_60); // 30분 단위 올림
        return FEE_UP_TO_60_MIN + blocks * FEE_PER_BLOCK_OVER_60;
    }

    /** 양수 정수 올림 나눗셈: ceil(a / b) */
    private int ceilDiv(int a, int b) {
        return (a + b - 1) / b;
    }
}
