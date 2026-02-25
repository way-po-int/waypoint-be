package waypoint.mvp.plan.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import waypoint.mvp.plan.domain.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

	@Query("""
		SELECT e FROM Expense e
		LEFT JOIN FETCH e.block b
		LEFT JOIN fetch b.place p
		WHERE e.budget.id = :budgetId AND e.externalId = :externalId
		""")
	Optional<Expense> findByBudgetIdAndExternalId(
		@Param("budgetId") Long budgetId,
		@Param("externalId") String externalId
	);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		SELECT e FROM Expense e
		LEFT JOIN FETCH e.block b
		LEFT JOIN fetch b.place p
		LEFT JOIN fetch b.timeBlock t
		LEFT JOIN fetch t.planDay pd
		WHERE e.budget.id = :budgetId AND e.externalId = :externalId
		""")
	Optional<Expense> findByExternalIdWithLock(
		@Param("budgetId") Long budgetId,
		@Param("externalId") String externalId
	);

	@Query("""
		SELECT MIN(e.rank) FROM Expense e
		WHERE (
		    (:timeBlockId IS NULL AND e.planDay.id = :planDayId AND e.timeBlock IS NULL)
		    OR (:timeBlockId IS NOT NULL AND e.timeBlock.id = :timeBlockId)
		)
		AND e.rank > :prevRank
		""")
	Long findNextRank(
		@Param("timeBlockId") Long timeBlockId,
		@Param("planDayId") Long planDayId,
		@Param("prevRank") Long prevRank
	);

	@Query("""
		SELECT COALESCE(MAX(e.rank), 0) FROM Expense e
		WHERE (
		    (:timeBlockId IS NULL AND e.planDay.id = :planDayId AND e.timeBlock IS NULL)
		    OR (:timeBlockId IS NOT NULL AND e.timeBlock.id = :timeBlockId)
		)
		""")
	Long findLastRank(@Param("timeBlockId") Long timeBlockId, @Param("planDayId") Long planDayId);

	@Query("""
		SELECT e FROM Expense e
		WHERE (
		    (:timeBlockId IS NULL AND e.planDay.id = :planDayId AND e.timeBlock IS NULL)
		    OR (:timeBlockId IS NOT NULL AND e.timeBlock.id = :timeBlockId)
		)
		ORDER BY e.rank ASC
		""")
	List<Expense> findByTimeBlockId(@Param("timeBlockId") Long timeBlockId, @Param("planDayId") Long planDayId);

	@Query("""
		SELECT e FROM Expense e
		LEFT JOIN FETCH e.timeBlock t
		LEFT JOIN FETCH e.block b
		LEFT JOIN FETCH b.timeBlock bt
		LEFT JOIN fetch b.place p
		WHERE (
		    (e.type = 'ADDITIONAL' AND e.planDay.id = :planDayId)
		    OR (e.type = 'BLOCK' AND bt.planDay.id = :planDayId)
		)
		AND e.budget.id = :budgetId
		ORDER BY COALESCE(t.startTime, bt.startTime) ASC NULLS FIRST, e.rank ASC
		""")
	List<Expense> findAllByBudgetIdAndPlanDayId(@Param("budgetId") Long budgetId, @Param("planDayId") Long planDayId);
}
