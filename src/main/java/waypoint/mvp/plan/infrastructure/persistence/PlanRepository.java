package waypoint.mvp.plan.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import waypoint.mvp.plan.domain.Plan;

public interface PlanRepository extends JpaRepository<Plan, Long> {

	@Query("SELECT p FROM PlanMember pm JOIN pm.plan p "
		+ "WHERE pm.user.id = :userId AND pm.deletedAt IS NULL "
		+ "ORDER BY pm.createdAt DESC")
	Slice<Plan> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

	Optional<Plan> findByExternalId(String externalId);
}
