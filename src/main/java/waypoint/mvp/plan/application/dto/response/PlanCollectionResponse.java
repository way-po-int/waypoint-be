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
		AddedBy addedBy = new AddedBy(member.getExternalId(), member.getPicture(), member.getNickname());

		return new PlanCollectionResponse(collection.getExternalId(), collection.getTitle(), addedBy);
	}

	record AddedBy(String planMemberId, String picture, String nickname) {

	}
}
