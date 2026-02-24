package waypoint.mvp.place.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import waypoint.mvp.place.domain.ManualPlace;

public interface ManualPlaceRepository extends JpaRepository<ManualPlace, Long> {
}
