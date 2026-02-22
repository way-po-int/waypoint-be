package waypoint.mvp.plan.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import waypoint.mvp.plan.domain.Budget;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

	Optional<Budget> findByPlanId(Long planId);

	Optional<Budget> findByPlanExternalId(String planExternalId);
}
