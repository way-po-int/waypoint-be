package waypoint.mvp.plan.application;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.plan.application.dto.PlanDaySyncResult;
import waypoint.mvp.plan.application.dto.response.PlanUpdateResponse;
import waypoint.mvp.plan.domain.Plan;
import waypoint.mvp.plan.domain.PlanDay;
import waypoint.mvp.plan.domain.TimeBlock;
import waypoint.mvp.plan.infrastructure.persistence.BlockRepository;
import waypoint.mvp.plan.infrastructure.persistence.PlanDayRepository;
import waypoint.mvp.plan.infrastructure.persistence.TimeBlockRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlanDayService {

	private final PlanDayRepository planDayRepository;
	private final TimeBlockRepository timeBlockRepository;
	private final BlockRepository blockRepository;

	@Transactional
	public void initPlanDays(Plan plan) {
		int totalDays = calculateTotalDays(plan.getStartDate(), plan.getEndDate());

		for (int day = 1; day <= totalDays; day++) {
			planDayRepository.save(PlanDay.create(plan, day));
		}
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

		if (targetDays > currentDays) {
			expandDays(plan, currentDays, targetDays);
			return PlanDaySyncResult.success();
		}

		if (targetDays < currentDays) {
			return shrinkDays(plan, currentDays, confirm);
		}

		return PlanDaySyncResult.success();
	}

	private void expandDays(Plan plan, int currentDays, int targetDays) {
		for (int day = currentDays + 1; day <= targetDays; day++) {
			planDayRepository.save(PlanDay.create(plan, day));
		}
	}

	private PlanDaySyncResult shrinkDays(Plan plan, int targetDays,
		boolean confirm) {
		List<PlanUpdateResponse.AffectedDay> affectedDays = findAffectedDays(plan.getId(), targetDays);

		boolean hasSchedules = affectedDays.stream()
			.anyMatch(ad -> ad.scheduleCount() > 0);

		if (hasSchedules && !confirm) {
			return PlanDaySyncResult.withWarnings(affectedDays);
		}

		deleteExcessDays(plan.getId(), targetDays);
		return PlanDaySyncResult.success();
	}

	private List<PlanUpdateResponse.AffectedDay> findAffectedDays(Long planId, int targetDays) {
		List<Object[]> rows = planDayRepository.countTimeBlocksByDayGreaterThan(planId, targetDays);

		return rows.stream()
			.map(row -> new PlanUpdateResponse.AffectedDay(
				(int)row[0],
				(long)row[1]
			))
			.toList();
	}

	private void deleteExcessDays(Long planId, int targetDays) {
		List<PlanDay> excessDays = planDayRepository.findAllByPlanIdAndDayGreaterThan(planId, targetDays);

		if (excessDays.isEmpty()) {
			return;
		}

		List<TimeBlock> timeBlocks = timeBlockRepository.findAllByPlanDayIn(excessDays);

		if (!timeBlocks.isEmpty()) {
			blockRepository.deleteAllByTimeBlockIn(timeBlocks);
			timeBlockRepository.deleteAllByPlanDayIn(excessDays);
		}

		planDayRepository.deleteAll(excessDays);
	}

	private int calculateTotalDays(LocalDate startDate, LocalDate endDate) {
		return (int)ChronoUnit.DAYS.between(startDate, endDate) + 1;
	}
}
