package waypoint.mvp.plan.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum ExpenseError implements ErrorCode {

	EXPENSE_NOT_FOUND(HttpStatus.NOT_FOUND, "지출을 찾을 수 없습니다."),
	INVALID_ITEM_COST(HttpStatus.BAD_REQUEST, "금액은 0 이상이어야 합니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
