package waypoint.mvp.plan.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import waypoint.mvp.plan.domain.PlanMember;

public interface PlanMemberRepository extends JpaRepository<PlanMember, Long> {

	@Query("SELECT pm FROM PlanMember pm WHERE pm.plan.id = :planId AND pm.user.id = :userId AND pm.deletedAt IS NULL")
	Optional<PlanMember> findActiveByUserId(@Param("planId") Long planId, @Param("userId") Long userId);

	@Query("SELECT pm FROM PlanMember pm WHERE pm.plan.id = :planId AND pm.user.id = :userId AND pm.deletedAt IS NOT NULL")
	Optional<PlanMember> findWithdrawnMember(@Param("planId") Long planId, @Param("userId") Long userId);

	@Query("SELECT count(pm) > 0 FROM PlanMember pm WHERE pm.plan.id = :planId AND pm.user.id = :userId AND pm.deletedAt IS NULL")
	boolean existsActive(@Param("planId") Long planId, @Param("userId") Long userId);
}
