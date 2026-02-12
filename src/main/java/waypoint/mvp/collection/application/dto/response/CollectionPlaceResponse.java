package waypoint.mvp.collection.application.dto.response;

import waypoint.mvp.collection.domain.CollectionPlace;
import waypoint.mvp.place.application.dto.PlaceResponse;

public record CollectionPlaceResponse(
	String collectionPlaceId,
	String memo,
	PlaceResponse place,
	PickPassResponse pickPass

) {
	public static CollectionPlaceResponse of(
		CollectionPlace collectionPlace,
		PlaceResponse place,
		PickPassResponse pickPass

	) {
		return new CollectionPlaceResponse(
			collectionPlace.getExternalId(),
			collectionPlace.getMemo(),
			place,
			pickPass
		);
	}
}
