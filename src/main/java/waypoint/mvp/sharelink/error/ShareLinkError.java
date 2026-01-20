package waypoint.mvp.sharelink.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum ShareLinkError implements ErrorCode {
	INVALID_INVITATION_LINK(HttpStatus.BAD_REQUEST, "유효하지 않은 초대 링크입니다."),
	EXPIRED_INVITATION_LINK(HttpStatus.BAD_REQUEST, "만료된 초대 링크입니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
