package waypoint.mvp.collection.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import waypoint.mvp.collection.domain.CollectionPlace;

public interface CollectionPlaceRepository extends JpaRepository<CollectionPlace, Long> {
	@Query("SELECT cp FROM CollectionPlace cp LEFT JOIN FETCH cp.place LEFT JOIN FETCH cp.socialMedia WHERE cp.id = :id AND cp.collection.id = :collectionId")
	Optional<CollectionPlace> findByIdAndCollectionId(@Param("id") Long id, @Param("collectionId") Long collectionId);

	boolean existsByCollectionIdAndPlaceId(Long collectionId, Long placeId);

	@Query(value = "select cp from CollectionPlace cp join fetch cp.place where cp.collection.id = :collectionId",
		countQuery = "select count(cp) from CollectionPlace cp where cp.collection.id = :collectionId")
	Page<CollectionPlace> findAllByCollectionId(@Param("collectionId") Long collectionId, Pageable pageable);

	@Query(value = "select cp from CollectionPlace cp join fetch cp.place where cp.collection.id = :collectionId and cp.addedBy.id <> :myMemberId",
		countQuery = "select count(cp) from CollectionPlace cp where cp.collection.id = :collectionId and cp.addedBy.id <> :myMemberId")
	Page<CollectionPlace> findAllByCollectionIdAndAddedByIdNot(@Param("collectionId") Long collectionId,
		@Param("myMemberId") Long myMemberId, Pageable pageable);
}
