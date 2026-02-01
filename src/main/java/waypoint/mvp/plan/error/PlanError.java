package waypoint.mvp.plan.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum PlanError implements ErrorCode {
	PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 플랜을 찾을 수 없습니다."),
	INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "종료일은 시작일보다 빠를 수 없습니다."),
	FORBIDDEN_NOT_OWNER(HttpStatus.FORBIDDEN, "플랜의 소유자만 이 작업을 수행할 수 있습니다."),
	MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 플랜에 속한 멤버입니다."),
	FORBIDDEN_NOT_MEMBER(HttpStatus.FORBIDDEN, "플랜의 멤버만 이 작업을 수행할 수 있습니다."),
	NEED_TO_DELEGATE_OWNERSHIP(HttpStatus.CONFLICT, "소유자는 소유권을 다른 멤버에게 위임해야 탈퇴할 수 있습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
