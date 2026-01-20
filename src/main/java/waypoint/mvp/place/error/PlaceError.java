package waypoint.mvp.place.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum PlaceError implements ErrorCode {
	PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "Place를 찾을 수 없습니다."),
	PLACE_LOOKUP_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "장소 조회 처리에 실패했습니다."),
	PLACE_ID_NOT_FOUND(HttpStatus.NOT_FOUND, "PlaceId를 찾지 못했습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
