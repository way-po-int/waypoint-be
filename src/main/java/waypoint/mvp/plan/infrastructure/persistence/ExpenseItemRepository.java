package waypoint.mvp.plan.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import waypoint.mvp.plan.domain.Expense;
import waypoint.mvp.plan.domain.ExpenseItem;

public interface ExpenseItemRepository extends JpaRepository<ExpenseItem, Long> {

	List<ExpenseItem> findAllByExpenseId(Long expenseId);

	void deleteAllInBatchByExpense(Expense expense);

	@Query("""
		SELECT COALESCE(SUM(ei.cost), 0)
		FROM ExpenseItem ei
		JOIN ei.expense e
		LEFT JOIN e.block b
		WHERE e.budget.id = :budgetId
		AND (
		    e.type = 'ADDITIONAL'
		    OR (e.type = 'BLOCK' AND b.selected = true)
		)
		""")
	long calculateFixedCost(@Param("budgetId") Long budgetId);

	@Query("""
		SELECT COALESCE(SUM(tb_max.max_cost), 0)
		FROM (
		    SELECT MAX(b_sum.block_total) AS max_cost
		    FROM (
		        SELECT b.timeBlock.id AS tb_id, SUM(ei.cost) AS block_total
		        FROM ExpenseItem ei
		        JOIN ei.expense e JOIN e.block b
		        WHERE e.budget.id = :budgetId
		        AND e.type = 'BLOCK'
		        AND NOT EXISTS (
		            SELECT 1 FROM Block bs
		            WHERE bs.timeBlock = b.timeBlock AND bs.selected = true
		        )
		        GROUP BY b.timeBlock.id, b.id
		    ) b_sum
		    GROUP BY b_sum.tb_id
		) tb_max
		""")
	long calculateUnfixedCost(@Param("budgetId") Long budgetId);
}
