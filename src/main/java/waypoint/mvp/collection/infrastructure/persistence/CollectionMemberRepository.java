package waypoint.mvp.collection.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import waypoint.mvp.collection.domain.CollectionMember;

public interface CollectionMemberRepository extends JpaRepository<CollectionMember, Long> {

	@Query("SELECT cm FROM CollectionMember cm WHERE cm.id = :memberId AND cm.collection.id = :collectionId AND cm.deletedAt IS NULL")
	Optional<CollectionMember> findActive(@Param("memberId") Long memberId, @Param("collectionId") Long collectionId);

	@Query("SELECT cm FROM CollectionMember cm WHERE cm.collection.id = :collectionId AND cm.user.id = :userId AND cm.deletedAt IS NULL")
	Optional<CollectionMember> findActiveByUserId(@Param("collectionId") Long collectionId,
		@Param("userId") Long userId);

	@Query("SELECT cm FROM CollectionMember cm WHERE cm.collection.id = :collectionId AND cm.user.id = :userId AND cm.deletedAt IS NOT NULL")
	Optional<CollectionMember> findWithdrawnMember(@Param("collectionId") Long collectionId,
		@Param("userId") Long userId);

	@Query("SELECT count(cm) > 0 FROM CollectionMember cm WHERE cm.collection.id = :collectionId AND cm.user.id = :userId AND cm.deletedAt IS NULL")
	boolean existsActive(@Param("collectionId") Long collectionId, @Param("userId") Long userId);

}
