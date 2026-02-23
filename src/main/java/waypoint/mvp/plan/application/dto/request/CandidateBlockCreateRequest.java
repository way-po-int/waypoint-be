package waypoint.mvp.plan.application.dto.request;

import java.util.List;

import jakarta.validation.constraints.AssertTrue;

public record CandidateBlockCreateRequest(
	List<String> collectionPlaceIds,

	List<String> placeIds
) {

	@AssertTrue(message = "collectionPlaceIds 또는 placeIds 중 하나만 입력해야 합니다.")
	public boolean isOnlyOneProvided() {

		boolean hasCollection =
			collectionPlaceIds != null && !collectionPlaceIds.isEmpty();

		boolean hasPlace =
			placeIds != null && !placeIds.isEmpty();

		return hasCollection ^ hasPlace;
	}

	public static CandidateBlockCreateRequest createCollectionPlaceIds(List<String> collectionPlaceIds) {
		return new CandidateBlockCreateRequest(collectionPlaceIds, null);
	}

	public static CandidateBlockCreateRequest createPlaceIds(List<String> placeIds) {
		return new CandidateBlockCreateRequest(null, placeIds);
	}

}
