package waypoint.mvp.plan.application.dto.response;

import java.util.List;

public record PlanUpdateResponse(
	boolean requiresConfirmation,
	PlanResponse plan,
	List<AffectedDay> affectedDays
) {

	public static PlanUpdateResponse success(PlanResponse plan) {
		return new PlanUpdateResponse(false, plan, null);
	}

	public static PlanUpdateResponse confirmRequired(List<AffectedDay> affectedDays) {
		return new PlanUpdateResponse(true, null, affectedDays);
	}

	public record AffectedDay(
		int day,
		long scheduleCount
	) {
	}
}
