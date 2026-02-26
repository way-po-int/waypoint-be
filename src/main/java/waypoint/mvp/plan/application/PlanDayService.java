package waypoint.mvp.plan.application;

import static java.util.stream.IntStream.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.plan.application.dto.PlanDaySyncResult;
import waypoint.mvp.plan.application.dto.response.PlanUpdateResponse;
import waypoint.mvp.plan.application.dto.response.PlanUpdateResponse.PlanUpdateType;
import waypoint.mvp.plan.domain.Plan;
import waypoint.mvp.plan.domain.PlanDay;
import waypoint.mvp.plan.infrastructure.persistence.PlanDayRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlanDayService {

	private final PlanDayRepository planDayRepository;

	@Transactional
	public void initPlanDays(Plan plan) {
		int totalDays = calculateTotalDays(plan.getStartDate(), plan.getEndDate());
		List<PlanDay> planDays = rangeClosed(1, totalDays)
			.mapToObj(day -> PlanDay.create(plan, day))
			.toList();

		planDayRepository.saveAll(planDays);
	}

	/**
	 * 날짜 변경 시 PlanDay를 동기화한다.
	 * - 일수가 늘어나면: 부족한 일차를 추가
	 * - 일수가 줄어들면: 삭제될 일차에 일정이 있는지 확인 후 처리
	 */
	@Transactional
	public PlanDaySyncResult syncPlanDays(Plan plan, LocalDate newStartDate, LocalDate newEndDate,
		boolean confirm) {
		int currentDays = planDayRepository.countByPlanId(plan.getId());
		int targetDays = calculateTotalDays(newStartDate, newEndDate);

		// 1. 일차수 증가 케이스
		if (targetDays > currentDays) {
			if (!confirm) {
				return PlanDaySyncResult.withWarnings(PlanUpdateType.INCREASE, List.of());
			}
			expandDays(plan, currentDays, targetDays);
			return PlanDaySyncResult.success();
		}

		// 2. 일차수 축소 케이스
		if (targetDays < currentDays) {
			return shrinkDays(plan, targetDays, confirm);
		}

		// 3. 일차수 동일하나 날짜가 변경된 케이스
		if (!plan.getStartDate().equals(newStartDate) || !plan.getEndDate().equals(newEndDate)) {
			if (!confirm) {
				return PlanDaySyncResult.withWarnings(PlanUpdateType.STAY, List.of());
			}
		}

		return PlanDaySyncResult.success();
	}

	private void expandDays(Plan plan, int currentDays, int targetDays) {
		List<PlanDay> newPlanDays = rangeClosed(currentDays + 1, targetDays)
			.mapToObj(day -> PlanDay.create(plan, day))
			.toList();

		planDayRepository.saveAll(newPlanDays);
	}

	private PlanDaySyncResult shrinkDays(Plan plan, int targetDays,
		boolean confirm) {
		List<PlanUpdateResponse.AffectedDay> affectedDays = findAffectedDays(plan.getId(), targetDays);

		if (!confirm) {
			return PlanDaySyncResult.withWarnings(PlanUpdateType.DECREASE, affectedDays);
		}

		deleteExcessDays(plan.getId(), targetDays);
		return PlanDaySyncResult.success();
	}

	private List<PlanUpdateResponse.AffectedDay> findAffectedDays(Long planId, int targetDays) {
		return planDayRepository.countTimeBlocksByDayGreaterThan(planId, targetDays);
	}

	private void deleteExcessDays(Long planId, int targetDays) {
		planDayRepository.deleteAllForExcessDays(planId, targetDays);
	}

	private int calculateTotalDays(LocalDate startDate, LocalDate endDate) {
		return (int)ChronoUnit.DAYS.between(startDate, endDate) + 1;
	}
}
