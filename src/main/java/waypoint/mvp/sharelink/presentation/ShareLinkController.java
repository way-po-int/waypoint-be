package waypoint.mvp.sharelink.presentation;

import java.net.URI;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.jwt.JwtTokenProvider;
import waypoint.mvp.auth.security.principal.AuthPrincipal;
import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.global.util.CookieUtils;
import waypoint.mvp.sharelink.application.ShareLinkService;
import waypoint.mvp.sharelink.application.ShareLinkService.InvitationResult;

@RestController
@RequiredArgsConstructor
@RequestMapping("/invite")
public class ShareLinkController {

	private final ShareLinkService shareLinkService;
	private final CookieUtils cookieUtils;
	private final JwtTokenProvider jwtTokenProvider;

	@Value("${waypoint.cookie.guest-access-token-name}")
	private String guestCookieName;

	@Value("${waypoint.cookie.guest-access-token-max-age-seconds}")
	private long guestCookieMaxAgeSeconds;

	@Value("${waypoint.cookie.refresh-token-name}")
	private String refreshTokenName;

	@GetMapping("/{code}")
	public ResponseEntity<Void> handleInvitation(
		@PathVariable String code,
		@AuthenticationPrincipal AuthPrincipal user,
		HttpServletRequest request
	) {
		AuthPrincipal authenticatedUser = user;

		if (authenticatedUser == null) {
			Optional<Cookie> refreshTokenCookie = cookieUtils.getCookie(request, refreshTokenName);
			if (refreshTokenCookie.isPresent()) { // refreshToken이 없다면 Guest로 접속
				String refreshToken = refreshTokenCookie.get().getValue();
				Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
				authenticatedUser = UserPrincipal.from(authentication.getPrincipal());
			}
		}

		InvitationResult result = shareLinkService.processInvitationLink(code, authenticatedUser);

		return switch (result) {
			case InvitationResult.GuestInvitation(var redirectUrl, var shareLinkCode) -> {
				ResponseCookie cookie = cookieUtils.createCookie(guestCookieName, shareLinkCode,
					guestCookieMaxAgeSeconds);
				yield ResponseEntity.status(HttpStatus.FOUND)
					.header(HttpHeaders.SET_COOKIE, cookie.toString())
					.location(URI.create(redirectUrl))
					.build();
			}
			case InvitationResult.UserInvitation(var redirectUrl) -> ResponseEntity.status(HttpStatus.FOUND)
				.location(URI.create(redirectUrl))
				.build();
		};
	}
}
