package waypoint.mvp.collection.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import waypoint.mvp.collection.domain.CollectionMember;

public interface CollectionMemberRepository extends JpaRepository<CollectionMember, Long> {

	Optional<CollectionMember> findByCollectionIdAndUserId(Long collectionId, Long userId);
}
