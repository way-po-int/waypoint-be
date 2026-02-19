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
	ALREADY_SELECTED(HttpStatus.CONFLICT, "이미 선택된 후보가 있습니다. 후보 변경을 원하시면 확정 취소해 주세요."),
	NOT_SELECTED(HttpStatus.BAD_REQUEST, "확정된 후보가 없습니다."),
	CANNOT_ADD_CANDIDATE_TO_FREE_BLOCK(HttpStatus.BAD_REQUEST, "자유 시간 블록에는 후보지를 추가할 수 없습니다."),
	BLOCK_CREATE_TYPE_UNSUPPORTED(HttpStatus.BAD_REQUEST, "블록 생성은 %s 타입만 가능합니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
