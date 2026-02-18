package waypoint.mvp.plan.application.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import waypoint.mvp.plan.domain.BlockOpinionType;

public record BlockOpinionCreateRequest(
	@NotNull(message = "의견 타입은 필수입니다.")
	BlockOpinionType type,

	List<String> tagIds,

	@Size(max = 300, message = "의견은 최대 300자까지 입력할 수 있습니다.")
	String comment
) {
}
