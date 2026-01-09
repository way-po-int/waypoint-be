package waypoint.mvp.auth.security.jwt;

import java.time.Instant;

public record TokenInfo(
	String token,
	Instant expiresAt,
	long expiresIn
) {
	public static TokenInfo of(String token, Instant expiresAt, long expiresIn) {
		return new TokenInfo(token, expiresAt, expiresIn);
	}
}
