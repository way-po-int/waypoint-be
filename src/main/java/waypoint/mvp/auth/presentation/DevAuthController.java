package waypoint.mvp.auth.presentation;

import java.util.Collections;
import java.util.Objects;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.application.AuthService;
import waypoint.mvp.auth.presentation.dto.response.TokenResponse;
import waypoint.mvp.auth.security.jwt.JwtTokenProvider;
import waypoint.mvp.auth.security.jwt.TokenInfo;
import waypoint.mvp.auth.security.principal.UserInfo;
import waypoint.mvp.auth.util.CookieUtils;
import waypoint.mvp.user.application.UserService;
import waypoint.mvp.user.application.dto.SocialUserProfile;
import waypoint.mvp.user.domain.Provider;
import waypoint.mvp.user.domain.SocialAccount;
import waypoint.mvp.user.domain.User;

@Profile({"local", "dev"})
@RestController
@RequestMapping("/dev/auth")
@RequiredArgsConstructor
class DevAuthController {

	private final AuthService authService;
	private final UserService userService;
	private final JwtTokenProvider jwtTokenProvider;
	private final CookieUtils cookieUtils;

	record LoginRequest(
		@NotNull Provider provider,
		@NotNull String providerId,
		@NotNull String nickname,
		String picture,
		String email) {
	}

	@PostMapping("/login")
	public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
		SocialAccount socialAccount = SocialAccount.create(request.provider, request.providerId);
		SocialUserProfile profile = SocialUserProfile.of(
			socialAccount,
			request.nickname,
			Objects.requireNonNullElse(request.picture, ""),
			Objects.requireNonNullElse(request.email, "test@test.com")
		);
		User user = userService.loadSocialUser(profile);

		UserInfo userInfo = new UserInfo(user.getId());
		Authentication authentication = new UsernamePasswordAuthenticationToken(userInfo, null,
			Collections.emptyList());

		TokenInfo refreshToken = authService.generateRefreshToken(authentication);
		TokenInfo accessToken = jwtTokenProvider.generateAccessToken(userInfo);
		ResponseCookie cookie = cookieUtils.createRefreshToken(refreshToken.token());
		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, cookie.toString())
			.body(TokenResponse.of(accessToken));
	}
}
