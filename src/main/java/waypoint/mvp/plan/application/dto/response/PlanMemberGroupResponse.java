package waypoint.mvp.plan.application.dto.response;

import java.util.List;

public record PlanMemberGroupResponse(
	PlanMemberResponse me,
	List<PlanMemberResponse> members,
	boolean isAuthenticated
) {
}
