package waypoint.mvp.place.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import waypoint.mvp.place.domain.PlaceCategory;

public interface PlaceCategoryRepository extends JpaRepository<PlaceCategory, Long> {
}
