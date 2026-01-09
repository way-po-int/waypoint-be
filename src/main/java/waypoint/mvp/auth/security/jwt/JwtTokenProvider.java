package waypoint.mvp.auth.security.jwt;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import waypoint.mvp.auth.security.principal.UserInfo;

@Component
public class JwtTokenProvider {

	private static final String TOKEN = "token";

	private final SecretKey key;
	private final long accessExpiresIn;
	private final long refreshExpiresIn;

	public JwtTokenProvider(
		@Value("${jwt.secret}") String secretKey,
		@Value("${jwt.access-expires-in}") long accessExpiresIn,
		@Value("${jwt.refresh-expires-in}") long refreshExpiresIn
	) {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		this.key = Keys.hmacShaKeyFor(keyBytes);
		this.accessExpiresIn = accessExpiresIn;
		this.refreshExpiresIn = refreshExpiresIn;
	}

	public TokenInfo generateAccessToken(UserInfo userInfo) {
		Instant expiresAt = Instant.now().plusSeconds(accessExpiresIn);
		String accessToken = Jwts.builder()
			.subject(userInfo.id().toString())
			.claim(TOKEN, JwtType.ACCESS.getValue())
			.expiration(Date.from(expiresAt))
			.signWith(key)
			.compact();
		return TokenInfo.of(accessToken, expiresAt, accessExpiresIn);
	}

	public TokenInfo generateRefreshToken(UserInfo userInfo) {
		Instant expiresAt = Instant.now().plusSeconds(refreshExpiresIn);
		String refreshToken = Jwts.builder()
			.id(UUID.randomUUID().toString())
			.subject(userInfo.id().toString())
			.claim(TOKEN, JwtType.REFRESH.getValue())
			.expiration(Date.from(expiresAt))
			.signWith(key)
			.compact();
		return TokenInfo.of(refreshToken, expiresAt, refreshExpiresIn);
	}

	public Authentication getAuthentication(String token) {
		Claims claims = parseClaims(token);
		return new UsernamePasswordAuthenticationToken(UserInfo.from(claims), null, Collections.emptyList());
	}

	public JwtCode validateAccessToken(String accessToken) {
		return validateToken(accessToken, JwtType.ACCESS);
	}

	public JwtCode validateRefreshToken(String refreshToken) {
		return validateToken(refreshToken, JwtType.REFRESH);
	}

	private JwtCode validateToken(String token, JwtType jwtType) {
		try {
			Claims claims = Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
			return jwtType.getValue().equals(claims.get(TOKEN, String.class))
				? JwtCode.VALID_TOKEN
				: JwtCode.INVALID_TOKEN;
		} catch (ExpiredJwtException e) {
			return JwtCode.EXPIRED_TOKEN;
		} catch (JwtException | IllegalArgumentException e) {
			return JwtCode.INVALID_TOKEN;
		}
	}

	private Claims parseClaims(String token) {
		try {
			return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
		} catch (ExpiredJwtException e) {
			return e.getClaims();
		}
	}
}
