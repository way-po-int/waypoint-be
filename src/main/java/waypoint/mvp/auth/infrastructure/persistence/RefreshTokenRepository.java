package waypoint.mvp.auth.infrastructure.persistence;

import org.springframework.data.repository.CrudRepository;

import waypoint.mvp.auth.domain.RefreshToken;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}
