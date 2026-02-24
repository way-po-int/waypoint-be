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

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		SELECT e FROM Expense e
		LEFT JOIN FETCH e.block b
		LEFT JOIN fetch b.place p
		LEFT JOIN fetch b.timeBlock t
		WHERE e.externalId = :externalId
		""")
	Optional<Expense> findByExternalIdWithLock(@Param("externalId") String externalId);

	@Query("SELECT MIN(e.rank) FROM Expense e WHERE e.timeBlock.id = :timeBlockId AND e.rank > :prevRank")
	Long findNextRank(@Param("timeBlockId") Long timeBlockId, @Param("prevRank") Long prevRank);

	@Query("""
		SELECT COALESCE(MAX(e.rank), 0) FROM Expense e
		WHERE (:timeBlockId IS NULL AND e.timeBlock IS NULL)
		OR (:timeBlockId IS NOT NULL AND e.timeBlock.id = :timeBlockId)
		""")
	Long findLastRank(@Param("timeBlockId") Long timeBlockId);

	@Query("SELECT e FROM Expense e WHERE e.timeBlock.id = :timeBlockId ORDER BY e.rank ASC")
	List<Expense> findByTimeBlockId(Long timeBlockId);
}
