package waypoint.mvp.collection.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import waypoint.mvp.collection.domain.CollectionPlaceDraft;

public interface CollectionPlaceDraftRepository extends JpaRepository<CollectionPlaceDraft, Long> {

	Optional<CollectionPlaceDraft> findByMemberId(Long memberId);
}
