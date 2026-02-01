package waypoint.mvp.plan.application;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum PlanMemberError implements ErrorCode {

	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "컬렉션 멤버를 찾을 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
