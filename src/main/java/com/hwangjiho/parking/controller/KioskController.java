package com.hwangjiho.parking.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hwangjiho.parking.dto.Candidate;
import com.hwangjiho.parking.service.KioskService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class KioskController {

    private final KioskService kioskService;

    // STEP1: 번호판 입력
    @GetMapping("/kiosk/step1")
    public String step1() {
        return "kiosk/step1";
    }

    // STEP2: 후보 차량 리스트
    @GetMapping("/kiosk/step2")
    public String step2(@RequestParam String plate, Model model) {
        List<Candidate> candidates = kioskService.getCandidates(plate, 4);
        model.addAttribute("plate", plate);
        model.addAttribute("candidates", candidates);
        return "kiosk/step2";
    }

    // STEP3: 후보 차량 확정(결제수단 선택 화면)
    @GetMapping("/kiosk/step3")
    public String step3(@RequestParam String candidateId,
                        @RequestParam String plate,
                        Model model) {
        model.addAttribute("candidateId", candidateId);
        model.addAttribute("plate", plate);
        return "kiosk/step3"; // 결제수단 선택 화면(현금/카드 등)
    }

    // STEP4: 결제 입력 화면 (현금 시뮬레이션 입력 등)
    @GetMapping("/kiosk/step4")
    public String step4(@RequestParam String candidateId,
                        @RequestParam String plate,
                        @RequestParam String payMethod,   // "cash" 또는 "card" 등
                        Model model) {
        model.addAttribute("candidateId", candidateId);
        model.addAttribute("plate", plate);
        model.addAttribute("payMethod", payMethod);
        return "kiosk/step4"; // 현금 입력 폼(간단)
    }

    // STEP4 → STEP5: 결제 완료로 이동 (액션 처리)
    @PostMapping("/kiosk/step4/complete")
    public String step4Complete(@RequestParam String candidateId,
                                @RequestParam String plate,
                                @RequestParam String payMethod,
                                @RequestParam(required = false) Integer paidAmount, // 현금일 때만 사용
                                RedirectAttributes ra) {
        // TODO: 필요 시 결제 검증/로직 추가 (지금은 네비게이션만)
        ra.addAttribute("candidateId", candidateId);
        ra.addAttribute("plate", plate);
        ra.addAttribute("payMethod", payMethod);
        // 예: 총액, 거스름돈 등을 계산해 전달하고 싶다면 아래 처럼 추가
        // ra.addAttribute("totalAmount", 10000);
        // ra.addAttribute("change", (paidAmount != null ? paidAmount - 10000 : 0));
        return "redirect:/kiosk/step5";
    }

    // STEP5: 정산 완료 + 영수증 버튼
    @GetMapping("/kiosk/step5")
    public String step5(@RequestParam String candidateId,
                        @RequestParam String plate,
                        @RequestParam String payMethod,
                        Model model) {
        model.addAttribute("candidateId", candidateId);
        model.addAttribute("plate", plate);
        model.addAttribute("payMethod", payMethod);
        return "kiosk/step5";
    }

    // STEP5에서 "영수증 발행"을 눌렀을 때(화면에 영수증 보여주기)
    @GetMapping("/kiosk/step5/receipt")
    public String step5Receipt(@RequestParam String candidateId,
                               @RequestParam String plate,
                               @RequestParam String payMethod,
                               Model model) {
        model.addAttribute("candidateId", candidateId);
        model.addAttribute("plate", plate);
        model.addAttribute("payMethod", payMethod);
        return "kiosk/receipt"; // 영수증 화면
    }
}
