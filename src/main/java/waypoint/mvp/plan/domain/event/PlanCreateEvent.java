package waypoint.mvp.plan.domain.event;

import waypoint.mvp.auth.security.principal.UserPrincipal;

public record PlanCreateEvent(
	Long planId,
	UserPrincipal user
) {
	public static PlanCreateEvent of(Long planId, UserPrincipal user) {
		return new PlanCreateEvent(planId, user);
	}
}
