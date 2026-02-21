package waypoint.mvp.plan.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import waypoint.mvp.plan.domain.ExpenseItem;

public interface ExpenseItemRepository extends JpaRepository<ExpenseItem, Long> {
}
