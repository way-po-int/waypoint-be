package waypoint.mvp.global.error;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
	String name();

	HttpStatus getHttpStatus();

	String getMessage();

	default String getMessage(Object... args) {
		return getMessage().formatted(args);
	}
}
