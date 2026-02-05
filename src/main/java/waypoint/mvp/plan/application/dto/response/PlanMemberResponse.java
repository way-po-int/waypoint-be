package waypoint.mvp.plan.application.dto.response;

import waypoint.mvp.plan.domain.PlanMember;

public record PlanMemberResponse(
	String planMemberId,
	String nickname,
	String picture,
	String role
) {
	public static PlanMemberResponse from(PlanMember member) {
		return new PlanMemberResponse(member.getExternalId(), member.getNickname(),
			member.getPicture(), member.getRole().toString());
	}
}
