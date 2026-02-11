package waypoint.mvp.user.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import waypoint.mvp.global.util.ValidationPatterns;

public record UserUpdateRequest(
	@NotBlank
	@Size(min = 2, max = 10)
	@Pattern(
		regexp = ValidationPatterns.NICKNAME_REGEX,
		message = "닉네임은 2~10자의 한글/영문/숫자만 가능합니다. (공백/특수문자 불가)"
	)
	String nickname
) {
}
