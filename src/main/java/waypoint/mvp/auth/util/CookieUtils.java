package waypoint.mvp.auth.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {

	public static final String REFRESH_TOKEN = "refresh_token";
	private static final String PATH = "/";

	private final boolean secure;
	private final String sameSite;
	private final long refreshTokenMaxAge;

	public CookieUtils(
		@Value("${cookie.secure}") boolean secure,
		@Value("${cookie.same-site}") String sameSite,
		@Value("${jwt.refresh-expires-in}") long refreshExpiresIn
	) {
		this.secure = secure;
		this.sameSite = sameSite;
		this.refreshTokenMaxAge = refreshExpiresIn;
	}

	public ResponseCookie createRefreshToken(String refreshToken) {
		return createCookie(REFRESH_TOKEN, refreshToken, refreshTokenMaxAge);
	}

	public ResponseCookie deleteRefreshToken() {
		return deleteCookie(REFRESH_TOKEN);
	}

	public ResponseCookie createCookie(String cookieName, String value, long maxAge) {
		return ResponseCookie.from(cookieName, value)
			.httpOnly(true)
			.secure(secure)
			.path(PATH)
			.maxAge(maxAge)
			.sameSite(sameSite)
			.build();
	}

	public ResponseCookie deleteCookie(String cookieName) {
		return ResponseCookie.from(cookieName, "")
			.httpOnly(true)
			.secure(secure)
			.path(PATH)
			.maxAge(0)
			.sameSite(sameSite)
			.build();
	}
}
