package waypoint.mvp.plan.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import waypoint.mvp.plan.domain.PlanDay;
import waypoint.mvp.plan.domain.TimeBlock;

public interface TimeBlockRepository extends JpaRepository<TimeBlock, Long> {

	List<TimeBlock> findAllByPlanDayIn(List<PlanDay> planDays);

	void deleteAllByPlanDayIn(List<PlanDay> planDays);

	@Modifying
	@Query("DELETE FROM TimeBlock tb WHERE tb.planDay IN "
		+ "(SELECT pd FROM PlanDay pd WHERE pd.plan.id = :planId AND pd.day > :targetDays)")
	void deleteAllForExcessDays(@Param("planId") Long planId, @Param("targetDays") int targetDays);
}
