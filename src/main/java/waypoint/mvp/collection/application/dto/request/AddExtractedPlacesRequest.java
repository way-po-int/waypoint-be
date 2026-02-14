package waypoint.mvp.collection.application.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record AddExtractedPlacesRequest(
	@NotEmpty(message = "저장할 장소를 하나 이상 선택해야 합니다.")
	List<@NotBlank(message = "장소 ID 값은 비어있을 수 없습니다.") String> placeIds
) {
}
