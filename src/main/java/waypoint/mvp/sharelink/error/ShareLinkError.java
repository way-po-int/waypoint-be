package waypoint.mvp.sharelink.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum ShareLinkError implements ErrorCode {
	INVALID_LINK(HttpStatus.NOT_FOUND, "유효하지 않은 초대 링크입니다."),
	EXPIRED_LINK(HttpStatus.NOT_FOUND, "만료된 초대 링크입니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
