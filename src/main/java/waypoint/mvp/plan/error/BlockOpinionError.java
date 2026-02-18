package waypoint.mvp.plan.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum BlockOpinionError implements ErrorCode {

	BLOCK_OPINION_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 해당 블록에 대한 의견을 작성했습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
