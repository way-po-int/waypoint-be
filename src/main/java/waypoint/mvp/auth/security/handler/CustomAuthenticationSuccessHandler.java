package waypoint.mvp.auth.security.handler;


import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import waypoint.mvp.auth.application.AuthService;
import waypoint.mvp.auth.security.jwt.TokenInfo;
import waypoint.mvp.auth.security.principal.CustomOidcUser;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.global.util.CookieUtils;
import waypoint.mvp.sharelink.application.ShareLinkService;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final AuthService authService;
	private final CookieUtils cookieUtils;
	private final ShareLinkService shareLinkService;

	@Value("${spring.security.oauth2.redirect-uri}")
	private String redirectUri;

	@Value("${waypoint.cookie.guest-access-token-name}")
	private String guestCookieName;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {

		TokenInfo refreshTokenInfo = authService.generateRefreshToken(authentication);
		ResponseCookie cookie = cookieUtils.createRefreshToken(refreshTokenInfo.token());
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

		CustomOidcUser oidcUser = (CustomOidcUser)authentication.getPrincipal();
		log.info("소셜 로그인 성공: userId={}, provider={}", oidcUser.getId(), oidcUser.getProvider());

		// 게스트 쿠키가 있는 경우 게스트 초대처리
		Optional<Cookie> guestCookie = cookieUtils.getCookie(request, guestCookieName);
		if (guestCookie.isPresent()) {
			String shareLinkCode = guestCookie.get().getValue();
			try {
				shareLinkService.acceptInvitation(shareLinkCode, oidcUser.getId());
				log.info("게스트 초대 자동 수락 성공: userId={}, shareLinkCode={}", oidcUser.getId(), shareLinkCode);
			} catch (BusinessException e) {
				log.warn("게스트 초대 자동 수락 중 비즈니스 예외 발생 (로그인 흐름 유지): {}", e.getMessage());
			} catch (Exception e) {
				log.error("게스트 초대 자동 수락 중 알 수 없는 예외 발생", e);
			}

			response.addHeader(HttpHeaders.SET_COOKIE, cookieUtils.deleteCookie(guestCookieName).toString());
		}

		String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
			.build().toUriString();

		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}
}
