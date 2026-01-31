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
import waypoint.mvp.collection.application.dto.response.CollectionPlaceListResponse;
import waypoint.mvp.collection.application.dto.response.CollectionPlaceResponse;
import waypoint.mvp.collection.application.dto.response.ExtractionJobResponse;
import waypoint.mvp.collection.application.dto.response.PickPassResponse;
import waypoint.mvp.collection.application.dto.response.SocialMediaResponse;
import waypoint.mvp.collection.domain.Collection;
import waypoint.mvp.collection.domain.CollectionMember;
import waypoint.mvp.collection.domain.CollectionPlace;
import waypoint.mvp.collection.domain.CollectionPlaceDraft;
import waypoint.mvp.collection.domain.CollectionPlacePreference;
import waypoint.mvp.collection.domain.service.CollectionAuthorizer;
import waypoint.mvp.collection.error.CollectionError;
import waypoint.mvp.collection.error.CollectionPlaceError;
import waypoint.mvp.collection.infrastructure.persistence.CollectionPlaceDraftRepository;
import waypoint.mvp.collection.infrastructure.persistence.CollectionPlacePreferenceRepository;
import waypoint.mvp.collection.infrastructure.persistence.CollectionPlaceRepository;
import waypoint.mvp.collection.infrastructure.persistence.CollectionRepository;
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

	private final CollectionAuthorizer collectionAuthorizer;
	private final CollectionRepository collectionRepository;
	private final CollectionMemberService collectionMemberService;

	private final PlaceRepository placeRepository;
	private final CollectionPlaceRepository collectionPlaceRepository;
	private final CollectionPlacePreferenceRepository preferenceRepository;

	private final SocialMediaService socialMediaService;
	private final CollectionPlaceDraftRepository jobRepository;

	@Transactional
	public CollectionPlaceResponse addPlace(
		Long collectionId,
		CollectionPlaceCreateRequest request,
		AuthPrincipal principal
	) {
		Collection collection = getCollection(collectionId);
		CollectionMember me = getActiveMember(collectionId, principal.getId());

		Long placeId = parsePlaceId(request.placeId());
		Place place = placeRepository.findById(placeId)
			.orElseThrow(() -> new BusinessException(PlaceError.PLACE_NOT_FOUND));

		if (collectionPlaceRepository.existsByCollectionIdAndPlaceId(collectionId, placeId)) {
			throw new BusinessException(CollectionPlaceError.COLLECTION_PLACE_ALREADY_EXISTS);
		}

		CollectionPlace collectionPlace = CollectionPlace.create(collection, place, me);
		CollectionPlace saved = collectionPlaceRepository.save(collectionPlace);

		PlaceResponse placeResponse = PlaceResponse.from(place, extractPhotos(place));
		return CollectionPlaceResponse.of(saved, placeResponse, List.of(), List.of());
	}

	@Transactional
	public ExtractionJobResponse addPlacesFromUrl(
		Long collectionId,
		CollectionPlaceFromUrlRequest request,
		AuthPrincipal principal
	) {
		CollectionMember member = collectionMemberService.getMemberByUserId(collectionId, principal.getId());

		// 장소 추출 이벤트 요청
		SocialMediaInfo socialMediaInfo = socialMediaService.addJob(request.url());

		// 어떤 멤버가 어떤 URL을 요청했는지 구분하기 위한 중간 테이블
		CollectionPlaceDraft draft = createOrGetDraft(member, socialMediaInfo.id());

		return new ExtractionJobResponse(
			draft.getId().toString(),
			socialMediaInfo.status()
		);
	}

	private CollectionPlaceDraft createOrGetDraft(CollectionMember member, Long socialMediaId) {
		return jobRepository.findByMemberIdAndSocialMediaId(member.getId(), socialMediaId)
			.orElseGet(() -> {
				SocialMedia media = socialMediaService.getSocialMedia(socialMediaId);
				CollectionPlaceDraft draft = CollectionPlaceDraft.create(member, media);
				return jobRepository.save(draft);
			});
	}

	public CollectionPlaceListResponse getPlaces(
		Long collectionId,
		int page,
		int size,
		CollectionPlaceSort sort,
		Long addedByMemberId,
		AuthPrincipal principal
	) {
		collectionAuthorizer.verifyAccess(principal, collectionId);

		int safePage = Math.max(page, 1);
		int safeSize = Math.max(size, 1);

		Sort jpaSort = Sort.by(sort == CollectionPlaceSort.LATEST ? Sort.Direction.DESC : Sort.Direction.ASC, "createdAt")
			.and(Sort.by(Sort.Direction.DESC, "id"));

		PageRequest pageable = PageRequest.of(safePage - 1, safeSize, jpaSort);

		Slice<CollectionPlace> result;
		if (addedByMemberId != null) {
			collectionMemberService.getMember(collectionId, addedByMemberId);

			result = collectionPlaceRepository.findAllByCollectionIdAndAddedById(
				collectionId,
				addedByMemberId,
				pageable
			);
		} else {
			result = collectionPlaceRepository.findAllByCollectionId(collectionId, pageable);
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

		return CollectionPlaceListResponse.of(
			content,
			result.hasNext(),
			safeSize,
			safePage
		);
	}

	public CollectionPlaceDetailResponse getPlaceDetail(
		Long collectionId,
		Long collectionPlaceId,
		AuthPrincipal principal
	) {
		collectionAuthorizer.verifyAccess(principal, collectionId);

		CollectionPlace collectionPlace = getCollectionPlace(collectionId, collectionPlaceId);

		PlaceResponse placeResponse = PlaceResponse.from(
			collectionPlace.getPlace(),
			extractPhotos(collectionPlace.getPlace())
		);
		SocialMediaResponse socialMediaResponse = toSocialMediaResponse(collectionPlace);

		return CollectionPlaceDetailResponse.of(collectionPlace, placeResponse, socialMediaResponse);
	}

	@Transactional
	public void updateMemo(
		Long collectionId,
		Long collectionPlaceId,
		CollectionPlaceUpdateRequest request,
		AuthPrincipal principal
	) {
		getActiveMember(collectionId, principal.getId());

		CollectionPlace collectionPlace = getCollectionPlace(collectionId, collectionPlaceId);
		collectionPlace.updateMemo(request.memo());
	}

	@Transactional
	public void deletePlace(
		Long collectionId,
		Long collectionPlaceId,
		AuthPrincipal principal
	) {
		getActiveMember(collectionId, principal.getId());

		CollectionPlace collectionPlace = getCollectionPlace(collectionId, collectionPlaceId);
		collectionPlaceRepository.delete(collectionPlace);
	}

	@Transactional
	public PickPassResponse pickOrPass(
		Long collectionId,
		Long collectionPlaceId,
		CollectionPlacePreference.Type type,
		AuthPrincipal principal
	) {
		CollectionMember me = getActiveMember(collectionId, principal.getId());
		CollectionPlace place = getCollectionPlace(collectionId, collectionPlaceId);

		Optional<CollectionPlacePreference> existingOpt =
			preferenceRepository.findByPlaceIdAndMemberId(collectionPlaceId, me.getId());

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
			preferenceRepository.findAllByPlaceIdIn(List.of(collectionPlaceId))
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

	private Collection getCollection(Long collectionId) {
		return collectionRepository.findById(collectionId)
			.orElseThrow(() -> new BusinessException(CollectionError.COLLECTION_NOT_FOUND));
	}

	private CollectionMember getActiveMember(Long collectionId, Long userId) {
		try {
			return collectionMemberService.getMemberByUserId(collectionId, userId);
		} catch (BusinessException e) {
			throw new BusinessException(CollectionError.FORBIDDEN_NOT_MEMBER, e);
		}
	}

	private CollectionPlace getCollectionPlace(Long collectionId, Long collectionPlaceId) {
		return collectionPlaceRepository.findByIdAndCollectionId(collectionPlaceId, collectionId)
			.orElseThrow(() -> new BusinessException(CollectionPlaceError.COLLECTION_PLACE_NOT_FOUND));
	}

	private Long parsePlaceId(String raw) {
		try {
			return Long.parseLong(raw);
		} catch (NumberFormatException e) {
			throw new BusinessException(CollectionPlaceError.INVALID_PLACE_ID, e);
		}
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
