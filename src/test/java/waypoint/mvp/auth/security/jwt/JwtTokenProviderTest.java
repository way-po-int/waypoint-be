package waypoint.mvp.auth.security.jwt;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.core.Authentication;

import waypoint.mvp.auth.security.principal.UserInfo;

class JwtTokenProviderTest {

	private static final String JWT_SECRET = "ThisIsATestSecretKeyForJwtWhichIsLongEnough";
	private static final long ACCESS_EXPIRES_IN = 3600;
	private static final long REFRESH_EXPIRES_IN = 604800;

	private JwtTokenProvider jwtTokenProvider;

	@BeforeEach
	void setUp() {
		jwtTokenProvider = new JwtTokenProvider(JWT_SECRET, ACCESS_EXPIRES_IN, REFRESH_EXPIRES_IN);
	}

	@Test
	@DisplayName("Access Token을 발급하고 검증에 성공한다.")
	void generateAccessToken() {
		// given
		UserInfo userInfo = new UserInfo(1L);
		Instant now = Instant.now();

		// when
		TokenInfo accessToken = jwtTokenProvider.generateAccessToken(userInfo);

		// then
		assertThat(accessToken).isNotNull();
		assertThat(jwtTokenProvider.validateAccessToken(accessToken.token()))
			.isEqualTo(JwtCode.VALID_TOKEN);
		assertThat(jwtTokenProvider.validateRefreshToken(accessToken.token()))
			.isEqualTo(JwtCode.INVALID_TOKEN);
		assertThat(accessToken.expiresAt()).isCloseTo(
			now.plusSeconds(ACCESS_EXPIRES_IN), within(1, ChronoUnit.SECONDS));
		assertThat(accessToken.expiresIn()).isEqualTo(ACCESS_EXPIRES_IN);
	}

	@Test
	@DisplayName("Refresh Token을 발급하고 검증에 성공한다.")
	void generateRefreshToken() {
		// given
		UserInfo userInfo = new UserInfo(1L);
		Instant now = Instant.now();

		// when
		TokenInfo refreshToken = jwtTokenProvider.generateRefreshToken(userInfo);

		// then
		assertThat(refreshToken).isNotNull();
		assertThat(jwtTokenProvider.validateRefreshToken(refreshToken.token()))
			.isEqualTo(JwtCode.VALID_TOKEN);
		assertThat(jwtTokenProvider.validateAccessToken(refreshToken.token()))
			.isEqualTo(JwtCode.INVALID_TOKEN);
		assertThat(refreshToken.expiresAt()).isCloseTo(
			now.plusSeconds(REFRESH_EXPIRES_IN), within(1, ChronoUnit.SECONDS));
		assertThat(refreshToken.expiresIn()).isEqualTo(REFRESH_EXPIRES_IN);
	}

	@Test
	@DisplayName("토큰에서 UserInfo를 추출한다.")
	void getAuthentication() {
		// given
		Long userId = 1L;
		UserInfo userInfo = new UserInfo(userId);
		TokenInfo accessToken = jwtTokenProvider.generateAccessToken(userInfo);

		// when
		Authentication authentication = jwtTokenProvider.getAuthentication(accessToken.token());

		// then
		assertThat(authentication).isNotNull();
		assertThat(authentication.getPrincipal()).isInstanceOf(UserInfo.class);

		UserInfo extractedUserInfo = (UserInfo)authentication.getPrincipal();
		assertThat(extractedUserInfo.id()).isEqualTo(userId);
	}

	@Test
	@DisplayName("만료된 토큰을 검증하면 EXPIRED_TOKEN을 반환한다.")
	void validateExpiredToken() {
		// given
		long expiresIn = -1000L;
		JwtTokenProvider shortProvider = new JwtTokenProvider(JWT_SECRET, expiresIn, expiresIn);
		UserInfo userInfo = new UserInfo(1L);
		TokenInfo accessToken = shortProvider.generateAccessToken(userInfo);
		TokenInfo refreshToken = shortProvider.generateRefreshToken(userInfo);

		// when & then
		assertThat(shortProvider.validateAccessToken(accessToken.token()))
			.isEqualTo(JwtCode.EXPIRED_TOKEN);
		assertThat(shortProvider.validateRefreshToken(refreshToken.token()))
			.isEqualTo(JwtCode.EXPIRED_TOKEN);
	}

	@ParameterizedTest
	@DisplayName("형식이 잘못된 토큰을 검증하면 INVALID_TOKEN을 반환한다.")
	@NullAndEmptySource
	@ValueSource(strings = {"header.payload"})
	void validateNullOrEmptyToken(String token) {
		// when & then
		assertThat(jwtTokenProvider.validateAccessToken(token))
			.isEqualTo(JwtCode.INVALID_TOKEN);
	}

	@Test
	@DisplayName("다른 비밀키로 서명된 토큰을 검증하면 INVALID_TOKEN을 반환한다.")
	void validateForgedToken() {
		// given
		String fakeKey = "ThisIsAFakeSecretKeyForJwtWhichIsLongEnough";
		UserInfo userInfo = new UserInfo(1L);
		JwtTokenProvider fakeProvider = new JwtTokenProvider(fakeKey, ACCESS_EXPIRES_IN, REFRESH_EXPIRES_IN);
		TokenInfo accessToken = fakeProvider.generateAccessToken(userInfo);

		// when & then
		assertThat(jwtTokenProvider.validateAccessToken(accessToken.token()))
			.isEqualTo(JwtCode.INVALID_TOKEN);
	}
}
