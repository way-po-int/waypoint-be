package waypoint.mvp.auth.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
	USER_WITHDRAWN(HttpStatus.FORBIDDEN, "현재 탈퇴 처리 중인 계정으로는 로그인할 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
