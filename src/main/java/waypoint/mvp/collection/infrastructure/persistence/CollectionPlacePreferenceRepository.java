package waypoint.mvp.collection.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import waypoint.mvp.collection.domain.CollectionPlacePreference;

public interface CollectionPlacePreferenceRepository
	extends JpaRepository<CollectionPlacePreference, Long> {

	Optional<CollectionPlacePreference> findByPlaceIdAndMemberId(Long collectionPlaceId, Long collectionMemberId);

	List<CollectionPlacePreference> findAllByPlaceIdAndType(Long collectionPlaceId,
		CollectionPlacePreference.Type type);

	List<CollectionPlacePreference> findAllByPlaceIdIn(List<Long> collectionPlaceIds);
}
