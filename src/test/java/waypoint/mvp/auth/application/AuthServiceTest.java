package waypoint.mvp.auth.application;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import waypoint.mvp.auth.application.dto.AuthTokens;
import waypoint.mvp.auth.domain.RefreshToken;
import waypoint.mvp.auth.error.exception.ExpiredRefreshTokenException;
import waypoint.mvp.auth.error.exception.InvalidRefreshTokenException;
import waypoint.mvp.auth.infrastructure.persistence.RefreshTokenRepository;
import waypoint.mvp.auth.security.jwt.JwtTokenProvider;
import waypoint.mvp.auth.security.jwt.TokenInfo;
import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.auth.util.HashUtils;
import waypoint.mvp.global.annotation.ServiceTest;
import waypoint.mvp.user.infrastructure.persistence.UserRepository;

@ServiceTest
class AuthServiceTest {

	@Autowired
	private AuthService authService;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	private RefreshTokenRepository refreshTokenRepository;

	@Autowired
	private UserRepository userRepository;

	@Test
	@DisplayName("토큰 재발급에 성공한다.")
	void reissue_success() {
		// given
		var savedUser = userRepository.save(
			waypoint.mvp.user.domain.User.create(
				waypoint.mvp.user.domain.SocialAccount.create(waypoint.mvp.user.domain.Provider.GOOGLE,
					"provider-id-1"),
				"테스터",
				"",
				"test@test.com"
			)
		);
		Long userId = savedUser.getId();
		UserPrincipal userInfo = new UserPrincipal(userId);
		TokenInfo oldRefreshToken = jwtTokenProvider.generateRefreshToken(userInfo);

		String hashedOldRefreshToken = HashUtils.generateHash(oldRefreshToken.token());
		RefreshToken refreshToken = RefreshToken.create(
			userId,
			hashedOldRefreshToken,
			oldRefreshToken.expiresAt()
		);
		refreshTokenRepository.save(refreshToken);

		// when
		AuthTokens authTokens = authService.reissue(oldRefreshToken.token());

		// then
		TokenInfo newAccessToken = authTokens.accessToken();
		TokenInfo newRefreshToken = authTokens.refreshToken();
		String hashedNewRefreshToken = HashUtils.generateHash(newRefreshToken.token());

		Optional<RefreshToken> savedToken = refreshTokenRepository.findByToken(hashedNewRefreshToken);
		assertThat(savedToken).isPresent();
		assertThat(savedToken.get().getUserId()).isEqualTo(userId);

		Optional<RefreshToken> oldTokenCheck = refreshTokenRepository.findByToken(hashedOldRefreshToken);
		assertThat(oldTokenCheck).isEmpty();

		assertThat(hashedNewRefreshToken).isNotEqualTo(hashedOldRefreshToken);
		assertThat(newAccessToken.token()).isNotNull();
	}

	@Test
	@DisplayName("유효하지 않은 토큰이 들어오면 예외가 발생한다.")
	void reissue_invalidToken_fail() {
		// given
		String invalidToken = "invalid_token";

		// when & then
		assertThatThrownBy(() -> authService.reissue(invalidToken))
			.isInstanceOf(InvalidRefreshTokenException.class);
	}

	@Test
	@DisplayName("DB에 리프레시 토큰이 없다면 예외가 발생한다.")
	void reissue_notFound_fail() {
		// given
		UserPrincipal userInfo = new UserPrincipal(1L);
		String refreshToken = jwtTokenProvider.generateRefreshToken(userInfo).token();

		// when & then
		assertThatThrownBy(() -> authService.reissue(refreshToken))
			.isInstanceOf(InvalidRefreshTokenException.class);
	}

	@Test
	@DisplayName("DB에 있는 리프레시 토큰이 만료된 경우 예외가 발생하고, 해당 토큰은 삭제한다.")
	void reissue_expired_fail() {
		// given
		UserPrincipal userInfo = new UserPrincipal(1L);
		TokenInfo tokenInfo = jwtTokenProvider.generateRefreshToken(userInfo);
		String tokenValue = tokenInfo.token();

		String hashedToken = HashUtils.generateHash(tokenValue);
		RefreshToken refreshToken = RefreshToken.create(
			1L,
			hashedToken,
			Instant.now().minusSeconds(1)
		);
		refreshTokenRepository.save(refreshToken);

		// when & then
		assertThatThrownBy(() -> authService.reissue(tokenValue))
			.isInstanceOf(ExpiredRefreshTokenException.class);

		Optional<RefreshToken> deletedToken = refreshTokenRepository.findByToken(hashedToken);
		assertThat(deletedToken).isEmpty();
	}

	@Test
	@DisplayName("DB에는 해싱된 리프레시 토큰이 저장된다.")
	void generateRefreshToken_success() {
		// given
		Long userId = 1L;
		UserPrincipal userInfo = new UserPrincipal(userId);
		Authentication authentication = new UsernamePasswordAuthenticationToken(userInfo, null,
			Collections.emptyList());

		// when
		TokenInfo tokenInfo = authService.generateRefreshToken(authentication);

		// then
		String expectedHash = HashUtils.generateHash(tokenInfo.token());
		Optional<RefreshToken> savedToken = refreshTokenRepository.findByToken(expectedHash);
		assertThat(savedToken).isPresent();
		assertThat(savedToken.get().getToken()).isEqualTo(expectedHash);
		assertThat(savedToken.get().getToken()).isNotEqualTo(tokenInfo.token());
		assertThat(savedToken.get().getUserId()).isEqualTo(userId);
	}

	@Test
	@DisplayName("로그아웃에 성공한다.")
	void logout_success() {
		// given
		Long userId = 1L;
		UserPrincipal userInfo = new UserPrincipal(userId);
		TokenInfo tokenInfo = jwtTokenProvider.generateRefreshToken(userInfo);

		String hashedToken = HashUtils.generateHash(tokenInfo.token());
		RefreshToken refreshToken = RefreshToken.create(
			userId,
			hashedToken,
			tokenInfo.expiresAt()
		);
		refreshTokenRepository.save(refreshToken);

		// when
		authService.logout(userInfo, tokenInfo.token());

		// then
		Optional<RefreshToken> deletedToken = refreshTokenRepository.findByToken(hashedToken);
		assertThat(deletedToken).isEmpty();
	}

	@Test
	@DisplayName("로그아웃 시 토큰 소유자와 로그인 유저가 다르면 토큰을 삭제하지 않는다.")
	void logout_differentUser_doesNotDeleteToken() {
		// given
		Long loginUserId = 1L;
		Long tokenOwnerId = 2L;
		String refreshToken = "refresh_token";
		UserPrincipal userInfo = new UserPrincipal(loginUserId);

		String hashedToken = HashUtils.generateHash(refreshToken);
		Instant expiresAt = Instant.now().plusSeconds(3600);
		RefreshToken savedToken = RefreshToken.create(tokenOwnerId, hashedToken, expiresAt);
		refreshTokenRepository.save(savedToken);

		// when
		authService.logout(userInfo, refreshToken);

		// then
		Optional<RefreshToken> foundToken = refreshTokenRepository.findByToken(hashedToken);
		assertThat(foundToken).isPresent();
	}
}
