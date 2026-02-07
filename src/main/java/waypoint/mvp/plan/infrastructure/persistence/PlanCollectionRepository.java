package waypoint.mvp.plan.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import waypoint.mvp.plan.domain.PlanCollection;

public interface PlanCollectionRepository extends JpaRepository<PlanCollection, Long> {

	boolean existsByPlanIdAndCollectionId(Long planId, Long collectionId);

	@Query("SELECT COUNT(pc) > 0 FROM PlanCollection pc "
		+ "WHERE pc.plan.externalId = :planExternalId AND pc.collection.externalId = :collectionExternalId")
	boolean existsByPlanIdAndCollectionId(@Param("planExternalId") String planExternalId,
		@Param("collectionExternalId") String collectionExternalId);
}
