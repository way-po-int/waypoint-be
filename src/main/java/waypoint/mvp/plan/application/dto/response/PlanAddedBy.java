package waypoint.mvp.plan.application.dto.response;

import waypoint.mvp.plan.domain.PlanMember;

public record PlanAddedBy(
	String planMemberId,
	String nickname,
	String picture
) {
	public static PlanAddedBy from(PlanMember member) {
		return new PlanAddedBy(member.getExternalId(), member.getNickname(), member.getPicture());
	}
}
