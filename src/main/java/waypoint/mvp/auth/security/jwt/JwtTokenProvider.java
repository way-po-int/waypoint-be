package waypoint.mvp.auth.security.jwt;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import waypoint.mvp.auth.domain.Role;
import waypoint.mvp.auth.security.principal.UserPrincipal;

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

	public TokenInfo generateAccessToken(UserPrincipal userInfo) {
		Instant expiresAt = Instant.now().plusSeconds(accessExpiresIn);
		String accessToken = Jwts.builder()
			.subject(userInfo.id().toString())
			.claim(TOKEN, JwtType.ACCESS.getValue())
			.expiration(Date.from(expiresAt))
			.signWith(key)
			.compact();
		return TokenInfo.of(accessToken, expiresAt, accessExpiresIn);
	}

	public TokenInfo generatePreTermsAccessToken(UserPrincipal userInfo) {
		Instant expiresAt = Instant.now().plusSeconds(accessExpiresIn);
		String accessToken = Jwts.builder()
			.subject(userInfo.id().toString())
			.claim(TOKEN, JwtType.PRE_TERMS_ACCESS.getValue())
			.expiration(Date.from(expiresAt))
			.signWith(key)
			.compact();
		return TokenInfo.of(accessToken, expiresAt, accessExpiresIn);
	}

	public TokenInfo generateRefreshToken(UserPrincipal userInfo) {
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

		Role role = JwtType.from(claims.get(TOKEN, String.class)).getRole();
		List<GrantedAuthority> authorities = role != null
			? List.of(new SimpleGrantedAuthority(role.getAuthority()))
			: List.of();

		return new UsernamePasswordAuthenticationToken(
			UserPrincipal.from(claims),
			null,
			authorities
		);
	}

	public JwtCode validateAccessToken(String accessToken) {
		return validateToken(accessToken, JwtType.ACCESS, JwtType.PRE_TERMS_ACCESS);
	}

	public JwtCode validateRefreshToken(String refreshToken) {
		return validateToken(refreshToken, JwtType.REFRESH);
	}

	private JwtCode validateToken(String token, JwtType... jwtTypes) {
		try {
			Claims claims = Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();

			JwtType jwtType = JwtType.from(claims.get(TOKEN, String.class));
			return Arrays.asList(jwtTypes).contains(jwtType)
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
