package waypoint.mvp.place.application.dto;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.NotBlank;

public record ManualPlaceInfo(
	@NotBlank(message = "장소 이름은 필수입니다.")
	String name,

	@NotBlank(message = "주소는 필수입니다.")
	String address,

	@NotBlank(message = "URL을 입력해 주세요.")
	@URL(message = "올바른 URL 형식이 아닙니다.")
	String socialMediaUrl
) {
}
