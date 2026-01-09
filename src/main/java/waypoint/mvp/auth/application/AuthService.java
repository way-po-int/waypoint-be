package waypoint.mvp.auth.application;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.application.dto.AuthTokens;
import waypoint.mvp.auth.domain.RefreshToken;
import waypoint.mvp.auth.error.exception.ExpiredRefreshTokenException;
import waypoint.mvp.auth.error.exception.InvalidRefreshTokenException;
import waypoint.mvp.auth.infrastructure.persistence.RefreshTokenRepository;
import waypoint.mvp.auth.security.jwt.JwtCode;
import waypoint.mvp.auth.security.jwt.JwtTokenProvider;
import waypoint.mvp.auth.security.jwt.TokenInfo;
import waypoint.mvp.auth.security.principal.UserInfo;
import waypoint.mvp.auth.util.HashUtils;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenRepository refreshTokenRepository;

	public AuthTokens reissue(String refreshToken) {
		JwtCode jwtCode = jwtTokenProvider.validateRefreshToken(refreshToken);
		if (JwtCode.INVALID_TOKEN == jwtCode) {
			throw new InvalidRefreshTokenException();
		}

		String hashedRefreshToken = HashUtils.generateHash(refreshToken);
		RefreshToken savedToken = refreshTokenRepository.findByToken(hashedRefreshToken)
			.orElseThrow(InvalidRefreshTokenException::new);

		refreshTokenRepository.delete(savedToken);
		if (savedToken.isExpired()) {
			throw new ExpiredRefreshTokenException();
		}

		Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
		UserInfo userInfo = UserInfo.from(authentication.getPrincipal());
		return AuthTokens.of(
			jwtTokenProvider.generateAccessToken(userInfo),
			generateRefreshToken(authentication)
		);
	}

	public TokenInfo generateRefreshToken(Authentication authentication) {
		UserInfo userInfo = UserInfo.from(authentication.getPrincipal());
		TokenInfo tokenInfo = jwtTokenProvider.generateRefreshToken(userInfo);

		RefreshToken refreshToken = RefreshToken.create(
			userInfo.id(),
			HashUtils.generateHash(tokenInfo.token()),
			tokenInfo.expiresAt()
		);

		refreshTokenRepository.save(refreshToken);
		return tokenInfo;
	}
}
