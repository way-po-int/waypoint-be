package waypoint.mvp.collection.application.dto.response;

import java.util.List;

public record PickPassResponse(
	PickPassGroup picked,
	PickPassGroup passed
) {

	public static PickPassResponse of(
		List<CollectionMemberResponse> pickedMembers,
		List<CollectionMemberResponse> passedMembers
	) {
		return new PickPassResponse(
			PickPassGroup.of(pickedMembers),
			PickPassGroup.of(passedMembers)
		);
	}

	public record PickPassGroup(
		List<CollectionMemberResponse> members,
		int count
	) {
		public static PickPassGroup of(List<CollectionMemberResponse> members) {
			return new PickPassGroup(members, members.size());
		}
	}
}
