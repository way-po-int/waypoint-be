package waypoint.mvp.plan.application.dto.response;

import waypoint.mvp.collection.domain.Collection;
import waypoint.mvp.plan.domain.PlanCollection;
import waypoint.mvp.plan.domain.PlanMember;

public record PlanCollectionResponse(
	String collectionId,
	String title,
	AddedBy addedBy
) {

	public static PlanCollectionResponse from(PlanCollection planCollection) {
		Collection collection = planCollection.getCollection();
		PlanMember member = planCollection.getMember();
		String planMemberId = member.getDeletedAt() != null ? member.getExternalId() : "";
		AddedBy addedBy = new AddedBy(planMemberId, member.getPicture(), member.getNickname());

		return new PlanCollectionResponse(collection.getExternalId(), collection.getTitle(), addedBy);
	}

	/**
	 * 컬렉션에 플랜을 추가한 사용자 정보
	 *
	 * <p>탈퇴한 멤버의 id는 빈 문자열("")로 처리된다.</p>
	 */
	record AddedBy(String planMemberId, String picture, String nickname) {

	}
}
