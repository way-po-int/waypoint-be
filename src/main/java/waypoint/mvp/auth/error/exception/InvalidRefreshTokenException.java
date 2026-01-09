package waypoint.mvp.auth.error.exception;

import io.jsonwebtoken.JwtException;
import waypoint.mvp.auth.error.AuthError;

public class InvalidRefreshTokenException extends JwtException {

	public InvalidRefreshTokenException() {
		super(AuthError.INVALID_REFRESH_TOKEN.getMessage());
	}
}
