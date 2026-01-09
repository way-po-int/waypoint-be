package waypoint.mvp.auth.application;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.domain.RefreshToken;
import waypoint.mvp.auth.infrastructure.persistence.RefreshTokenRepository;
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
