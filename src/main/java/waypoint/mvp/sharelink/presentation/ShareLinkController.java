package waypoint.mvp.sharelink.presentation;

import java.net.URI;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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
import lombok.extern.slf4j.Slf4j;
import waypoint.mvp.auth.security.jwt.JwtCode;
import waypoint.mvp.auth.security.jwt.JwtTokenProvider;
import waypoint.mvp.auth.security.principal.AuthPrincipal;
import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.global.util.CookieUtils;
import waypoint.mvp.sharelink.application.ShareLinkService;
import waypoint.mvp.sharelink.application.ShareLinkService.InvitationResult;
import waypoint.mvp.sharelink.error.ShareLinkError;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/s")
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

	@Value("${waypoint.frontend.base-url}")
	private String frontendBaseUrl;

	@Value("${waypoint.frontend.error-page-path}")
	private String errorPagePath;

	@GetMapping("/{code}")
	public ResponseEntity<Void> handleInvitation(
		@PathVariable String code,
		@AuthenticationPrincipal AuthPrincipal user,
		HttpServletRequest request
	) {
		try {
			AuthPrincipal authenticatedUser = user;

			if (authenticatedUser == null) {
				Optional<Cookie> refreshTokenCookie = cookieUtils.getCookie(request, refreshTokenName);
				if (refreshTokenCookie.isPresent()) {
					String refreshToken = refreshTokenCookie.get().getValue();
					if (jwtTokenProvider.validateRefreshToken(refreshToken) == JwtCode.VALID_TOKEN) {
						Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
						authenticatedUser = UserPrincipal.from(authentication.getPrincipal());
					}
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
		} catch (BusinessException e) {
			log.warn("초대 링크 처리 중 비즈니스 예외가 발생했습니다. code: {}, error: {}", code, e.getMessage());
			// 에러 타입에 따라 404 페이지로 리다이렉트
			ShareLinkError errorCode = extractErrorCode(e);
			String errorParam = determineErrorParam(errorCode);
			String errorRedirectUrl = buildErrorRedirectUrl(errorParam);
			return ResponseEntity.status(HttpStatus.FOUND)
				.location(URI.create(errorRedirectUrl))
				.build();
		} catch (Exception e) {
			log.error("초대 링크 처리 중 예상치 못한 시스템 오류가 발생했습니다. code: {}", code, e);
			return ResponseEntity.status(HttpStatus.FOUND)
				.location(URI.create(frontendBaseUrl + errorPagePath))
				.build();
		}
	}

	private ShareLinkError extractErrorCode(BusinessException e) {
		// ProblemDetail의 properties에서 code를 추출 시도
		Object codeProperty = e.getBody().getProperties().get("code");
		if (codeProperty instanceof String) {
			String errorCode = (String)codeProperty;
			try {
				return ShareLinkError.valueOf(errorCode);
			} catch (IllegalArgumentException ex) {
				// 유효하지 않은 에러 코드면 기본값 반환
				return ShareLinkError.INVALID_LINK;
			}
		}

		// HTTP 상태 코드로 기본 에러 타입 추정
		HttpStatusCode statusCode = e.getStatusCode();
		if (statusCode instanceof HttpStatus httpStatus && httpStatus == HttpStatus.NOT_FOUND) {
			return ShareLinkError.INVALID_LINK;
		}
		return ShareLinkError.INVALID_LINK;
	}

	private String determineErrorParam(ShareLinkError errorCode) {
		return switch (errorCode) {
			case INVALID_LINK -> "invalid_link";
			case EXPIRED_LINK -> "expired_link";
		};
	}

	private String buildErrorRedirectUrl(String errorParam) {
		return frontendBaseUrl + errorPagePath + "?error=" + errorParam;
	}
}
