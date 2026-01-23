package waypoint.mvp.collection.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import waypoint.mvp.collection.domain.PlaceExtractionJob;

public interface CollectionPlaceExtractionJobRepository extends JpaRepository<PlaceExtractionJob, Long> {

	boolean existsByMemberIdAndSocialMediaId(Long memberId, Long socialMediaId);
}
