package waypoint.mvp.plan.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import waypoint.mvp.plan.domain.PlanCollection;

public interface PlanCollectionRepository extends JpaRepository<PlanCollection, Long> {
}
