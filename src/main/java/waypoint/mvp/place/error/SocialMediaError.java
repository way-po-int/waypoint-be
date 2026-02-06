package waypoint.mvp.place.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum SocialMediaError implements ErrorCode {
	SOCIAL_MEDIA_UNSUPPORTED(HttpStatus.BAD_REQUEST, "지원하지 않는 URL입니다."),
	SOCIAL_MEDIA_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 소셜 미디어를 찾을 수 없습니다."),
	SOCIAL_MEDIA_PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 소셜 미디어 장소를 찾을 수 없습니다."),
	SOCIAL_MEDIA_INVALID_STATUS(HttpStatus.INTERNAL_SERVER_ERROR, "현재 상태(%s)에서는 작업을 수행할 수 없습니다. (필요 상태: %s)");

	private final HttpStatus httpStatus;
	private final String message;
}
