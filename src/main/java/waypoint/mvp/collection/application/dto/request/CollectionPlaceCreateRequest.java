package waypoint.mvp.collection.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CollectionPlaceCreateRequest(
	@NotBlank(message = "placeId는 필수입니다.")
	String placeId
) {
}
