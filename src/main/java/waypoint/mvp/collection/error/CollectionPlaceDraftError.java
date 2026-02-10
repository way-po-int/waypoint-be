package waypoint.mvp.collection.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum CollectionPlaceDraftError implements ErrorCode {

	DRAFT_IN_PROGRESS(HttpStatus.CONFLICT, "이미 진행 중인 작업이 있습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
