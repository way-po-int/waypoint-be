package waypoint.mvp.auth.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import waypoint.mvp.auth.domain.RefreshToken;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
	Optional<RefreshToken> findByToken(String token);
}
