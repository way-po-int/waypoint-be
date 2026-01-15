package waypoint.mvp.auth.security.handler;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import waypoint.mvp.auth.application.AuthService;
import waypoint.mvp.auth.security.jwt.TokenInfo;
import waypoint.mvp.auth.security.principal.CustomOidcUser;
import waypoint.mvp.auth.util.CookieUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final AuthService authService;
	private final CookieUtils cookieUtils;

	@Value("${spring.security.oauth2.redirect-uri}")
	private String redirectUri;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {

		TokenInfo refreshTokenInfo = authService.generateRefreshToken(authentication);
		ResponseCookie cookie = cookieUtils.createRefreshToken(refreshTokenInfo.token());
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

		CustomOidcUser oidcUser = (CustomOidcUser)authentication.getPrincipal();
		log.info("소셜 로그인 성공: userId={}, provider={}", oidcUser.getId(), oidcUser.getProvider());

		String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
			.build().toUriString();

		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}
}
