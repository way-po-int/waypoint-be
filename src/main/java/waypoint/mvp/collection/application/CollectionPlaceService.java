package waypoint.mvp.collection.application;

import static java.util.stream.Collectors.*;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.AuthPrincipal;
import waypoint.mvp.collection.application.dto.request.CollectionPlaceCreateRequest;
import waypoint.mvp.collection.application.dto.request.CollectionPlaceFromUrlRequest;
import waypoint.mvp.collection.application.dto.request.CollectionPlaceUpdateRequest;
import waypoint.mvp.collection.application.dto.response.CollectionMemberResponse;
import waypoint.mvp.collection.application.dto.response.CollectionPlaceDetailResponse;
import waypoint.mvp.collection.application.dto.response.CollectionPlaceResponse;
import waypoint.mvp.collection.application.dto.response.ExtractionJobResponse;
import waypoint.mvp.collection.application.dto.response.PickPassResponse;
import waypoint.mvp.collection.application.dto.response.SocialMediaResponse;
import waypoint.mvp.collection.domain.Collection;
import waypoint.mvp.collection.domain.CollectionMember;
import waypoint.mvp.collection.domain.CollectionPlace;
import waypoint.mvp.collection.domain.CollectionPlaceDraft;
import waypoint.mvp.collection.domain.CollectionPlacePreference;
import waypoint.mvp.collection.error.CollectionError;
import waypoint.mvp.collection.error.CollectionPlaceError;
import waypoint.mvp.collection.infrastructure.persistence.CollectionPlaceDraftRepository;
import waypoint.mvp.collection.infrastructure.persistence.CollectionPlacePreferenceRepository;
import waypoint.mvp.collection.infrastructure.persistence.CollectionPlaceRepository;
import waypoint.mvp.collection.infrastructure.persistence.CollectionRepository;
import waypoint.mvp.global.auth.ResourceAuthorizer;
import waypoint.mvp.global.common.SliceResponse;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.place.application.SocialMediaService;
import waypoint.mvp.place.application.dto.PlaceResponse;
import waypoint.mvp.place.application.dto.SocialMediaInfo;
import waypoint.mvp.place.domain.Place;
import waypoint.mvp.place.domain.SocialMedia;
import waypoint.mvp.place.error.PlaceError;
import waypoint.mvp.place.infrastructure.persistence.PlaceRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollectionPlaceService {

	private final ResourceAuthorizer collectionAuthorizer;
	private final CollectionRepository collectionRepository;
	private final CollectionMemberService collectionMemberService;

	private final PlaceRepository placeRepository;
	private final CollectionPlaceRepository collectionPlaceRepository;
	private final CollectionPlacePreferenceRepository preferenceRepository;

	private final SocialMediaService socialMediaService;
	private final CollectionPlaceDraftRepository jobRepository;

	@Transactional
	public CollectionPlaceResponse addPlace(
		String collectionId,
		CollectionPlaceCreateRequest request,
		AuthPrincipal principal
	) {
		Collection collection = getCollection(collectionId);
		collectionAuthorizer.verifyMember(principal, collection.getId());
		CollectionMember me = getActiveMember(collection.getId(), principal.getId());

		Place place = getPlace(request.placeId());

		if (collectionPlaceRepository.existsByCollectionIdAndPlaceId(collection.getId(), place.getId())) {
			throw new BusinessException(CollectionPlaceError.COLLECTION_PLACE_ALREADY_EXISTS);
		}

		CollectionPlace saved = collectionPlaceRepository.save(CollectionPlace.create(collection, place, me));
		PlaceResponse placeResponse = PlaceResponse.from(place, extractPhotos(place));
		return CollectionPlaceResponse.of(saved, placeResponse, List.of(), List.of());
	}

	private Place getPlace(String placeId) {
		return placeRepository.findByExternalId(placeId)
			.orElseThrow(() -> new BusinessException(PlaceError.PLACE_NOT_FOUND));
	}

	@Transactional
	public ExtractionJobResponse addPlacesFromUrl(
		String collectionId,
		CollectionPlaceFromUrlRequest request,
		AuthPrincipal principal
	) {
		Collection collection = getCollection(collectionId);
		collectionAuthorizer.verifyMember(principal, collection.getId());
		CollectionMember member = collectionMemberService.findMemberByUserId(collection.getId(), principal.getId());

		// 장소 추출 이벤트 요청
		SocialMediaInfo socialMediaInfo = socialMediaService.addJob(request.url());

		// 어떤 멤버가 어떤 URL을 요청했는지 구분하기 위한 중간 테이블
		CollectionPlaceDraft draft = createOrGetDraft(member, socialMediaInfo.id());

		return new ExtractionJobResponse(
			draft.getExternalId(),
			socialMediaInfo.status()
		);
	}

	private CollectionMember getActiveMember(Long collectionId, Long userId) {
		try {
			return collectionMemberService.findMemberByUserId(collectionId, userId);
		} catch (BusinessException e) {
			throw new BusinessException(CollectionError.FORBIDDEN_NOT_MEMBER, e);
		}
	}

	public SliceResponse<CollectionPlaceResponse> getPlaces(
		String collectionId,
		int page,
		int size,
		CollectionPlaceSort sort,
		String addedByMemberId,
		AuthPrincipal principal
	) {
		Collection collection = getCollection(collectionId);
		collectionAuthorizer.verifyAccess(principal, collection.getId());

		int safePage = Math.max(page, 1);
		int safeSize = Math.max(size, 1);

		Sort jpaSort = Sort.by(sort == CollectionPlaceSort.LATEST ? Sort.Direction.DESC : Sort.Direction.ASC,
				"createdAt")
			.and(Sort.by(Sort.Direction.DESC, "id"));

		PageRequest pageable = PageRequest.of(safePage - 1, safeSize, jpaSort);

		Slice<CollectionPlace> result;
		if (addedByMemberId != null) {
			collectionMemberService.getEntity(collection.getId(), addedByMemberId);
			result = collectionPlaceRepository.findAllByCollectionIdAndAddedByExternalId(
				collection.getId(),
				addedByMemberId,
				pageable
			);
		} else {
			result = collectionPlaceRepository.findAllByCollectionId(collection.getId(), pageable);
		}

		List<CollectionPlace> places = result.getContent();
		List<Long> collectionPlaceIds = places.stream().map(CollectionPlace::getId).toList();

		Map<Long, Map<CollectionPlacePreference.Type, List<CollectionMemberResponse>>> grouped =
			groupPreferences(collectionPlaceIds);

		List<CollectionPlaceResponse> content = places.stream().map(cp -> {
			PlaceResponse placeResponse = PlaceResponse.from(cp.getPlace(), extractPhotos(cp.getPlace()));

			Map<CollectionPlacePreference.Type, List<CollectionMemberResponse>> byType =
				grouped.getOrDefault(cp.getId(), Collections.emptyMap());

			List<CollectionMemberResponse> picked = byType.getOrDefault(CollectionPlacePreference.Type.PICK, List.of());
			List<CollectionMemberResponse> passed = byType.getOrDefault(CollectionPlacePreference.Type.PASS, List.of());

			return CollectionPlaceResponse.of(cp, placeResponse, picked, passed);
		}).toList();

		return new SliceResponse<>(content, result.hasNext(), safePage, safeSize);
	}

	public CollectionPlaceDetailResponse getPlaceDetail(
		String collectionId,
		String collectionPlaceId,
		AuthPrincipal principal
	) {
		Collection collection = getCollection(collectionId);
		collectionAuthorizer.verifyAccess(principal, collection.getId());

		CollectionPlace collectionPlace = getCollectionPlace(collection, collectionPlaceId);

		PlaceResponse placeResponse = PlaceResponse.from(
			collectionPlace.getPlace(),
			extractPhotos(collectionPlace.getPlace())
		);
		SocialMediaResponse socialMediaResponse = toSocialMediaResponse(collectionPlace);

		return CollectionPlaceDetailResponse.of(collectionPlace, placeResponse, socialMediaResponse);
	}

	@Transactional
	public void updateMemo(
		String collectionId,
		String collectionPlaceId,
		CollectionPlaceUpdateRequest request,
		AuthPrincipal principal
	) {
		Collection collection = getCollection(collectionId);
		collectionAuthorizer.verifyMember(principal, collection.getId());
		getActiveMember(collection.getId(), principal.getId());

		CollectionPlace collectionPlace = getCollectionPlace(collection, collectionPlaceId);
		collectionPlace.updateMemo(request.memo());
	}

	@Transactional
	public void deletePlace(
		String collectionId,
		String collectionPlaceId,
		AuthPrincipal principal
	) {
		Collection collection = getCollection(collectionId);
		collectionAuthorizer.verifyMember(principal, collection.getId());
		getActiveMember(collection.getId(), principal.getId());

		CollectionPlace collectionPlace = getCollectionPlace(collection, collectionPlaceId);
		collectionPlaceRepository.delete(collectionPlace);
	}

	@Transactional
	public PickPassResponse pickOrPass(
		String collectionId,
		String collectionPlaceId,
		CollectionPlacePreference.Type type,
		AuthPrincipal principal
	) {
		Collection collection = getCollection(collectionId);
		collectionAuthorizer.verifyMember(principal, collection.getId());

		CollectionMember me = getActiveMember(collection.getId(), principal.getId());
		CollectionPlace place = getCollectionPlace(collection, collectionPlaceId);

		Long collectionPlacePk = place.getId();

		Optional<CollectionPlacePreference> existingOpt =
			preferenceRepository.findByPlaceIdAndMemberId(collectionPlacePk, me.getId());

		if (existingOpt.isPresent()) {
			CollectionPlacePreference existing = existingOpt.get();

			if (existing.getType() == type) {
				preferenceRepository.delete(existing);
			} else {
				existing.changeType(type);
			}
		} else {
			preferenceRepository.save(CollectionPlacePreference.create(place, me, type));
		}

		Map<CollectionPlacePreference.Type, List<CollectionMemberResponse>> preferenceByType =
			preferenceRepository.findAllByPlaceIdIn(List.of(collectionPlacePk))
				.stream()
				.collect(groupingBy(
					CollectionPlacePreference::getType,
					mapping(p -> CollectionMemberResponse.from(p.getMember()), toList())
				));

		List<CollectionMemberResponse> picked =
			preferenceByType.getOrDefault(CollectionPlacePreference.Type.PICK, List.of());
		List<CollectionMemberResponse> passed =
			preferenceByType.getOrDefault(CollectionPlacePreference.Type.PASS, List.of());

		return PickPassResponse.of(picked, passed);
	}

	private CollectionPlaceDraft createOrGetDraft(CollectionMember member, Long socialMediaId) {
		return jobRepository.findByMemberIdAndSocialMediaId(member.getId(), socialMediaId)
			.orElseGet(() -> {
				SocialMedia media = socialMediaService.getSocialMedia(socialMediaId);
				CollectionPlaceDraft draft = CollectionPlaceDraft.create(member, media);
				return jobRepository.save(draft);
			});
	}

	private Collection getCollection(String collectionId) {
		return collectionRepository.findByExternalId(collectionId)
			.orElseThrow(() -> new BusinessException(CollectionError.COLLECTION_NOT_FOUND));
	}

	private CollectionPlace getCollectionPlace(Collection collection, String collectionPlaceId) {
		return collectionPlaceRepository.findByExternalIdAndCollectionId(collectionPlaceId, collection.getId())
			.orElseThrow(() -> new BusinessException(CollectionPlaceError.COLLECTION_PLACE_NOT_FOUND));
	}

	private List<String> extractPhotos(Place place) {
		if (place.getDetail() == null || place.getDetail().getPhotoName() == null) {
			return List.of();
		}
		return List.of(place.getDetail().getPhotoName());
	}

	private SocialMediaResponse toSocialMediaResponse(CollectionPlace collectionPlace) {
		if (collectionPlace.getSocialMedia() == null) {
			return null;
		}
		return SocialMediaResponse.from(collectionPlace.getSocialMedia());
	}

	private Map<Long, Map<CollectionPlacePreference.Type, List<CollectionMemberResponse>>> groupPreferences(
		List<Long> collectionPlaceIds
	) {
		if (collectionPlaceIds.isEmpty()) {
			return Collections.emptyMap();
		}

		List<CollectionPlacePreference> preference = preferenceRepository.findAllByPlaceIdIn(collectionPlaceIds);

		return preference.stream()
			.collect(groupingBy(
				pref -> pref.getPlace().getId(),
				groupingBy(
					CollectionPlacePreference::getType,
					() -> new EnumMap<>(CollectionPlacePreference.Type.class),
					mapping(pref -> CollectionMemberResponse.from(pref.getMember()), toList())
				)
			));
	}

	public enum CollectionPlaceSort {
		LATEST, OLDEST
	}
}
