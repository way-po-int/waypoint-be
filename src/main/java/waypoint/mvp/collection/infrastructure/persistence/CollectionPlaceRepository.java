package waypoint.mvp.collection.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import waypoint.mvp.collection.domain.CollectionPlace;

public interface CollectionPlaceRepository extends JpaRepository<CollectionPlace, Long> {

	Optional<CollectionPlace> findByIdAndCollectionId(Long id, Long collectionId);

	boolean existsByCollectionIdAndPlaceId(Long collectionId, Long placeId);

	Page<CollectionPlace> findAllByCollectionId(Long collectionId, Pageable pageable);

	Page<CollectionPlace> findAllByCollectionIdAndAddedByIdNot(Long collectionId, Long myMemberId, Pageable pageable);
}
