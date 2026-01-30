package waypoint.mvp.collection.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum CollectionError implements ErrorCode {
	COLLECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 컬렉션을 찾을 수 없습니다."),
	FORBIDDEN_NOT_OWNER(HttpStatus.FORBIDDEN, "컬렉션의 소유자만 이 작업을 수행할 수 있습니다."),
	MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 컬렉션에 속한 멤버입니다."),
	FORBIDDEN_NOT_MEMBER(HttpStatus.FORBIDDEN, "컬렉션의 멤버만 이 작업을 수행할 수 있습니다."),
	FORBIDDEN_NOT_GUEST(HttpStatus.FORBIDDEN, "컬렉션을 조회할 수 있는 권한이 없습니다."),
	NEED_TO_DELEGATE_OWNERSHIP(HttpStatus.CONFLICT, "소유자는 소유권을 다른 멤버에게 위임해야 탈퇴할 수 있습니다."),
	CANNOT_DELEGATE_OWNERSHIP_TO_SELF(HttpStatus.CONFLICT, "%s에게 소유권을 위임할 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
