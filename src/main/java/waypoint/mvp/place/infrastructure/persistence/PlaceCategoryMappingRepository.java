package waypoint.mvp.place.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import waypoint.mvp.place.domain.PlaceCategoryMapping;

public interface PlaceCategoryMappingRepository extends JpaRepository<PlaceCategoryMapping, String> {
}
