package waypoint.mvp.collection.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum CollectionError implements ErrorCode {
	COLLECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Collection을 찾을 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
