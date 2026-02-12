package waypoint.mvp.plan.application.dto;

import java.util.Collections;
import java.util.List;

import waypoint.mvp.plan.application.dto.response.PlanUpdateResponse;

public record PlanDaySyncResult(
	List<PlanUpdateResponse.AffectedDay> affectedDays
) {

	private static final PlanDaySyncResult EMPTY = new PlanDaySyncResult(Collections.emptyList());

	public static PlanDaySyncResult success() {
		return EMPTY;
	}

	public static PlanDaySyncResult withWarnings(List<PlanUpdateResponse.AffectedDay> affectedDays) {
		return new PlanDaySyncResult(affectedDays);
	}

	public boolean hasWarnings() {
		return affectedDays != null && !affectedDays.isEmpty();
	}
}
