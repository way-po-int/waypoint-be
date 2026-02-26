package waypoint.mvp.plan.application.dto;

import java.util.Collections;
import java.util.List;

import waypoint.mvp.plan.application.dto.response.PlanUpdateResponse.AffectedDay;
import waypoint.mvp.plan.application.dto.response.PlanUpdateResponse.PlanUpdateType;

public record PlanDaySyncResult(
	PlanUpdateType updateType,
	List<AffectedDay> affectedDays
) {

	private static final PlanDaySyncResult EMPTY = new PlanDaySyncResult(null, Collections.emptyList());

	public static PlanDaySyncResult success() {
		return EMPTY;
	}

	public static PlanDaySyncResult withWarnings(PlanUpdateType updateType, List<AffectedDay> affectedDays) {
		return new PlanDaySyncResult(updateType, affectedDays);
	}

	public boolean hasWarnings() {
		return updateType != null;
	}
}
