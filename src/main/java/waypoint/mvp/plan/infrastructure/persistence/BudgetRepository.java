package waypoint.mvp.plan.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import waypoint.mvp.plan.domain.Budget;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
}
