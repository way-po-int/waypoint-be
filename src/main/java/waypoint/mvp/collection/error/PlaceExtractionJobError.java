package waypoint.mvp.collection.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum PlaceExtractionJobError implements ErrorCode {

	JOB_IN_PROGRESS(HttpStatus.CONFLICT, "이미 진행 중인 작업이 있습니다."),
	JOB_NOT_FOUND(HttpStatus.NOT_FOUND, "요청하신 추출 작업을 찾을 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
