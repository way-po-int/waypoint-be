package waypoint.mvp.plan.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import waypoint.mvp.plan.domain.Plan;

public interface PlanRepository extends JpaRepository<Plan, Long> {

	@Query("SELECT pm.plan FROM PlanMember  pm WHERE pm.user.id = :userId")
	Slice<Plan> findAllByUserId(@Param("userId") Long userId, Pageable pageable);
}
