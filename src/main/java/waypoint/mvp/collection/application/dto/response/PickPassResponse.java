package waypoint.mvp.collection.application.dto.response;

import java.util.List;

public record PickPassResponse(
	PickPassGroup picked,
	PickPassGroup passed,
	MyPreferenceStatus myPreference
) {

	public enum MyPreferenceStatus {
		PICK,
		PASS,
		NOTHING;
	}

	public static PickPassResponse of(
		List<CollectionMemberResponse> pickedMembers,
		List<CollectionMemberResponse> passedMembers,
		String collectionMemberId
	) {

		boolean isPicked = collectionMemberId != null && pickedMembers.stream()
			.anyMatch(m -> m.collectionMemberId().equals(collectionMemberId));

		boolean isPassed = collectionMemberId != null && !isPicked && passedMembers.stream()
			.anyMatch(m -> m.collectionMemberId().equals(collectionMemberId));

		MyPreferenceStatus myPreference =
			isPicked ? MyPreferenceStatus.PICK : (isPassed ? MyPreferenceStatus.PASS : MyPreferenceStatus.NOTHING);

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
