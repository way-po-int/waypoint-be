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
import waypoint.mvp.auth.security.principal.UserInfo;
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
	public ResponseEntity<Void> handleInvitation(
		@PathVariable String code, @AuthenticationPrincipal UserInfo userInfo
	) {
		InvitationResult result = shareLinkService.processInvitationLink(code, userInfo);

		return switch (result) {
			case InvitationResult.GuestInvitation guest -> {
				ResponseCookie cookie = cookieUtils.createCookie(guestCookieName, guest.shareLinkCode(),
					guestCookieMaxAgeSeconds);
				yield ResponseEntity.status(HttpStatus.FOUND)
					.header(HttpHeaders.SET_COOKIE, cookie.toString())
					.location(URI.create(guest.redirectUrl()))
					.build();
			}
			case InvitationResult.UserInvitation user -> ResponseEntity.status(HttpStatus.FOUND)
				.location(URI.create(user.redirectUrl()))
				.build();
		};
	}
}
