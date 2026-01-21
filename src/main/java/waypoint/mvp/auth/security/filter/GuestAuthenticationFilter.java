package waypoint.mvp.auth.security.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.error.exception.GuestAuthenticationException;
import waypoint.mvp.auth.security.principal.GuestPrincipal;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.sharelink.application.ShareLinkService;

@Component
@RequiredArgsConstructor
public class GuestAuthenticationFilter extends OncePerRequestFilter {

	private final ShareLinkService shareLinkService;

	@Value("${waypoint.cookie.guest-access-token-name}")
	private String guestCookieName;

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain)
		throws ServletException, IOException {

		if (isNotAuthenticated()) {
			authenticateGuestFromCookie(request);
		}

		filterChain.doFilter(request, response);
	}

	private boolean isNotAuthenticated() {
		return SecurityContextHolder.getContext().getAuthentication() == null;
	}

	private void authenticateGuestFromCookie(HttpServletRequest request) throws GuestAuthenticationException {
		try {
			findGuestCookie(request)
				.map(Cookie::getValue)
				.map(shareLinkService::findValidLink)
				.map(GuestPrincipal::from)
				.ifPresent(this::setAuthentication);
		} catch (BusinessException e) {
			/** 예상된 인증 실패(BusinessException)는 AuthenticationException으로 변환하여 401 에러를 유도하고,
			 *  예상치 못한 코드 버그(RuntimeException)는 500 에러를 발생**/
			throw new GuestAuthenticationException(e.getMessage(), e);
		}
	}

	private void setAuthentication(GuestPrincipal principal) {
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken(principal, null, null)
		);
	}

	private Optional<Cookie> findGuestCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return Optional.empty();
		}
		return Arrays.stream(cookies)
			.filter(cookie -> cookie.getName().equals(guestCookieName))
			.findFirst();
	}
}
