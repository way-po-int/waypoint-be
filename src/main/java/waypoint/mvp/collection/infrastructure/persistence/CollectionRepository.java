package waypoint.mvp.collection.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import waypoint.mvp.collection.domain.Collection;

public interface CollectionRepository extends JpaRepository<Collection, Long> {
}
