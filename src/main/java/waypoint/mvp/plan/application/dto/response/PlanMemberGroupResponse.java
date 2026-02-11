package waypoint.mvp.plan.application.dto.response;

import java.util.List;

public record PlanMemberGroupResponse(
	Boolean isAuthenticated,
	PlanMemberResponse me,
	List<PlanMemberResponse> members
) {
}
