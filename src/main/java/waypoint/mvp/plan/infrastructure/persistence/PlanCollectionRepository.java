package waypoint.mvp.plan.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

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

	@Query("SELECT pc FROM PlanCollection pc JOIN pc.collection c "
		+ "WHERE pc.plan.externalId = :planExternalId AND c.externalId = :collectionExternalId")
	Optional<PlanCollection> findByPlanIdAndCollectionId(
		@Param("planExternalId") String planExternalId,
		@Param("collectionExternalId") String collectionExternalId
	);

	@Query("SELECT pc FROM PlanCollection pc JOIN FETCH pc.collection JOIN FETCH pc.member WHERE pc.plan.externalId = :planExternalId")
	List<PlanCollection> findAllByPlanId(@Param("planExternalId") String planExternalId);

	@Query("SELECT pc.collection.id FROM PlanCollection pc WHERE pc.plan.id = :planId")
	List<Long> findCollectionIdsByPlanId(@Param("planId") Long planId);

	@Query("SELECT pc FROM PlanCollection pc "
		+ "JOIN FETCH pc.plan p "
		+ "LEFT JOIN FETCH pc.collection c "
		+ "WHERE p.externalId IN :planExternalIds")
	List<PlanCollection> findAllByPlanExternalIdIn(@Param("planExternalIds") List<String> planExternalIds);

	@Query("SELECT DISTINCT c.externalId FROM PlanCollection pc "
		+ "JOIN pc.collection c "
		+ "WHERE pc.plan.externalId = :planExternalId "
		+ "AND c.externalId IN :collectionExternalIds")
	List<String> findExistingCollectionExternalIds(
		@Param("planExternalId") String planExternalId,
		@Param("collectionExternalIds") List<String> collectionExternalIds
	);
}
