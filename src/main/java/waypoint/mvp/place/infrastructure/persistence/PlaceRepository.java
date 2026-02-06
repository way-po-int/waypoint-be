package waypoint.mvp.place.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import waypoint.mvp.place.domain.Place;

public interface PlaceRepository extends JpaRepository<Place, Long> {

	Optional<Place> findByDetailPlaceId(String detailPlaceId);

	Optional<Place> findByExternalId(String externalId);
}
