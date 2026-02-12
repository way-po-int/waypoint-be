package waypoint.mvp.collection.application.dto.response;

import waypoint.mvp.collection.domain.CollectionPlace;
import waypoint.mvp.place.application.dto.PlaceResponse;

public record CollectionPlaceDetailResponse(
	String collectionPlaceId,
	String memo,
	PlaceResponse place,
	SocialMediaResponse socialMedia,
	PickPassResponse pickPass
) {
	public static CollectionPlaceDetailResponse of(
		CollectionPlace collectionPlace,
		PlaceResponse place,
		SocialMediaResponse socialMedia,
		PickPassResponse pickPass
	) {
		return new CollectionPlaceDetailResponse(
			collectionPlace.getExternalId(),
			collectionPlace.getMemo(),
			place,
			socialMedia,
			pickPass
		);
	}
}
