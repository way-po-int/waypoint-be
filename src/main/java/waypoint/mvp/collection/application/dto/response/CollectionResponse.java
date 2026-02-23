package waypoint.mvp.collection.application.dto.response;

import waypoint.mvp.collection.domain.Collection;

public record CollectionResponse(
	String collectionId,
	String title,
	String thumbnail,
	int memberCount,
	int placeCount
) {
	public static CollectionResponse from(Collection collection, int placeCount) {
		return new CollectionResponse(
			collection.getExternalId(),
			collection.getTitle(),
			collection.getThumbnail(),
			collection.getMemberCount(),
			placeCount
		);
	}
}
