package waypoint.mvp.auth.presentation;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.application.AuthService;
import waypoint.mvp.auth.application.dto.AuthTokens;
import waypoint.mvp.auth.presentation.dto.response.TokenResponse;
import waypoint.mvp.auth.util.CookieUtils;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final CookieUtils cookieUtils;

	@PostMapping("/reissue")
	public ResponseEntity<TokenResponse> reissue(
		@CookieValue(name = CookieUtils.REFRESH_TOKEN, required = false) String refreshToken
	) {
		AuthTokens authTokens = authService.reissue(refreshToken);
		ResponseCookie cookie = cookieUtils.createRefreshToken(authTokens.refreshToken().token());
		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, cookie.toString())
			.body(TokenResponse.of(authTokens.accessToken()));
	}
}
