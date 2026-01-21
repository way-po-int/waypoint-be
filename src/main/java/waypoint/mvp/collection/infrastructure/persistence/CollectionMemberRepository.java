package waypoint.mvp.collection.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import waypoint.mvp.collection.domain.Collection;
import waypoint.mvp.collection.domain.CollectionMember;
import waypoint.mvp.user.domain.User;

public interface CollectionMemberRepository extends JpaRepository<CollectionMember, Long> {

	Optional<CollectionMember> findByCollectionAndUser(Collection collection, User user);

	Optional<CollectionMember> findByCollectionIdAndUserId(Long collectionId, Long userId);

	boolean existsByCollectionIdAndUserId(Long collectionId, Long userId);

	boolean existsByCollectionAndUser(Collection collection, User user);
}
