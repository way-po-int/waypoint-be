package waypoint.mvp.collection.application;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.AuthPrincipal;
import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.collection.application.dto.request.CollectionPlaceCreateRequest;
import waypoint.mvp.collection.application.dto.request.CollectionPlaceUpdateRequest;
import waypoint.mvp.collection.application.dto.response.CollectionPlaceDetailResponse;
import waypoint.mvp.collection.application.dto.response.CollectionPlaceListResponse;
import waypoint.mvp.collection.application.dto.response.CollectionPlaceResponse;
import waypoint.mvp.collection.application.dto.response.PickPassMemberResponse;
import waypoint.mvp.collection.application.dto.response.PickPassResponse;
import waypoint.mvp.collection.application.dto.response.PlaceResponse;
import waypoint.mvp.collection.application.dto.response.SocialMediaResponse;
import waypoint.mvp.collection.domain.Collection;
import waypoint.mvp.collection.domain.CollectionMember;
import waypoint.mvp.collection.domain.CollectionPlace;
import waypoint.mvp.collection.domain.CollectionPlacePreference;
import waypoint.mvp.collection.domain.service.CollectionAuthorizer;
import waypoint.mvp.collection.error.CollectionError;
import waypoint.mvp.collection.error.CollectionPlaceError;
import waypoint.mvp.collection.infrastructure.persistence.CollectionMemberRepository;
import waypoint.mvp.collection.infrastructure.persistence.CollectionPlacePreferenceRepository;
import waypoint.mvp.collection.infrastructure.persistence.CollectionPlaceRepository;
import waypoint.mvp.collection.infrastructure.persistence.CollectionRepository;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.place.domain.Place;
import waypoint.mvp.place.error.PlaceError;
import waypoint.mvp.place.infrastructure.persistence.PlaceRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollectionPlaceCrudService {

	private final CollectionAuthorizer collectionAuthorizer;
	private final CollectionRepository collectionRepository;
	private final CollectionMemberRepository collectionMemberRepository;

	private final PlaceRepository placeRepository;

	private final CollectionPlaceRepository collectionPlaceRepository;
	private final CollectionPlacePreferenceRepository preferenceRepository;

	@Transactional
	public CollectionPlaceResponse addPlace(Long collectionId, CollectionPlaceCreateRequest request,
		UserPrincipal principal) {

		Collection collection = getCollection(collectionId);
		CollectionMember me = getActiveMember(collectionId, principal.id());

		Long placeId = parseLongId(request.placeId(), CollectionPlaceError.INVALID_PLACE_ID);
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

	public CollectionPlaceListResponse getPlaces(
		Long collectionId,
		int page,
		int size,
		CollectionPlaceSort sort,
		boolean othersOnly,
		AuthPrincipal principal
	) {
		verifyAccess(principal, collectionId);

		int safePage = Math.max(page, 1);
		int safeSize = Math.max(size, 1);

		Sort jpaSort = Sort.by(sort == CollectionPlaceSort.LATEST ? Sort.Direction.DESC : Sort.Direction.ASC,
				"createdAt")
			.and(Sort.by(Sort.Direction.DESC, "id"));

		PageRequest pageable = PageRequest.of(safePage - 1, safeSize, jpaSort);

		Page<CollectionPlace> result;
		if (!principal.isGuest() && othersOnly) {
			CollectionMember me = getActiveMember(collectionId, principal.getId());
			result = collectionPlaceRepository.findAllByCollectionIdAndAddedByIdNot(collectionId, me.getId(), pageable);
		} else {
			result = collectionPlaceRepository.findAllByCollectionId(collectionId, pageable);
		}

		List<CollectionPlace> places = result.getContent();
		List<Long> collectionPlaceIds = places.stream().map(CollectionPlace::getId).toList();

		Map<Long, Map<CollectionPlacePreference.Type, List<PickPassMemberResponse>>> grouped =
			groupPreferences(collectionPlaceIds);

		List<CollectionPlaceResponse> content = places.stream().map(cp -> {
			PlaceResponse placeResponse = PlaceResponse.from(cp.getPlace(), extractPhotos(cp.getPlace()));

			Map<CollectionPlacePreference.Type, List<PickPassMemberResponse>> byType =
				grouped.getOrDefault(cp.getId(), Collections.emptyMap());

			List<PickPassMemberResponse> picked = byType.getOrDefault(CollectionPlacePreference.Type.PICK, List.of());
			List<PickPassMemberResponse> passed = byType.getOrDefault(CollectionPlacePreference.Type.PASS, List.of());

			return CollectionPlaceResponse.of(cp, placeResponse, picked, passed);
		}).toList();

		return CollectionPlaceListResponse.of(
			content,
			result.hasNext(),
			safeSize,
			safePage
		);
	}

	public CollectionPlaceDetailResponse getPlaceDetail(Long collectionId, Long collectionPlaceId,
		AuthPrincipal principal) {
		verifyAccess(principal, collectionId);

		CollectionPlace collectionPlace = getCollectionPlace(collectionId, collectionPlaceId);

		PlaceResponse placeResponse = PlaceResponse.from(collectionPlace.getPlace(),
			extractPhotos(collectionPlace.getPlace()));
		SocialMediaResponse socialMediaResponse = toSocialMediaResponse(collectionPlace);

		return CollectionPlaceDetailResponse.of(collectionPlace, placeResponse, socialMediaResponse);
	}

	@Transactional
	public void updateMemo(Long collectionId, Long collectionPlaceId, CollectionPlaceUpdateRequest request,
		UserPrincipal principal) {

		getActiveMember(collectionId, principal.id());

		CollectionPlace collectionPlace = getCollectionPlace(collectionId, collectionPlaceId);
		collectionPlace.updateMemo(request.memo());
	}

	@Transactional
	public void deletePlace(Long collectionId, Long collectionPlaceId, UserPrincipal principal) {

		getActiveMember(collectionId, principal.id());

		CollectionPlace collectionPlace = getCollectionPlace(collectionId, collectionPlaceId);

		collectionPlaceRepository.delete(collectionPlace);
	}

	@Transactional
	public PickPassResponse pickOrPass(
		Long collectionId,
		Long collectionPlaceId,
		CollectionPlacePreference.Type type,
		UserPrincipal principal
	) {
		CollectionMember me = getActiveMember(collectionId, principal.id());
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

		List<PickPassMemberResponse> picked = preferenceRepository.findAllByPlaceIdAndType(collectionPlaceId,
				CollectionPlacePreference.Type.PICK)
			.stream().map(p -> PickPassMemberResponse.from(p.getMember())).toList();

		List<PickPassMemberResponse> passed = preferenceRepository.findAllByPlaceIdAndType(collectionPlaceId,
				CollectionPlacePreference.Type.PASS)
			.stream().map(p -> PickPassMemberResponse.from(p.getMember())).toList();

		return PickPassResponse.of(picked, passed);
	}

	private void verifyAccess(AuthPrincipal principal, Long collectionId) {
		collectionAuthorizer.verifyAccess(principal, collectionId);
	}

	private Collection getCollection(Long collectionId) {
		return collectionRepository.findById(collectionId)
			.orElseThrow(() -> new BusinessException(CollectionError.COLLECTION_NOT_FOUND));
	}

	private CollectionMember getActiveMember(Long collectionId, Long userId) {
		return collectionMemberRepository.findActiveByUserId(collectionId, userId)
			.orElseThrow(() -> new BusinessException(CollectionError.FORBIDDEN_NOT_MEMBER));
	}

	private CollectionPlace getCollectionPlace(Long collectionId, Long collectionPlaceId) {
		return collectionPlaceRepository.findByIdAndCollectionId(collectionPlaceId, collectionId)
			.orElseThrow(() -> new BusinessException(CollectionPlaceError.COLLECTION_PLACE_NOT_FOUND));
	}

	private Long parseLongId(String raw, CollectionPlaceError error) {
		try {
			return Long.parseLong(raw);
		} catch (NumberFormatException e) {
			throw new BusinessException(error, e);
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

	private Map<Long, Map<CollectionPlacePreference.Type, List<PickPassMemberResponse>>> groupPreferences(
		List<Long> collectionPlaceIds) {
		if (collectionPlaceIds.isEmpty()) {
			return Collections.emptyMap();
		}

		List<CollectionPlacePreference> preference = preferenceRepository.findAllByPlaceIdIn(collectionPlaceIds);

		return preference.stream()
			.collect(java.util.stream.Collectors.groupingBy(
				pref -> pref.getPlace().getId(),
				java.util.stream.Collectors.groupingBy(
					CollectionPlacePreference::getType,
					() -> new EnumMap<>(CollectionPlacePreference.Type.class),
					java.util.stream.Collectors.mapping(
						pref -> PickPassMemberResponse.from(pref.getMember()),
						java.util.stream.Collectors.toList()
					)
				)
			));
	}

	public enum CollectionPlaceSort {
		LATEST, OLDEST
	}
}
