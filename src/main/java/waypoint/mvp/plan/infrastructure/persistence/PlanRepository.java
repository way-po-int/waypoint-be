package waypoint.mvp.plan.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import waypoint.mvp.plan.domain.Plan;

public interface PlanRepository extends JpaRepository<Plan, Long> {

}
