package waypoint.mvp.auth.presentation.dto.response;

import waypoint.mvp.auth.security.jwt.TokenInfo;

public record TokenResponse(
	String accessToken,
	long expiresIn
) {
	public static TokenResponse of(TokenInfo accessToken) {
		return new TokenResponse(
			accessToken.token(),
			accessToken.expiresIn()
		);
	}
}
