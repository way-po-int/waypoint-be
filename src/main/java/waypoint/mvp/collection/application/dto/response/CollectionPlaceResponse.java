package waypoint.mvp.collection.application.dto.response;

import java.util.List;

import waypoint.mvp.collection.domain.CollectionPlace;

public record CollectionPlaceResponse(
	String collectionPlaceId,
	String memo,
	PlaceResponse place,
	List<PickPassMemberResponse> pickedMember,
	List<PickPassMemberResponse> passedMember
) {
	public static CollectionPlaceResponse of(
		CollectionPlace collectionPlace,
		PlaceResponse place,
		List<PickPassMemberResponse> pickedMember,
		List<PickPassMemberResponse> passedMember
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
