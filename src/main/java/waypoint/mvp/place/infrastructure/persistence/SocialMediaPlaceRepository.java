package waypoint.mvp.place.infrastructure.persistence;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import waypoint.mvp.place.domain.Place;
import waypoint.mvp.place.domain.PlaceSearchStatus;
import waypoint.mvp.place.domain.SocialMediaPlace;

public interface SocialMediaPlaceRepository extends JpaRepository<SocialMediaPlace, Long> {
	boolean existsBySocialMediaIdAndStatusIn(Long socialMediaId, Collection<PlaceSearchStatus> statuses);

	@Query("SELECT smp FROM SocialMediaPlace smp JOIN FETCH smp.place p WHERE smp.socialMedia.id = :socialMediaId")
	List<SocialMediaPlace> findAllBySocialMediaId(Long socialMediaId);

	@Query("SELECT p FROM SocialMediaPlace smp JOIN smp.place p"
		+ " WHERE smp.socialMedia.id = :socialMediaId AND p.externalId IN :placeIds"
		+ " AND NOT EXISTS (SELECT 1 FROM CollectionPlace cp WHERE cp.place = p AND cp.collection.id = :collectionId)")
	List<Place> findPlacesNotAddedToCollection(
		@Param("socialMediaId") Long socialMediaId,
		@Param("placeIds") List<String> placeIds,
		@Param("collectionId") Long collectionId
	);
}
