package waypoint.mvp.auth.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthError {
	UNAUTHORIZED("인증이 필요합니다."),
	FORBIDDEN("리소스에 접근할 권한이 없습니다."),
	INVALID_TOKEN("유효하지 않은 토큰입니다."),
	EXPIRED_TOKEN("토큰이 만료되었습니다."),
	INVALID_REFRESH_TOKEN("유효하지 않은 리프레시 토큰입니다."),
	EXPIRED_REFRESH_TOKEN("리프레시 토큰이 만료되었습니다.");

	private final String message;
}
