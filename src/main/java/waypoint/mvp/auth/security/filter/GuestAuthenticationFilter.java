package waypoint.mvp.auth.security.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.GuestPrincipal;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.sharelink.application.ShareLinkService;

@Component
@RequiredArgsConstructor
public class GuestAuthenticationFilter extends OncePerRequestFilter {
	private static final AntPathMatcher matcher = new AntPathMatcher();
	private static final List<String> INCLUDE_PATHS = Arrays.asList(
		"/collections/**",
		"/plan/**"
	);

	private final ShareLinkService shareLinkService;

	@Value("${waypoint.cookie.guest-access-token-name}")
	private String guestCookieName;

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain)
		throws ServletException, IOException {

		try {
			authenticateGuestFromCookie(request);
		} catch (BusinessException e) {
			// Guest 인증 실패는 정상 흐름이므로, SecurityContext를 비워둔 채로 다음 필터로 진행
		}

		filterChain.doFilter(request, response);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			return true;
		}

		if (!"GET".equalsIgnoreCase(request.getMethod())) {
			return true;
		}

		String uri = request.getRequestURI();
		return INCLUDE_PATHS.stream()
			.noneMatch(pattern -> matcher.match(pattern, uri));
	}


	private void authenticateGuestFromCookie(HttpServletRequest request) {
		findGuestCookie(request)
			.map(Cookie::getValue)
			.map(shareLinkService::findValidLink)
			.map(GuestPrincipal::from)
			.ifPresent(this::setAuthentication);
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
