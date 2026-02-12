package waypoint.mvp.place.infrastructure.persistence;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import waypoint.mvp.place.domain.PlaceSearchStatus;
import waypoint.mvp.place.domain.SocialMediaPlace;

public interface SocialMediaPlaceRepository extends JpaRepository<SocialMediaPlace, Long> {
	boolean existsBySocialMediaIdAndStatusIn(Long socialMediaId, Collection<PlaceSearchStatus> statuses);

	@Query("SELECT smp FROM SocialMediaPlace smp JOIN FETCH smp.place p WHERE smp.socialMedia.id = :socialMediaId")
	List<SocialMediaPlace> findAllBySocialMediaId(Long socialMediaId);
}
