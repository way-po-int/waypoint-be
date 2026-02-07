package waypoint.mvp.collection.application.dto.request;

import jakarta.validation.constraints.Size;
import waypoint.mvp.global.validation.annotation.MemoPolicy;

public record CollectionPlaceUpdateRequest(
	@Size(max = 300, message = "memo는 최대 300자까지 입력할 수 있습니다.")
	@MemoPolicy
	String memo
) {
}
