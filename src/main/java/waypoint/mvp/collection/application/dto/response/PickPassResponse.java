package waypoint.mvp.collection.application.dto.response;

import java.util.List;

public record PickPassResponse(
	List<CollectionMemberResponse> pickedMember,
	List<CollectionMemberResponse> passedMember
) {
	public static PickPassResponse of(
		List<CollectionMemberResponse> pickedMember,
		List<CollectionMemberResponse> passedMember
	) {
		return new PickPassResponse(pickedMember, passedMember);
	}
}
