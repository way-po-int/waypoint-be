package waypoint.mvp.place.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum SocialMediaError implements ErrorCode {
	SOCIAL_MEDIA_UNSUPPORTED(HttpStatus.BAD_REQUEST, "지원하지 않는 URL입니다."),
	SOCIAL_MEDIA_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 소셜 미디어를 찾을 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
