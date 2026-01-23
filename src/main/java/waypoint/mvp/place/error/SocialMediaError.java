package waypoint.mvp.place.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum SocialMediaError implements ErrorCode {
	UNSUPPORTED_SOCIAL_MEDIA(HttpStatus.BAD_REQUEST, "지원하지 않는 URL입니다."),
	DUPLICATE_EXTRACTION_JOB(HttpStatus.CONFLICT, "이미 요청하신 URL입니다. 작업이 완료될 때까지 기다려 주세요.");

	private final HttpStatus httpStatus;
	private final String message;
}
