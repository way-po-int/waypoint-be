package waypoint.mvp.sharelink.presentation;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.AuthPrincipal;
import waypoint.mvp.global.auth.annotations.AuthLevel;
import waypoint.mvp.global.auth.annotations.Authorize;
import waypoint.mvp.global.util.CookieUtils;
import waypoint.mvp.sharelink.application.ShareLinkService;
import waypoint.mvp.sharelink.application.ShareLinkService.InvitationResult;

@RestController
@RequiredArgsConstructor
@RequestMapping("/invite")
public class ShareLinkController {

	private final ShareLinkService shareLinkService;
	private final CookieUtils cookieUtils;

	@Value("${waypoint.cookie.guest-access-token-name}")
	private String guestCookieName;

	@Value("${waypoint.cookie.guest-access-token-max-age-seconds}")
	private long guestCookieMaxAgeSeconds;

	@GetMapping("/{code}")
	@Authorize(level = AuthLevel.GUEST_OR_MEMBER)
	public ResponseEntity<Void> handleInvitation(
		@PathVariable String code,
		@AuthenticationPrincipal AuthPrincipal user
	) {
		InvitationResult result = shareLinkService.processInvitationLink(code, user);

		return switch (result) {
			case InvitationResult.GuestInvitation(var shareLinkCode, var redirectUrl) -> {
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
