package waypoint.mvp.plan.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateCandidateBlockSelectRequest(
	@NotBlank(message = "blockId 필수 입니다.")
	String blockId,

	@NotNull(message = "fixed는 ture/false만 가능합니다.")
	Boolean fixed
) {

}
