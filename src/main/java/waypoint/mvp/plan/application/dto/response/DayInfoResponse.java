package waypoint.mvp.plan.application.dto.response;

import java.time.DayOfWeek;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import waypoint.mvp.plan.domain.Plan;
import waypoint.mvp.plan.domain.PlanDay;

public record DayInfoResponse(
	int day,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	LocalDate date,

	DayOfWeek dayOfWeek
) {
	public static DayInfoResponse from(PlanDay planDay, Plan plan) {
		LocalDate date = plan.getStartDate().plusDays(planDay.getDay() - 1);
		return new DayInfoResponse(planDay.getDay(), date, date.getDayOfWeek());
	}
}
