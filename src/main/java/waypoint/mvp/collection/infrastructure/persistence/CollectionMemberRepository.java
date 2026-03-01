package waypoint.mvp.collection.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import waypoint.mvp.collection.domain.CollectionMember;

public interface CollectionMemberRepository extends JpaRepository<CollectionMember, Long> {

	@Query("SELECT cm FROM CollectionMember cm WHERE cm.id = :memberId AND cm.collection.id = :collectionId AND cm.deletedAt IS NULL")
	Optional<CollectionMember> findActive(@Param("collectionId") Long collectionId, @Param("memberId") Long memberId);

	@Query("SELECT cm FROM CollectionMember  cm WHERE cm.externalId = :memberExternalId AND cm.collection.id = :collectionId AND cm.deletedAt IS NULL")
	Optional<CollectionMember> findActiveByMemberExternalId(
		@Param("collectionId") Long collectionId,
		@Param("memberExternalId") String memberExternalId
	);

	@Query("SELECT cm FROM CollectionMember cm WHERE cm.collection.id = :collectionId AND cm.user.id = :userId AND cm.deletedAt IS NULL")
	Optional<CollectionMember> findActiveByUserId(@Param("collectionId") Long collectionId,
		@Param("userId") Long userId);

	@Query("SELECT cm FROM CollectionMember cm WHERE cm.collection.id = :collectionId AND cm.user.id = :userId AND cm.deletedAt IS NOT NULL")
	Optional<CollectionMember> findWithdrawnMember(@Param("collectionId") Long collectionId,
		@Param("userId") Long userId);

	@Query("SELECT count(cm) > 0 FROM CollectionMember cm WHERE cm.collection.id = :collectionId AND cm.user.id = :userId AND cm.deletedAt IS NULL")
	boolean existsActive(@Param("collectionId") Long collectionId, @Param("userId") Long userId);

	@Query("SELECT cm FROM CollectionMember cm WHERE cm.collection.id = :collectionId AND cm.deletedAt IS NULL")
	List<CollectionMember> findActiveAll(@Param("collectionId") Long collectionId);

	@Modifying
	@Query("UPDATE CollectionMember cm SET cm.nickname = :nickname, cm.picture = :picture WHERE cm.user.id = :userId AND cm.deletedAt IS NULL")
	void updateProfileByUserId(
		@Param("userId") Long userId,
		@Param("nickname") String nickname,
		@Param("picture") String picture
	);
}
