package waypoint.mvp.plan.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import waypoint.mvp.plan.application.dto.response.PlanUpdateResponse;
import waypoint.mvp.plan.domain.PlanDay;

public interface PlanDayRepository extends JpaRepository<PlanDay, Long> {

	Optional<PlanDay> findByPlanIdAndDay(Long planId, int day);

	List<PlanDay> findAllByPlanIdAndDayGreaterThan(Long planId, int day);

	int countByPlanId(Long planId);

	@Query("SELECT new waypoint.mvp.plan.application.dto.response.PlanUpdateResponse$AffectedDay(pd.day, COUNT(tb)) "
		+ "FROM PlanDay pd LEFT JOIN TimeBlock tb ON tb.planDay = pd "
		+ "WHERE pd.plan.id = :planId AND pd.day > :day GROUP BY pd.day ORDER BY pd.day")
	List<PlanUpdateResponse.AffectedDay> countTimeBlocksByDayGreaterThan(@Param("planId") Long planId,
		@Param("day") int day);

	@Modifying
	@Query("DELETE FROM PlanDay pd WHERE pd.plan.id = :planId AND pd.day > :targetDays")
	void deleteAllForExcessDays(@Param("planId") Long planId, @Param("targetDays") int targetDays);
}
