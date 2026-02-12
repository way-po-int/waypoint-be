package waypoint.mvp.plan.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import waypoint.mvp.plan.domain.PlanDay;

public interface PlanDayRepository extends JpaRepository<PlanDay, Long> {

	Optional<PlanDay> findByPlanIdAndDay(Long planId, int day);

	List<PlanDay> findAllByPlanIdAndDayGreaterThan(Long planId, int day);

	int countByPlanId(Long planId);

	@Query("SELECT pd.day, COUNT(tb) FROM PlanDay pd LEFT JOIN TimeBlock tb ON tb.planDay = pd "
		+ "WHERE pd.plan.id = :planId AND pd.day > :day GROUP BY pd.day ORDER BY pd.day")
	List<Object[]> countTimeBlocksByDayGreaterThan(@Param("planId") Long planId, @Param("day") int day);
}
