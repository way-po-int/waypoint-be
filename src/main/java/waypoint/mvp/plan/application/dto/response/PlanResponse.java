package waypoint.mvp.plan.application.dto.response;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.fasterxml.jackson.annotation.JsonFormat;

import waypoint.mvp.plan.domain.Plan;

public record PlanResponse(
	String planId,

	String title,

	String thumbnail,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	LocalDate startDate,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	LocalDate endDate,

	int durationDays,
	int memberCount,
	int collectionCount
) {

	public static PlanResponse from(Plan plan, String thumbnail, int collectionCount) {
		return new PlanResponse(
			plan.getExternalId(),
			plan.getTitle(),
			thumbnail,
			plan.getStartDate(),
			plan.getEndDate(),
			calcDurationDays(plan.getStartDate(), plan.getEndDate()),
			plan.getMemberCount(),
			collectionCount
		);
	}

	private static int calcDurationDays(LocalDate startDate, LocalDate endDate) {
		return (int)ChronoUnit.DAYS.between(startDate, endDate) + 1;
	}
}
