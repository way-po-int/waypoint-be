package waypoint.mvp.place.infrastructure.persistence;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;

import waypoint.mvp.place.domain.PlaceSearchStatus;
import waypoint.mvp.place.domain.SocialMediaPlace;

public interface SocialMediaPlaceRepository extends JpaRepository<SocialMediaPlace, Long> {
	boolean existsBySocialMediaIdAndStatusIn(Long socialMediaId, Collection<PlaceSearchStatus> statuses);
}
