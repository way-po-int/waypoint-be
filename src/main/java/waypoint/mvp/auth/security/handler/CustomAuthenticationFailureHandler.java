package waypoint.mvp.auth.security.handler;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	@Value("${spring.security.oauth2.redirect-uri}")
	private String redirectUri;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException exception) throws IOException, ServletException {

		String errorCode = "unknown_error";
		if (exception instanceof OAuth2AuthenticationException e) {
			// https://datatracker.ietf.org/doc/html/rfc6749
			errorCode = e.getError().getErrorCode();
		}

		log.warn("소셜 로그인 실패: errorCode={}, message={}", errorCode, exception.getMessage());

		String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
			.queryParam("error_code", errorCode)
			.build().toUriString();

		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}
}
