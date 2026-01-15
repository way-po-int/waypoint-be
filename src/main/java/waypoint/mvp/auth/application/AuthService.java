package waypoint.mvp.auth.application;

import java.time.Instant;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AuthService {

	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenRepository refreshTokenRepository;

	@Transactional(noRollbackFor = ExpiredRefreshTokenException.class)
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

		log.info("토큰 재발급 성공: userId={}", userInfo.id());
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

	public void logout(UserInfo userInfo, String refreshToken) {
		if (ObjectUtils.isEmpty(refreshToken)) {
			return;
		}
		String hashedRefreshToken = HashUtils.generateHash(refreshToken);
		refreshTokenRepository.findByToken(hashedRefreshToken).ifPresentOrElse(token -> {
			if (!token.getUserId().equals(userInfo.id())) {
				log.warn("로그아웃 실패: requesterId={}, ownerId={}", userInfo.id(), token.getUserId());
				return;
			}
			refreshTokenRepository.delete(token);
			log.info("로그아웃 성공: userId={}", token.getUserId());
		}, () -> log.info("로그아웃 완료(만료된 리프레시 토큰): userId={}", userInfo.id()));
	}

	public long deleteExpiredTokens() {
		Instant now = Instant.now();
		return refreshTokenRepository.deleteExpiredTokens(now);
	}
}
