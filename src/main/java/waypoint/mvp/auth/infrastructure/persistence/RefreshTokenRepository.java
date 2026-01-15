package waypoint.mvp.auth.infrastructure.persistence;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import waypoint.mvp.auth.domain.RefreshToken;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {
	Optional<RefreshToken> findByToken(String token);

	@Modifying
	@Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
	long deleteExpiredTokens(@Param("now") Instant now);
}
