package waypoint.mvp.collection.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum CollectionPlaceError implements ErrorCode {

	COLLECTION_PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "컬렉션에 등록된 장소를 찾을 수 없습니다."),
	COLLECTION_PLACE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 컬렉션에 추가된 장소입니다."),
	INVALID_PLACE_ID(HttpStatus.BAD_REQUEST, "placeId 형식이 올바르지 않습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
