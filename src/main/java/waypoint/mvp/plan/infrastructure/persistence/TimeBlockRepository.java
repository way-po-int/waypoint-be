package waypoint.mvp.plan.infrastructure.persistence;

import java.time.LocalTime;
import java.util.List;
import java.time.LocalTime;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import waypoint.mvp.plan.domain.TimeBlock;

public interface TimeBlockRepository extends JpaRepository<TimeBlock, Long> {

	@Query("SELECT tb FROM TimeBlock tb "
		+ "WHERE tb.planDay.plan.id = :planId AND tb.planDay.day = :day "
		+ "ORDER BY tb.startTime ASC, tb.endTime ASC, tb.id ASC")
	Slice<TimeBlock> findAllByPlanIdAndDay(@Param("planId") Long planId, @Param("day") int day, Pageable pageable);

	@Query("SELECT tb FROM TimeBlock tb "
		+ "JOIN FETCH tb.planDay JOIN FETCH tb.planDay.plan "
		+ "WHERE tb.externalId = :externalId AND tb.planDay.plan.id = :planId")
	Optional<TimeBlock> findByExternalId(@Param("planId") Long planId, @Param("externalId") String externalId);

	@Query("SELECT tb FROM TimeBlock tb "
		+ "WHERE tb.planDay.id = :planDayId "
		+ "AND ((tb.startTime < :endTime AND tb.endTime > :startTime))")
	List<TimeBlock> findOverlappingTimeBlocks(
		@Param("planDayId") Long planDayId,
		@Param("startTime") LocalTime startTime,
		@Param("endTime") LocalTime endTime);

	@Query("SELECT tb FROM TimeBlock tb "
		+ "WHERE tb.planDay.id = :planDayId "
		+ "AND tb.id != :excludeId "
		+ "AND ((tb.startTime < :endTime AND tb.endTime > :startTime))")
	List<TimeBlock> findOverlappingTimeBlocksExcludingId(
		@Param("planDayId") Long planDayId,
		@Param("startTime") LocalTime startTime,
		@Param("endTime") LocalTime endTime,
		@Param("excludeId") Long excludeId);

	@Query("SELECT t FROM TimeBlock t"
		+ " JOIN FETCH t.planDay"
		+ " WHERE t.planDay.id = :planDayId"
		+ " AND t.startTime <= :startTime ORDER BY t.startTime DESC LIMIT 1")
	TimeBlock findPrevTimeBlock(@Param("planDayId") Long planDayId, @Param("startTime") LocalTime startTime);
}
