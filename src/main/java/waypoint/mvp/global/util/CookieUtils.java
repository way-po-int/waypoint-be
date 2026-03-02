package waypoint.mvp.global.util;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class CookieUtils {

	private static final String PATH = "/";

	private final boolean secure;
	private final String sameSite;
	private final String domain;
	private final long refreshTokenMaxAge;
	private final String refreshTokenName;

	public CookieUtils(
		@Value("${cookie.secure}") boolean secure,
		@Value("${cookie.same-site}") String sameSite,
		@Value("${waypoint.cookie.domain:#{null}}") String domain,
		@Value("${jwt.refresh-expires-in}") long refreshExpiresIn,
		@Value("${waypoint.cookie.refresh-token-name}") String refreshTokenName
	) {
		this.secure = secure;
		this.sameSite = sameSite;
		this.domain = domain;
		this.refreshTokenMaxAge = refreshExpiresIn;
		this.refreshTokenName = refreshTokenName;
	}

	public ResponseCookie createRefreshToken(String refreshToken) {
		return createCookie(refreshTokenName, refreshToken, refreshTokenMaxAge);
	}

	public ResponseCookie deleteRefreshToken() {
		return deleteCookie(refreshTokenName);
	}

	public ResponseCookie createCookie(String cookieName, String value, long maxAge) {
		ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookieName, value)
			.httpOnly(true)
			.secure(secure)
			.path(PATH)
			.maxAge(maxAge)
			.sameSite(sameSite);
		
		if (domain != null && !domain.isBlank()) {
			builder.domain(domain);
		}
		
		return builder.build();
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

	public Optional<Cookie> getCookie(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();

		if (cookies != null) {
			return Arrays.stream(cookies)
				.filter(cookie -> cookie.getName().equals(name))
				.findFirst();
		}

		return Optional.empty();
	}
}
