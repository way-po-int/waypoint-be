package waypoint.mvp.user.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum UserError implements ErrorCode {
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User를 찾을 수 업습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
