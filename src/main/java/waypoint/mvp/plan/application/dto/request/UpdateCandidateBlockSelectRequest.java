package waypoint.mvp.plan.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateCandidateBlockSelectRequest(
	@NotBlank(message = "blockId 필수 입니다.")
	String blockId,

	Boolean fixed
) {

}
