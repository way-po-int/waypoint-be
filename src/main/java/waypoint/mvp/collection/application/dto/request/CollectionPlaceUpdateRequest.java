package waypoint.mvp.collection.application.dto.request;

import jakarta.validation.constraints.Size;

public record CollectionPlaceUpdateRequest(
	@Size(max = 300, message = "memo는 최대 300자까지 입력할 수 있습니다.")
	String memo
) {
}
