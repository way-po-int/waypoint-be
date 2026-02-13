package waypoint.mvp.collection.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import waypoint.mvp.collection.domain.CollectionPlace;

public interface CollectionPlaceRepository extends JpaRepository<CollectionPlace, Long> {
	@Query("SELECT cp FROM CollectionPlace cp LEFT JOIN FETCH cp.place LEFT JOIN FETCH cp.socialMedia WHERE cp.externalId = :externalId AND cp.collection.id = :collectionId")
	Optional<CollectionPlace> findByExternalIdAndCollectionId(
		@Param("externalId") String externalId,
		@Param("collectionId") Long collectionId
	);

	@Query("SELECT cp FROM CollectionPlace cp LEFT JOIN FETCH cp.place LEFT JOIN FETCH cp.socialMedia WHERE cp.externalId = :externalId")
	Optional<CollectionPlace> findByExternalId(@Param("externalId") String externalId);

	@Query("SELECT cp FROM CollectionPlace cp LEFT JOIN FETCH cp.place LEFT JOIN FETCH cp.socialMedia WHERE cp.id = :collectionPlaceId")
	Optional<CollectionPlace> findByIdWithFetch(@Param("collectionPlaceId") Long collectionPlaceId);

	boolean existsByCollectionIdAndPlaceId(Long collectionId, Long placeId);

	@Query("select distinct cp from CollectionPlace cp join fetch cp.place where cp.collection.id = :collectionId")
	Slice<CollectionPlace> findAllByCollectionId(@Param("collectionId") Long collectionId, Pageable pageable);

	@Query("select distinct cp from CollectionPlace cp join fetch cp.place where cp.collection.id = :collectionId and cp.addedBy.externalId = :addedByMemberExternalId")
	Slice<CollectionPlace> findAllByCollectionIdAndAddedByExternalId(@Param("collectionId") Long collectionId,
		@Param("addedByMemberExternalId") String addedByMemberExternalId, Pageable pageable);
}
