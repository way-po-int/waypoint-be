package waypoint.mvp.plan.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreatePlanCollectionRequest(
	@NotBlank(message = "collectionId는 필수 입니다.")
	String collectionId
) {

}
