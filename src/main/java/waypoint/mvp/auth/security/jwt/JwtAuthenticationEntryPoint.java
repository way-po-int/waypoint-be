package waypoint.mvp.auth.security.jwt;

import java.io.IOException;
import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.error.AuthError;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException authException) throws IOException, ServletException {

		JwtCode jwtCode = request.getAttribute("jwtCode") instanceof JwtCode code
			? code : JwtCode.MISSING_TOKEN;

		// https://datatracker.ietf.org/doc/html/rfc6750#section-3.1
		response.setHeader("WWW-Authenticate", "Bearer realm=\"api\", error=\"invalid_token\"");
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setCharacterEncoding("UTF-8");

		String message = switch (jwtCode) {
			case EXPIRED_TOKEN -> AuthError.EXPIRED_TOKEN.getMessage();
			case INVALID_TOKEN -> AuthError.INVALID_TOKEN.getMessage();
			default -> AuthError.UNAUTHORIZED.getMessage();
		};
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, message);
		problemDetail.setInstance(URI.create(request.getRequestURI()));
		problemDetail.setProperty("code", jwtCode.name());
		objectMapper.writeValue(response.getWriter(), problemDetail);
	}
}
