package waypoint.mvp.collection.application.dto.response;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import waypoint.mvp.collection.domain.CollectionPlacePreference;

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

	public static PickPassResponse from(
		List<CollectionPlacePreference> preferences,
		String currentMemberExternalId
	) {
		Map<CollectionPlacePreference.Type, List<CollectionMemberResponse>> grouped = preferences.stream()
			.collect(groupingBy(
				CollectionPlacePreference::getType,
				() -> new EnumMap<>(CollectionPlacePreference.Type.class),
				mapping(pref -> CollectionMemberResponse.from(pref.getMember()), toList())
			));

		List<CollectionMemberResponse> pickedMembers =
			grouped.getOrDefault(CollectionPlacePreference.Type.PICK, List.of());
		List<CollectionMemberResponse> passedMembers =
			grouped.getOrDefault(CollectionPlacePreference.Type.PASS, List.of());

		return of(pickedMembers, passedMembers, currentMemberExternalId);
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
