package waypoint.mvp.collection.application.dto.response;

import java.util.List;

import waypoint.mvp.collection.domain.CollectionPlace;
import waypoint.mvp.place.application.dto.PlaceResponse;

public record CollectionPlaceResponse(
	String collectionPlaceId,
	String memo,
	PlaceResponse place,
	List<CollectionMemberResponse> pickedMember,
	List<CollectionMemberResponse> passedMember
) {
	public static CollectionPlaceResponse of(
		CollectionPlace collectionPlace,
		PlaceResponse place,
		List<CollectionMemberResponse> pickedMember,
		List<CollectionMemberResponse> passedMember
	) {
		return new CollectionPlaceResponse(
			collectionPlace.getId().toString(),
			collectionPlace.getMemo(),
			place,
			pickedMember,
			passedMember
		);
	}
}
