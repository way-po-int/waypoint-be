package waypoint.mvp.collection.application;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.AuthPrincipal;
import waypoint.mvp.collection.application.dto.request.AddExtractedPlacesRequest;
import waypoint.mvp.collection.application.dto.request.PlaceExtractionJobCreateRequest;
import waypoint.mvp.collection.application.dto.response.AddExtractedPlacesResponse;
import waypoint.mvp.collection.application.dto.response.CollectionPlaceResponse;
import waypoint.mvp.collection.application.dto.response.ExtractionJobDetailResponse;
import waypoint.mvp.collection.application.dto.response.ExtractionJobResponse;
import waypoint.mvp.collection.application.dto.response.PickPassResponse;
import waypoint.mvp.collection.domain.Collection;
import waypoint.mvp.collection.domain.CollectionMember;
import waypoint.mvp.collection.domain.CollectionPlace;
import waypoint.mvp.collection.domain.PlaceExtractionJob;
import waypoint.mvp.collection.error.PlaceExtractionJobError;
import waypoint.mvp.collection.infrastructure.persistence.CollectionPlaceRepository;
import waypoint.mvp.collection.infrastructure.persistence.PlaceExtractionJobRepository;
import waypoint.mvp.global.auth.ResourceAuthorizer;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.place.application.SocialMediaService;
import waypoint.mvp.place.domain.SocialMedia;
import waypoint.mvp.place.domain.SocialMediaPlace;
import waypoint.mvp.place.infrastructure.persistence.SocialMediaPlaceRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlaceExtractionJobService {

	private final CollectionService collectionService;
	private final CollectionMemberService collectionMemberService;
	private final SocialMediaService socialMediaService;
	private final CollectionPlaceQueryService collectionPlaceQueryService;

	private final CollectionPlaceRepository collectionPlaceRepository;
	private final PlaceExtractionJobRepository extractionJobRepository;
	private final SocialMediaPlaceRepository socialMediaPlaceRepository;
	private final ResourceAuthorizer collectionAuthorizer;

	@Transactional
	public ExtractionJobResponse createExtractionJob(
		String collectionId,
		PlaceExtractionJobCreateRequest request,
		AuthPrincipal user
	) {
		Collection collection = collectionService.getCollection(collectionId);
		collectionAuthorizer.verifyMember(user, collection.getId());

		CollectionMember member = collectionMemberService.findMemberByUserId(collection.getId(), user.getId());

		// 이미 진행 중인 작업이 있는지 확인
		extractionJobRepository.findByMemberIdAndDecidedAtIsNull(member.getId())
			.ifPresent(job -> {
				throw new BusinessException(PlaceExtractionJobError.JOB_IN_PROGRESS)
					.addProperty("job_id", job.getJobId());
			});

		// 장소 추출 이벤트 요청
		SocialMedia socialMedia = socialMediaService.getOrCreateSocialMedia(request.url());

		// 어떤 멤버가 어떤 URL을 요청했는지 구분하기 위한 중간 테이블
		PlaceExtractionJob job = PlaceExtractionJob.create(member, socialMedia);
		extractionJobRepository.save(job);

		return new ExtractionJobResponse(
			job.getJobId(),
			socialMedia.getStatus()
		);
	}

	public ExtractionJobDetailResponse getExtractionJob(String collectionId, String jobId, AuthPrincipal user) {
		Collection collection = collectionService.getCollection(collectionId);
		collectionAuthorizer.verifyMember(user, collection.getId());

		PlaceExtractionJob job = extractionJobRepository.findExtractionJob(jobId, collection.getId(), user.getId())
			.orElseThrow(() -> new BusinessException(PlaceExtractionJobError.JOB_NOT_FOUND));

		return toExtractionJobDetailResponse(job);
	}

	public ExtractionJobDetailResponse getLatestExtractionJob(String collectionId, AuthPrincipal user) {
		Collection collection = collectionService.getCollection(collectionId);
		collectionAuthorizer.verifyMember(user, collection.getId());

		PageRequest pageRequest = PageRequest.of(0, 1);
		PlaceExtractionJob job = extractionJobRepository
			.findLatestExtractionJob(collection.getId(), user.getId(), pageRequest)
			.stream()
			.findFirst()
			.orElseThrow(() -> new BusinessException(PlaceExtractionJobError.JOB_NOT_FOUND));

		return toExtractionJobDetailResponse(job);
	}

	@Transactional
	public AddExtractedPlacesResponse addExtractedPlaces(
		String collectionId,
		String jobId,
		AddExtractedPlacesRequest request,
		AuthPrincipal user
	) {
		Collection collection = collectionService.getCollection(collectionId);
		collectionAuthorizer.verifyMember(user, collection.getId());

		PlaceExtractionJob job = findExtractionJob(jobId, collection.getId(), user.getId());
		job.select();

		// 이미 컬렉션에 저장된 장소는 제외하고 CollectionPlace로 변환
		List<CollectionPlace> newCollectionPlaces = socialMediaPlaceRepository
			.findPlacesNotAddedToCollection(job.getSocialMedia().getId(), request.placeIds(), collection.getId())
			.stream()
			.map(place -> CollectionPlace.create(collection, place, job.getMember(), job.getSocialMedia()))
			.toList();

		collectionPlaceRepository.saveAll(newCollectionPlaces);

		// Pick/Pass 정보 일괄 조회 (N+1 문제 방지)
		CollectionMember member = collectionMemberService.findMemberByUserId(collection.getId(), user.getId());
		List<Long> collectionPlaceIds = newCollectionPlaces.stream()
			.map(CollectionPlace::getId)
			.toList();
		
		java.util.Map<Long, PickPassResponse> pickPassMap = 
			collectionPlaceQueryService.getPickPassBatch(collectionPlaceIds, member.getExternalId());
		
		List<CollectionPlaceResponse> addedPlaces = newCollectionPlaces.stream()
			.map(collectionPlace -> {
				PickPassResponse pickPass = pickPassMap.get(collectionPlace.getId());
				return CollectionPlaceResponse.of(
					collectionPlace,
					collectionPlaceQueryService.toPlaceResponse(collectionPlace),
					pickPass
				);
			})
			.toList();

		int totalSize = request.placeIds().size();
		return AddExtractedPlacesResponse.of(totalSize, addedPlaces);
	}

	@Transactional
	public void ignoreExtractionJob(String collectionId, String jobId, AuthPrincipal user) {
		Collection collection = collectionService.getCollection(collectionId);
		collectionAuthorizer.verifyMember(user, collection.getId());

		PlaceExtractionJob job = findExtractionJob(jobId, collection.getId(), user.getId());
		job.ignore();
	}

	private PlaceExtractionJob findExtractionJob(String jobId, Long collectionId, Long userId) {
		return extractionJobRepository
			.findByJobIdAndCollectionIdAndUserId(jobId, collectionId, userId)
			.orElseThrow(() -> new BusinessException(PlaceExtractionJobError.JOB_NOT_FOUND));
	}

	private ExtractionJobDetailResponse toExtractionJobDetailResponse(PlaceExtractionJob job) {
		List<SocialMediaPlace> socialMediaPlaces = socialMediaPlaceRepository
			.findAllBySocialMediaId(job.getSocialMedia().getId());

		return ExtractionJobDetailResponse.of(job, socialMediaPlaces);
	}
}
