package waypoint.mvp.plan.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum BlockError implements ErrorCode {
	BLOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 블록을 찾을 수 없습니다."),
	CANNOT_ADD_CANDIDATE_TO_FREE_BLOCK(HttpStatus.BAD_REQUEST, "자유 시간 블록에는 후보지를 추가할 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
