package waypoint.mvp.plan.domain.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum TimeBlockError implements ErrorCode {
	INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "종료시간은 시작시간보다 빠를 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
