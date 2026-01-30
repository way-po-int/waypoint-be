package waypoint.mvp.plan.application.dto.response;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import waypoint.mvp.plan.domain.Plan;

public record PlanResponse(
	String planId,

	String title,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	LocalDate startDate,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	LocalDate endDate,

	int memberCount) {

	public static PlanResponse from(Plan plan) {
		return new PlanResponse(
			plan.getId().toString(),
			plan.getTitle(),
			plan.getStartDate(),
			plan.getEndDate(),
			plan.getMemberCount()
		);
	}
}
