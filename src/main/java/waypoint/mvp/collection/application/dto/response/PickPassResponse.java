package waypoint.mvp.collection.application.dto.response;

import java.util.List;

public record PickPassResponse(
	List<CollectionMemberResponse> pickedMembers,
	List<CollectionMemberResponse> passedMembers
) {
	public static PickPassResponse of(
		List<CollectionMemberResponse> pickedMembers,
		List<CollectionMemberResponse> passedMembers
	) {
		return new PickPassResponse(pickedMembers, passedMembers);
	}
}
