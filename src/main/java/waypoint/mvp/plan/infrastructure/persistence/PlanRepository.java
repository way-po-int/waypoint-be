package waypoint.mvp.plan.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import waypoint.mvp.plan.domain.Plan;

public interface PlanRepository extends JpaRepository<Plan, Long> {

	@Query("SELECT p FROM Plan p WHERE p.deletedAt IS NULL AND p.id IN " +
		"(SELECT pm.plan.id FROM PlanMember pm WHERE pm.user.id = :userId)")
	Slice<Plan> findAllActiveByUserId(@Param("userId") Long userId, Pageable pageable);

	@Query("SELECT p FROM Plan p WHERE p.id = :planId AND p.deletedAt IS NULL")
	Optional<Plan> findActive(@Param("planId") Long planId);
}
