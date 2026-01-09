package waypoint.mvp.auth.error.exception;

import io.jsonwebtoken.JwtException;
import waypoint.mvp.auth.error.AuthError;

public class ExpiredRefreshTokenException extends JwtException {

	public ExpiredRefreshTokenException() {
		super(AuthError.EXPIRED_REFRESH_TOKEN.getMessage());
	}
}
