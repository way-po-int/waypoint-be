package waypoint.mvp.place.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum PlaceExternalError implements ErrorCode {
	PLACE_EXTERNAL_CONNECT_FAILED(HttpStatus.BAD_GATEWAY, "외부 지도 API 연결에 실패했습니다."),
	PLACE_EXTERNAL_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "외부 지도 API 응답이 지연되었습니다."),
	PLACE_EXTERNAL_HTTP_ERROR(HttpStatus.BAD_GATEWAY, "외부 지도 API 응답이 비정상입니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
