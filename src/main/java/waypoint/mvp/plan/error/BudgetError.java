package waypoint.mvp.plan.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum BudgetError implements ErrorCode {

	INVALID_TOTAL_BUDGET(HttpStatus.BAD_REQUEST, "총 예산은 0 이상이어야 합니다."),
	INVALID_TRAVELER_COUNT(HttpStatus.BAD_REQUEST, "여행 인원은 1 이상이어야 합니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
