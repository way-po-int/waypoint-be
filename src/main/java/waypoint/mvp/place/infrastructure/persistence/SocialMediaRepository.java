package waypoint.mvp.place.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;
import waypoint.mvp.place.domain.SocialMedia;

public interface SocialMediaRepository extends JpaRepository<SocialMedia, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT s FROM SocialMedia s WHERE s.id = :socialMediaId")
	Optional<SocialMedia> lockById(Long socialMediaId);

	Optional<SocialMedia> findByUrl(String url);
}
