package waypoint.mvp.auth.infrastructure.persistence;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityManager;
import waypoint.mvp.auth.domain.RefreshToken;
import waypoint.mvp.global.annotation.RepositoryTest;

@RepositoryTest
class RefreshTokenRepositoryTest {

	@Autowired
	private RefreshTokenRepository refreshTokenRepository;

	@Autowired
	private EntityManager em;

	@Test
	@DisplayName("만료 시간이 지난 토큰은 삭제되어야 한다.")
	void deleteExpiredTokens() {
		// given
		RefreshToken expiredToken = RefreshToken.create(
			1L,
			"expired_token",
			Instant.now().minusSeconds(3600)
		);
		RefreshToken validToken = RefreshToken.create(
			2L,
			"valid_token",
			Instant.now().plusSeconds(3600)
		);
		refreshTokenRepository.saveAll(List.of(expiredToken, validToken));
		em.flush();
		em.clear();

		// when
		long deletedCount = refreshTokenRepository.deleteExpiredTokens(Instant.now());

		// then
		assertThat(deletedCount).isEqualTo(1);
		assertThat(refreshTokenRepository.findById(expiredToken.getId())).isEmpty();
		assertThat(refreshTokenRepository.findById(validToken.getId())).isPresent();
	}
}
