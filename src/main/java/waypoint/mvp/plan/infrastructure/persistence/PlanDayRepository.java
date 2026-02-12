package waypoint.mvp.plan.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import waypoint.mvp.plan.domain.PlanDay;

public interface PlanDayRepository extends JpaRepository<PlanDay, Long> {

	Optional<PlanDay> findByPlanIdAndDay(Long planId, int day);
}
