package waypoint.mvp.plan.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import waypoint.mvp.plan.domain.Budget;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

	Optional<Budget> findByPlanId(Long planId);

	@Query("SELECT b FROM  Budget b JOIN FETCH b.plan p WHERE p.externalId = :planExternalId")
	Optional<Budget> findByPlanExternalId(@Param("planExternalId") String planExternalId);
}
