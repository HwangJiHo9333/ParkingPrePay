package com.hwangjiho.parking.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.SessionAttributes;

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
@SessionAttributes("lastPlate")
public class KioskController {

	private final KioskService kioskService;
	private final ParkingFeeService parkingFeeService;
	private final TariffService tariffService;
	private final VehicleEntryMapper vehicleEntryMapper;

	@GetMapping({ "/", "/index" })
	public String index() {
		return "redirect:/kiosk/step1";
	}

	@GetMapping("/kiosk/step1")
	public String step1(SessionStatus status) {
		status.setComplete();
		return "kiosk/step1";
	}

	@GetMapping("/kiosk/step2")
	public String step2(@RequestParam String plate, Model model) {
		List<Candidate> candidates = kioskService.getCandidates(plate, 4);
		model.addAttribute("plate", plate);
		model.addAttribute("candidates", candidates);
		model.addAttribute("lastPlate", plate);
		return "kiosk/step2";
	}

	@GetMapping("/kiosk/interphone")
	public String interphone() {
		return "kiosk/interphone";
	}

	@GetMapping("/kiosk/step3")
	public String step3(@RequestParam("carId") Long carId, Model model) {
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

	/* ---------- PRG: POST → REDIRECT → GET ---------- */

	/** POST: step3에서 계산한 내용을 저장하고 step4로 리다이렉트 */
	@PostMapping("/kiosk/step4")
	public String postStep4(FeeForm form, RedirectAttributes ra) {
		ParkingFeeInfo saved = kioskService.savePendingFee(form); // PENDING 저장
		// 출차/이용 최신화(표시 반영)
		parkingFeeService.updateExitAndUseMinutes(saved.getVehicleId());

		// tx(id) + 선택 결제수단을 쿼리스트링으로 넘김
		ra.addAttribute("tx", saved.getFeeId()); // 복구키
		ra.addAttribute("method", form.getPaymentMethod()); // 'cash' / 'card'

		return "redirect:/kiosk/step4";
	}

	/** GET: 복구용 step4. tx(권장) 또는 carId로 화면 데이터를 채움 */
	@GetMapping("/kiosk/step4")
	public String getStep4(@RequestParam(required = false) Long tx, @RequestParam(required = false) Long carId,
			@RequestParam(required = false, name = "method") String method, Model model) {

		ParkingFeeInfo fee;
		if (tx != null) {
			fee = kioskService.findPendingById(tx);
			if (fee != null)
				carId = fee.getVehicleId();
		} else {
			fee = kioskService.findLatestPendingByCarId(carId);
		}
		if (carId == null) {
			model.addAttribute("error", "不正なアクセスです。");
			return "kiosk/step1";
		}

		parkingFeeService.updateExitAndUseMinutes(carId);
		ParkingFeeRow row = parkingFeeService.getLatestOpenOrLastRow(carId);

		// ★ 결제수단 결정: 쿼리파라미터 > DB > 기본값 cash
		String paymentMethod = (method != null && !method.isBlank()) ? method
				: (fee != null && fee.getPaymentMethod() != null ? fee.getPaymentMethod() : "cash");

		model.addAttribute("carId", carId);
		model.addAttribute("paymentMethod", paymentMethod);

		model.addAttribute("entryTime",
				toYmdHm(row != null ? row.getEntryAt() : (fee != null ? fee.getEntryAt() : null)));
		model.addAttribute("exitTime", toYmdHm(row != null ? row.getExitAt() : null));

		int useMin = (row != null && row.getUseMinutes() != null) ? row.getUseMinutes()
				: (fee != null && fee.getUseMinutes() != null ? fee.getUseMinutes() : 0);
		int freeMin = (fee != null && fee.getFreeMinutes() != null) ? fee.getFreeMinutes() : 0;

		model.addAttribute("useMinutes", useMin);
		model.addAttribute("useTime", formatUseTime(useMin));
		model.addAttribute("freeMinutes", freeMin);
		model.addAttribute("freeTime", formatUseTime(freeMin));

		Integer rowTotal = (row != null ? row.getTotalFee() : null);
		int totalFee = (rowTotal != null) ? rowTotal
				: (fee != null && fee.getFinalFeeYen() != null ? fee.getFinalFeeYen() : 0);
		model.addAttribute("totalFee", totalFee);

		if (tx != null)
			model.addAttribute("tx", tx); // step5에서 뒤로가기용

		return "kiosk/step4";
	}

	/* step5 : 무료 직행/결제 완료 공통 진입 */
	@PostMapping("/kiosk/step5")
	public String step5(@RequestParam Long carId, @RequestParam(required = false) Integer amountYen,
			@RequestParam(required = false, defaultValue = "receipt") String paymentMethod,
			@RequestParam(required = false) Integer tenderedYen, @RequestParam(required = false) Integer changeYen,
			@RequestParam(required = false) Long tx, Model model) {

		parkingFeeService.updateExitAndUseMinutes(carId);
		ParkingFeeRow row = parkingFeeService.getLatestOpenOrLastRow(carId);
		if (row == null) {
			model.addAttribute("error", "対象の車両データが見つかりません。最初からやり直してください。");
			return "kiosk/step1";
		}

		String plate = safeStr(vehicleEntryMapper.findPlateById(carId));
		int useMin = nz(row.getUseMinutes());
		int freeMin = nz(row.getFreeMinutes());
		int dbTotal = nz(row.getTotalFee());

		int paidAmount = (amountYen != null ? amountYen : dbTotal);

		model.addAttribute("carId", carId);
		model.addAttribute("plate", plate);
		model.addAttribute("paymentMethod", paymentMethod);
		model.addAttribute("entryTime", toYmdHm(row.getEntryAt()));
		model.addAttribute("exitTime", toYmdHm(row.getExitAt()));
		model.addAttribute("useTime", formatUseTime(useMin));
		model.addAttribute("freeMinutes", freeMin);
		model.addAttribute("freeTime", formatUseTime(freeMin));

		model.addAttribute("totalFee", dbTotal);
		model.addAttribute("paidAmount", paidAmount);

		if (tenderedYen != null)
			model.addAttribute("tenderedYen", tenderedYen);
		if (changeYen != null)
			model.addAttribute("changeYen", changeYen);
		if (tx != null)
			model.addAttribute("tx", tx);

		return "kiosk/step5";
	}

	/* ===== Helpers ===== */
	private static String toYmdHm(LocalDateTime dt) {
		if (dt == null)
			return "";
		return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
	}

	private static String formatUseTime(int minutes) {
		int h = minutes / 60, m = minutes % 60;
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
