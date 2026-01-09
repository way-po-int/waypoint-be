package waypoint.mvp.auth.application.dto;

import waypoint.mvp.auth.security.jwt.TokenInfo;

public record AuthTokens(
	TokenInfo accessToken,
	TokenInfo refreshToken
) {
	public static AuthTokens of(TokenInfo accessToken, TokenInfo refreshToken) {
		return new AuthTokens(accessToken, refreshToken);
	}
}
