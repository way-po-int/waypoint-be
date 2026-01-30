package waypoint.mvp.plan.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum PlanError implements ErrorCode {
	Plan_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 플랜을 찾을 수 없습니다."),
	INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "종료일은 시작일보다 빠를 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
