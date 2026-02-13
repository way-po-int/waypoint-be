package waypoint.mvp.plan.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum BlockError implements ErrorCode {
	PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 블록을 찾을 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
