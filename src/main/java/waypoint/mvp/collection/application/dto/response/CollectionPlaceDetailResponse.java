package waypoint.mvp.collection.application.dto.response;

import waypoint.mvp.collection.domain.CollectionPlace;

public record CollectionPlaceDetailResponse(
	String collectionPlaceId,
	String memo,
	PlaceResponse place,
	SocialMediaResponse socialMedia
) {
	public static CollectionPlaceDetailResponse of(
		CollectionPlace collectionPlace,
		PlaceResponse place,
		SocialMediaResponse socialMedia
	) {
		return new CollectionPlaceDetailResponse(
			collectionPlace.getId().toString(),
			collectionPlace.getMemo(),
			place,
			socialMedia
		);
	}
}
