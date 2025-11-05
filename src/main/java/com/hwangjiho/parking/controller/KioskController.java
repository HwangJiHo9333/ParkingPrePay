package com.hwangjiho.parking.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.hwangjiho.parking.domain.ParkingFeeInfo;
import com.hwangjiho.parking.dto.Candidate;
import com.hwangjiho.parking.dto.FeeForm;
import com.hwangjiho.parking.mapper.VehicleEntryMapper;
import com.hwangjiho.parking.service.KioskService;
import com.hwangjiho.parking.service.ParkingFeeService;
import com.hwangjiho.parking.service.TariffService;
import com.hwangjiho.parking.service.dto.ParkingFeeRow;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class KioskController {

	private final KioskService kioskService; // STEP1/2 후보 + PENDING/PAID
	private final ParkingFeeService parkingFeeService; // 입출차/시간/요금 누적 테이블
	private final TariffService tariffService; // 요금 계산
	private final VehicleEntryMapper vehicleEntryMapper; // 번호판 조회

	/* 루트 → step1 */
	@GetMapping({ "/", "/index" })
	public String index() {
		return "redirect:/kiosk/step1";
	}

	/* STEP1 */
	@GetMapping("/kiosk/step1")
	public String step1() {
		return "kiosk/step1";
	}

	/* STEP2: 후보 차량 리스트 */
	@GetMapping("/kiosk/step2")
	public String step2(@RequestParam String plate, Model model) {
		List<Candidate> candidates = kioskService.getCandidates(plate, 4);
		model.addAttribute("plate", plate);
		model.addAttribute("candidates", candidates);
		return "kiosk/step2";
	}

	/* STEP3: 차량 확정 → 출차시간/이용시간 갱신 & 미리보기 */
	@GetMapping("/kiosk/step3")
	public String step3(@RequestParam("carId") Long carId, Model model) {
		// 없으면 입고행 생성, 미정산 건에 출차/이용 갱신
		parkingFeeService.ensureEntryRowIfAbsent(carId);
		parkingFeeService.updateExitAndUseMinutes(carId);

		ParkingFeeRow row = parkingFeeService.getLatestOpenOrLastRow(carId);
		if (row == null) {
			model.addAttribute("error", "対象の車両データが見つかりません。最初からやり直してください。");
			return "kiosk/step1";
		}

		String entryStr = toYmdHm(row.getEntryAt());
		String exitStr = toYmdHm(row.getExitAt());
		int useMin = nz(row.getUseMinutes());
		String useStr = formatUseTime(useMin);

		int baseFee = tariffService.calculateFee(useMin);

		model.addAttribute("carId", carId);
		model.addAttribute("entryTime", entryStr);
		model.addAttribute("exitTime", exitStr);
		model.addAttribute("useMinutes", useMin);
		model.addAttribute("useTime", useStr);
		model.addAttribute("fee", baseFee);
		return "kiosk/step3";
	}

	/* STEP3 → STEP4: 결제수단 화면 (PENDING 저장) */
	@PostMapping("/kiosk/step4")
	public String postStep4(FeeForm form, Model model) {
		ParkingFeeInfo saved = kioskService.savePendingFee(form); // PENDING insert
		ParkingFeeRow row = parkingFeeService.getLatestOpenOrLastRow(saved.getVehicleId());

		model.addAttribute("fee", saved);
		model.addAttribute("carId", saved.getVehicleId());
		model.addAttribute("paymentMethod", saved.getPaymentMethod());

		model.addAttribute("entryTime", toYmdHm(saved.getEntryAt()));
		model.addAttribute("exitTime", toYmdHm(row != null ? row.getExitAt() : null));

		int useMin = (row != null && row.getUseMinutes() != null) ? row.getUseMinutes() : nz(saved.getUseMinutes());
		int freeMin = nz(saved.getFreeMinutes());
		model.addAttribute("useMinutes", useMin);
		model.addAttribute("useTime", formatUseTime(useMin));
		model.addAttribute("freeMinutes", freeMin);
		model.addAttribute("freeTime", formatUseTime(freeMin));

		Integer rowTotal = (row != null ? row.getTotalFee() : null);
		int totalFee = (rowTotal != null) ? rowTotal : nz(saved.getFinalFeeYen());
		model.addAttribute("totalFee", totalFee);

		return "kiosk/step4";
	}

	/* STEP4 → STEP5 : 결제 완료(현금/카드) */
	@PostMapping("/kiosk/step4/complete")
	public String step4Complete(@RequestParam("carId") Long carId, @RequestParam("paymentMethod") String paymentMethod,
			Model model) {
		// 결제 직전 시각으로 출차/이용 최신화
		parkingFeeService.updateExitAndUseMinutes(carId);

		ParkingFeeRow row = parkingFeeService.getLatestOpenOrLastRow(carId);
		if (row == null) {
			model.addAttribute("error", "対象の車両データが見つかりません。最初からやり直してください。");
			return "kiosk/step1";
		}

		String plate = safeStr(vehicleEntryMapper.findPlateById(carId));

		int useMin = nz(row.getUseMinutes());
		int freeMin = nz(row.getFreeMinutes());
		int totalFee = nz(row.getTotalFee());

		model.addAttribute("carId", carId);
		model.addAttribute("plate", plate);
		model.addAttribute("paymentMethod", paymentMethod);
		model.addAttribute("entryTime", toYmdHm(row.getEntryAt()));
		model.addAttribute("exitTime", toYmdHm(row.getExitAt()));
		model.addAttribute("useTime", formatUseTime(useMin));
		model.addAttribute("freeMinutes", freeMin);
		model.addAttribute("freeTime", formatUseTime(freeMin));
		model.addAttribute("totalFee", totalFee);

		return "kiosk/step5";
	}

	/* STEP3 → STEP5 : 전액 무료/직접 이동 */
	@PostMapping("/kiosk/step5")
	public String step5(@RequestParam Long carId, @RequestParam(required = false) Integer amountYen, // 참고용
			@RequestParam(required = false, defaultValue = "receipt") String paymentMethod, Model model) {
		// 영수증 직전 시각 반영
		parkingFeeService.updateExitAndUseMinutes(carId);

		ParkingFeeRow row = parkingFeeService.getLatestOpenOrLastRow(carId);
		if (row == null) {
			model.addAttribute("error", "対象の車両データが見つかりません。最初からやり直してください。");
			return "kiosk/step1";
		}

		String plate = safeStr(vehicleEntryMapper.findPlateById(carId));

		int useMin = nz(row.getUseMinutes());
		int freeMin = nz(row.getFreeMinutes());
		int totalFee = nz(row.getTotalFee());

		model.addAttribute("carId", carId);
		model.addAttribute("plate", plate);
		model.addAttribute("paymentMethod", paymentMethod); // 'cash' / 'card' / 'receipt'
		model.addAttribute("entryTime", toYmdHm(row.getEntryAt()));
		model.addAttribute("exitTime", toYmdHm(row.getExitAt()));
		model.addAttribute("useTime", formatUseTime(useMin));
		model.addAttribute("freeMinutes", freeMin);
		model.addAttribute("freeTime", formatUseTime(freeMin));
		model.addAttribute("totalFee", totalFee);

		return "kiosk/step5";
	}

	/* ===== Helpers ===== */
	private static String toYmdHm(LocalDateTime dt) {
		if (dt == null)
			return "";
		return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
	}

	private static String formatUseTime(int minutes) {
		int h = minutes / 60;
		int m = minutes % 60;
		if (h > 0 && m > 0)
			return h + "時間" + m + "分";
		if (h > 0)
			return h + "時間";
		return m + "分";
	}

	private static int nz(Integer v) {
		return v == null ? 0 : v;
	}

	private static String safeStr(String s) {
		return s == null ? "" : s;
	}
}
