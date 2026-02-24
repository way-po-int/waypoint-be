package waypoint.mvp.collection.application.dto.response;

import java.util.List;

public record PickPassResponse(
	PickPassGroup picked,
	PickPassGroup passed,
	String myPreference
) {

	public static PickPassResponse of(
		List<CollectionMemberResponse> pickedMembers,
		List<CollectionMemberResponse> passedMembers,
		String collectionMemberId
	) {

		boolean isPicked = pickedMembers.stream()
			.anyMatch(m -> m.collectionMemberId().equals(collectionMemberId));

		boolean isPassed = !isPicked && passedMembers.stream()
			.anyMatch(m -> m.collectionMemberId().equals(collectionMemberId));

		String myPreference = isPicked ? "PICKED" : (isPassed ? "PASSED" : "");

		return new PickPassResponse(
			PickPassGroup.of(pickedMembers),
			PickPassGroup.of(passedMembers),
			myPreference
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
