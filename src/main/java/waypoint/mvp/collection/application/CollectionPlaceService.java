package waypoint.mvp.collection.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.UserInfo;
import waypoint.mvp.collection.application.dto.request.CollectionPlaceFromUrlRequest;
import waypoint.mvp.collection.application.dto.response.ExtractionJobResponse;
import waypoint.mvp.collection.domain.CollectionMember;
import waypoint.mvp.collection.domain.PlaceExtractionJob;
import waypoint.mvp.collection.error.CollectionError;
import waypoint.mvp.collection.infrastructure.persistence.CollectionMemberRepository;
import waypoint.mvp.collection.infrastructure.persistence.CollectionPlaceExtractionJobRepository;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.place.application.ExtractionJobService;
import waypoint.mvp.place.application.dto.ExtractionJobInfo;
import waypoint.mvp.place.domain.SocialMedia;
import waypoint.mvp.place.error.SocialMediaError;
import waypoint.mvp.place.infrastructure.persistence.SocialMediaRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CollectionPlaceService {

	private final ExtractionJobService extractionJobService;
	private final CollectionMemberRepository collectionMemberRepository;
	private final CollectionPlaceExtractionJobRepository jobRepository;
	private final SocialMediaRepository socialMediaRepository;

	@Transactional
	public ExtractionJobResponse addPlacesFromUrl(Long collectionId, CollectionPlaceFromUrlRequest request,
		UserInfo userInfo) {

		CollectionMember collectionMember = collectionMemberRepository
			.findByCollectionIdAndUserId(collectionId, userInfo.id())
			.orElseThrow(() -> new BusinessException(CollectionError.FORBIDDEN_NOT_MEMBER));
		Long memberId = collectionMember.getId();

		ExtractionJobInfo jobInfo = extractionJobService.addJob(request.url());

		if (jobRepository.existsByMemberIdAndSocialMediaId(memberId, jobInfo.id())) {
			throw new BusinessException(SocialMediaError.DUPLICATE_EXTRACTION_JOB);
		}

		PlaceExtractionJob job = createExtractionJob(collectionMember, jobInfo.id());
		return new ExtractionJobResponse(
			job.getId(),
			jobInfo.status()
		);
	}

	private PlaceExtractionJob createExtractionJob(CollectionMember member, Long socialMediaId) {
		SocialMedia media = socialMediaRepository.getReferenceById(socialMediaId);
		PlaceExtractionJob job = PlaceExtractionJob.create(member, media);
		return jobRepository.save(job);
	}
}
