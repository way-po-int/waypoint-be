package waypoint.mvp.collection.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import waypoint.mvp.collection.domain.CollectionPlaceDraft;

public interface CollectionPlaceDraftRepository extends JpaRepository<CollectionPlaceDraft, Long> {

	Optional<CollectionPlaceDraft> findByMemberId(Long memberId);

	@Query("SELECT d FROM CollectionPlaceDraft d JOIN FETCH d.member m"
		+ " JOIN FETCH m.collection c JOIN FETCH d.socialMedia s"
		+ " WHERE d.externalId = :externalId AND c.id = :collectionId AND m.user.id = :userId")
	Optional<CollectionPlaceDraft> findDraft(
		@Param("externalId") String externalId,
		@Param("collectionId") Long collectionId,
		@Param("userId") Long userId
	);

	@Query("SELECT d FROM CollectionPlaceDraft d JOIN FETCH d.member m"
		+ " JOIN FETCH m.collection c JOIN FETCH d.socialMedia s"
		+ " WHERE m.collection.id = :collectionId AND m.user.id = :userId"
		+ " ORDER BY d.id DESC")
	List<CollectionPlaceDraft> findLatestDraft(
		@Param("collectionId") Long collectionId,
		@Param("userId") Long userId,
		Pageable pageable
	);
}
