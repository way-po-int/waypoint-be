package waypoint.mvp.plan.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CandidateBlockSelectRequest(
	@NotBlank(message = "blockId 필수 입니다.")
	String blockId
) {
}
