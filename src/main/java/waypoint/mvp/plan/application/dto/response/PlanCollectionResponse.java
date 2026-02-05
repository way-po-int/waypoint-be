package waypoint.mvp.plan.application.dto.response;

import waypoint.mvp.collection.domain.Collection;
import waypoint.mvp.plan.domain.PlanCollection;

public record PlanCollectionResponse(
	String collectionId,
	String title,
	PlanMemberResponse planMember
) {

	public static PlanCollectionResponse from(PlanCollection planCollection) {
		Collection collection = planCollection.getCollection();
		PlanMemberResponse member = PlanMemberResponse.from(planCollection.getMember());

		return new PlanCollectionResponse(collection.getExternalId(), collection.getTitle(), member);
	}
}
