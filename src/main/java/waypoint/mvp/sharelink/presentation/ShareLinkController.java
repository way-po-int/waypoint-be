package waypoint.mvp.sharelink.presentation;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.UserInfo;
import waypoint.mvp.sharelink.application.ShareLinkService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/invite")
public class ShareLinkController {

	private final ShareLinkService shareLinkService;

	@GetMapping("/{code}")
	public ResponseEntity<Void> handleInvitation(@PathVariable String code, @AuthenticationPrincipal UserInfo userInfo, HttpServletResponse response) {
		String redirectUrl = shareLinkService.processInvitationLink(code, userInfo, response);
		return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectUrl)).build();
	}
}
