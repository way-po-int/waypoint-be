package waypoint.mvp.plan.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import waypoint.mvp.plan.domain.PlanDay;
import waypoint.mvp.plan.domain.TimeBlock;

public interface TimeBlockRepository extends JpaRepository<TimeBlock, Long> {

	List<TimeBlock> findAllByPlanDayIn(List<PlanDay> planDays);

	void deleteAllByPlanDayIn(List<PlanDay> planDays);
}
