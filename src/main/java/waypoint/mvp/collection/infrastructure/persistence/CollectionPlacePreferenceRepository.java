package waypoint.mvp.collection.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import waypoint.mvp.collection.domain.CollectionPlacePreference;

public interface CollectionPlacePreferenceRepository
	extends JpaRepository<CollectionPlacePreference, Long> {

	Optional<CollectionPlacePreference> findByPlaceIdAndMemberId(Long collectionPlaceId, Long collectionMemberId);

	@Query("SELECT p FROM CollectionPlacePreference p JOIN FETCH p.member WHERE p.place.id IN :collectionPlaceIds")
	List<CollectionPlacePreference> findAllByPlaceIdIn(@Param("collectionPlaceIds") List<Long> collectionPlaceIds);

	@Query("select p from CollectionPlacePreference p join fetch p.member where p.place.id = :collectionPlaceId")
	List<CollectionPlacePreference> findAllByPlaceId(@Param("collectionPlaceId") Long collectionPlaceId);

}
