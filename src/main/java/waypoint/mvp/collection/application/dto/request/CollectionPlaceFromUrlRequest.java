package waypoint.mvp.collection.application.dto.request;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.NotBlank;

public record CollectionPlaceFromUrlRequest(
	@NotBlank(message = "URL을 입력해 주세요.")
	@URL(message = "올바른 URL 형식이 아닙니다.")
	String url
) {
}
