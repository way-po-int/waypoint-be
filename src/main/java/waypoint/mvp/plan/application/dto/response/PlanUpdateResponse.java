package waypoint.mvp.plan.application.dto.response;

import java.util.List;

public record PlanUpdateResponse(
	boolean requiresConfirmation,
	PlanUpdateType updateType,
	PlanResponse plan,
	List<AffectedDay> affectedDays
) {

	public static PlanUpdateResponse success(PlanResponse plan) {
		return new PlanUpdateResponse(false, null, plan, null);
	}

	public static PlanUpdateResponse confirmRequired(PlanUpdateType updateType, List<AffectedDay> affectedDays) {
		return new PlanUpdateResponse(true, updateType, null, affectedDays);
	}

	public record AffectedDay(
		int day,
		long scheduleCount
	) {
	}

	public enum PlanUpdateType {
		STAY,      // 일차수 동일 (날짜만 변경)
		INCREASE,  // 일차수 증가
		DECREASE   // 일차수 감소
	}
}
