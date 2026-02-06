package waypoint.mvp.collection.application.dto.response;

import waypoint.mvp.collection.domain.Collection;

public record CollectionResponse(
	String id,
	String title,
	String thumbnail,
	int memberCount
) {
	public static CollectionResponse from(Collection collection) {
		return new CollectionResponse(collection.getExternalId(), collection.getTitle(), collection.getThumbnail(),
			collection.getMemberCount());
	}
}
