package waypoint.mvp.plan.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import waypoint.mvp.plan.domain.TimeBlock;

public interface TimeBlockRepository extends JpaRepository<TimeBlock, Long> {

	@Query("SELECT tb FROM TimeBlock tb WHERE tb.planDay.plan.id = :planId AND tb.planDay.day = :day")
	Slice<TimeBlock> findAllByPlanIdAndDay(@Param("planId") Long planId, @Param("day") int day, Pageable pageable);

	@Query("SELECT tb FROM TimeBlock tb "
		+ "JOIN FETCH tb.planDay "
		+ "WHERE tb.externalId = :externalId AND tb.planDay.plan.id = :planId")
	Optional<TimeBlock> findByExternalId(@Param("planId") Long planId, @Param("externalId") String externalId);
}
