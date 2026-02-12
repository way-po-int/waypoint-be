package waypoint.mvp.plan.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import waypoint.mvp.plan.domain.TimeBlock;

public interface TimeBlockRepository extends JpaRepository<TimeBlock, Long> {
}
