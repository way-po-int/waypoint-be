package waypoint.mvp.collection.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import waypoint.mvp.collection.domain.PlaceExtractionJob;

public interface PlaceExtractionJobRepository extends JpaRepository<PlaceExtractionJob, Long> {

	Optional<PlaceExtractionJob> findByMemberIdAndDecidedAtIsNull(Long memberId);

	@Query("SELECT j FROM PlaceExtractionJob j JOIN FETCH j.member m"
		+ " JOIN FETCH m.collection c JOIN FETCH j.socialMedia s"
		+ " WHERE j.jobId = :jobId AND c.id = :collectionId AND m.user.id = :userId")
	Optional<PlaceExtractionJob> findExtractionJob(
		@Param("jobId") String jobId,
		@Param("collectionId") Long collectionId,
		@Param("userId") Long userId
	);

	@Query("SELECT j FROM PlaceExtractionJob j JOIN FETCH j.member m"
		+ " JOIN FETCH m.collection c JOIN FETCH j.socialMedia s"
		+ " WHERE m.collection.id = :collectionId AND m.user.id = :userId AND j.decidedAt IS NULL"
		+ " ORDER BY j.id DESC")
	List<PlaceExtractionJob> findLatestExtractionJob(
		@Param("collectionId") Long collectionId,
		@Param("userId") Long userId,
		Pageable pageable
	);

	@Query("SELECT j FROM PlaceExtractionJob j JOIN j.member m"
		+ " WHERE j.jobId = :jobId AND m.collection.id = :collectionId AND m.user.id = :userId")
	Optional<PlaceExtractionJob> findByJobIdAndCollectionIdAndUserId(
		@Param("jobId") String jobId,
		@Param("collectionId") Long collectionId,
		@Param("userId") Long userId
	);
}
