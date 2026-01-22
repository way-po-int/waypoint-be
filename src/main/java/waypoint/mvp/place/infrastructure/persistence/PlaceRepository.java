package waypoint.mvp.place.infrastructure.persistence;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import waypoint.mvp.place.domain.Place;

public interface PlaceRepository extends JpaRepository<Place, Long> {

	@Query("select p.detail.placeId from Place p where p.detail.placeId in :placeIds")
	Set<String> findExistingPlaceIds(@Param("placeIds") Set<String> placeIds);
}
