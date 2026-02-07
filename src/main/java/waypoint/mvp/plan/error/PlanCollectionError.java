package waypoint.mvp.plan.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum PlanCollectionError implements ErrorCode {

	PLAN_COLLECTION_ALREADY_EXISTS(HttpStatus.CONFLICT, "해당 컬렉션은 이미 추가되었습니다"),
	PLAN_COLLECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "플랜에 등록된 컬렉션을 찾을 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
