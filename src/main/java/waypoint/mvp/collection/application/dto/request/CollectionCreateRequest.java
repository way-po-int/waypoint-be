package waypoint.mvp.collection.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import waypoint.mvp.global.validation.annotation.TitlePolicy;

public record CollectionCreateRequest(
	@NotBlank(message = "컬렉션 제목은 비워둘 수 없습니다.")
	@Size(min = 1, max = 20, message = "컬렉션 제목은 20자를 초과할 수 없습니다.")
	@TitlePolicy
	String title
) {
}
