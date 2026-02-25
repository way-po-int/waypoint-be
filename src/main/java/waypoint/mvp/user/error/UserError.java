package waypoint.mvp.user.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum UserError implements ErrorCode {
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User를 찾을 수 없습니다."),
	TERMS_ALREADY_ACCEPTED(HttpStatus.CONFLICT, "이미 약관에 동의한 사용자입니다."),
	UNSUPPORTED_IMAGE_CONTENT_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 이미지 형식입니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
