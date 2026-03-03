package waypoint.mvp.global.error.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class GuestAccessDeniedException extends ErrorResponseException {

	public GuestAccessDeniedException() {
		super(
			HttpStatus.FORBIDDEN,
			ProblemDetail.forStatusAndDetail(
				HttpStatus.FORBIDDEN,
				"로그인한 사용자만 접근 가능합니다."
			),
			null
		);
		super.getBody().setProperty("code", "GUEST_FORBIDDEN");
	}
}
