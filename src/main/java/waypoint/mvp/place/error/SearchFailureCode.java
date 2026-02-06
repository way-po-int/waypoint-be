package waypoint.mvp.place.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.JobFailureCode;

@Getter
@RequiredArgsConstructor
public enum SearchFailureCode implements JobFailureCode {
	PLACE_DATA_INVALID("유효하지 않은 장소 데이터입니다.", false),
	UNEXPECTED_ERROR("예기치 못한 오류가 발생했습니다.", false),
	PLACES_API_ERROR("Places API 에러 발생", true);

	private final String message;
	private final boolean isRetryable;
}
