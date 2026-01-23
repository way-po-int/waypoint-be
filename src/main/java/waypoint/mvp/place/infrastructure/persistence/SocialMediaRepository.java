package waypoint.mvp.place.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import waypoint.mvp.place.domain.SocialMedia;

public interface SocialMediaRepository extends JpaRepository<SocialMedia, Long> {
	Optional<SocialMedia> findByUrl(String url);
}
