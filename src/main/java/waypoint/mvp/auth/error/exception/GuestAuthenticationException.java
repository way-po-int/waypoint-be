package waypoint.mvp.auth.error.exception;

import org.springframework.security.core.AuthenticationException;

public class GuestAuthenticationException extends AuthenticationException {
	public GuestAuthenticationException(String message) {
		super(message);
	}
	public GuestAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
