package waypoint.mvp.plan.application.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record CreatePlanCollectionRequest(
	@NotEmpty(message = "collectionIds는 필수 입니다.")
	List<@NotBlank(message = "collectionId는 필수 입니다.") String> collectionIds
) {

}
