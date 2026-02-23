package waypoint.mvp.plan.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum BlockError implements ErrorCode {
	BLOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 블록을 찾을 수 없습니다."),
	BLOCK_NOT_IN_TIME_BLOCK(HttpStatus.BAD_REQUEST, "해당 블록은 이 타임블록에 속하지 않습니다."),
	CANDIDATE_COUNT_INSUFFICIENT(HttpStatus.BAD_REQUEST, "장소가 1개뿐이라면 후보지 확정을 사용할 수 없습니다."),
	NOT_SELECTED(HttpStatus.BAD_REQUEST, "확정된 후보가 없습니다."),
	CANNOT_ADD_CANDIDATE_TO_FREE_BLOCK(HttpStatus.BAD_REQUEST, "자유 시간 블록에는 후보지를 추가할 수 없습니다."),
	BLOCK_CREATE_TYPE_UNSUPPORTED(HttpStatus.BAD_REQUEST, "블록 생성은 %s 타입만 가능합니다."),
	TIME_BLOCK_EXACT_DUPLICATE(HttpStatus.CONFLICT, "동일한 시간대에 이미 일정이 있어요, 후보지로 추가해 주세요"),
	TIME_BLOCK_OVERLAP(HttpStatus.CONFLICT, "겹치는 일정이 있어요");

	private final HttpStatus httpStatus;
	private final String message;
}
