package waypoint.mvp.plan.application.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public record CandidateBlockCreateRequest(
	@NotEmpty
	List<String> collectionPlaceIds
) {

}
