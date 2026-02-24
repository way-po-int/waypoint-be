package waypoint.mvp.auth.security.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.jwt.JwtCode;
import waypoint.mvp.auth.security.jwt.JwtTokenProvider;
import waypoint.mvp.auth.security.jwt.JwtType;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String AUTH_TYPE = "Bearer ";

	private final JwtTokenProvider jwtTokenProvider;

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain) throws ServletException, IOException {

		String token = resolveToken(request);

		if (token != null) {
			JwtCode jwtCode = jwtTokenProvider.validateAccessToken(token);
			if (JwtCode.VALID_TOKEN == jwtCode) {
				JwtType jwtType = jwtTokenProvider.resolveJwtType(token);
				Authentication authentication = jwtTokenProvider.getAuthentication(token);

				if (authentication instanceof UsernamePasswordAuthenticationToken authToken) {
					authToken.setDetails(jwtType);
				}
				request.setAttribute("jwtType", jwtType);

				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
			request.setAttribute("jwtCode", jwtCode);
		}

		filterChain.doFilter(request, response);
	}

	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(AUTH_TYPE)) {
			return bearerToken.substring(AUTH_TYPE.length());
		}
		return null;
	}
}
