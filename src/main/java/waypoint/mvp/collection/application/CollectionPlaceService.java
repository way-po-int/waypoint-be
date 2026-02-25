package waypoint.mvp.collection.application;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.AuthPrincipal;
import waypoint.mvp.collection.application.dto.request.CollectionPlaceCreateRequest;
import waypoint.mvp.collection.application.dto.request.CollectionPlaceUpdateRequest;
import waypoint.mvp.collection.application.dto.response.CollectionPlaceDetailResponse;
import waypoint.mvp.collection.application.dto.response.CollectionPlaceResponse;
import waypoint.mvp.collection.application.dto.response.PickPassResponse;
import waypoint.mvp.collection.domain.Collection;
import waypoint.mvp.collection.domain.CollectionMember;
import waypoint.mvp.collection.domain.CollectionPlace;
import waypoint.mvp.collection.domain.CollectionPlacePreference;
import waypoint.mvp.collection.domain.PlaceSortType;
import waypoint.mvp.collection.error.CollectionError;
import waypoint.mvp.collection.error.CollectionPlaceError;
import waypoint.mvp.collection.infrastructure.persistence.CollectionPlacePreferenceRepository;
import waypoint.mvp.collection.infrastructure.persistence.CollectionPlaceRepository;
import waypoint.mvp.collection.infrastructure.persistence.CollectionRepository;
import waypoint.mvp.global.auth.ResourceAuthorizer;
import waypoint.mvp.global.common.SliceResponse;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.place.application.PlacePhotoService;
import waypoint.mvp.place.domain.Place;
import waypoint.mvp.place.error.PlaceError;
import waypoint.mvp.place.infrastructure.persistence.PlaceRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollectionPlaceService {

	private final ResourceAuthorizer collectionAuthorizer;
	private final CollectionRepository collectionRepository;
	private final CollectionMemberService collectionMemberService;
	private final CollectionPlaceQueryService collectionPlaceQueryService;

	private final PlaceRepository placeRepository;
	private final CollectionPlaceRepository collectionPlaceRepository;
	private final CollectionPlacePreferenceRepository preferenceRepository;

	private final PlacePhotoService placePhotoService;

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

		// 첫 번째 장소 추가 시 썸네일 자동 설정
		if (collection.isThumbnailEmpty()) {
			updateCollectionThumbnail(collection, place);
		}

		return CollectionPlaceResponse.of(
			saved,
			collectionPlaceQueryService.toPlaceResponse(saved),
			collectionPlaceQueryService.getPickPass(saved.getId(), me.getExternalId())
		);
	}

	private Place getPlace(String placeId) {
		return placeRepository.findByExternalId(placeId)
			.orElseThrow(() -> new BusinessException(PlaceError.PLACE_NOT_FOUND));
	}

	private void updateCollectionThumbnail(Collection collection, Place place) {
		String thumbnailUrl = placePhotoService.resolveRepresentativePhotoUris(place)
			.stream()
			.findFirst()
			.orElse(null);

		if (thumbnailUrl == null) {
			return;
		}
		int updatedCount = collectionRepository.updateThumbnailIfBlank(collection.getId(), thumbnailUrl);

		if (updatedCount > 0) {
			collection.updateThumbnail(thumbnailUrl);
		}
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
		String addedByMemberId,
		PlaceSortType sortType,
		Pageable pageable,
		AuthPrincipal principal
	) {
		Collection collection = getCollection(collectionId);
		collectionAuthorizer.verifyAccess(principal, collection.getId());

		CollectionMember member = getActiveMember(collection.getId(), principal.getId());
		return collectionPlaceQueryService.getPlacesByCollectionId(collection.getId(), addedByMemberId, sortType,
			pageable, member.getExternalId());
	}

	public CollectionPlaceDetailResponse getPlaceDetail(
		String collectionId,
		String collectionPlaceId,
		AuthPrincipal principal
	) {
		Collection collection = getCollection(collectionId);
		collectionAuthorizer.verifyAccess(principal, collection.getId());

		CollectionMember member = getActiveMember(collection.getId(), principal.getId());
		return collectionPlaceQueryService.getPlaceDetail(collectionId, collectionPlaceId, member.getExternalId());
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

		CollectionPlace collectionPlace = collectionPlaceQueryService.getCollectionPlace(collection, collectionPlaceId);
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

		CollectionPlace collectionPlace = collectionPlaceQueryService.getCollectionPlace(collection, collectionPlaceId);
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
		CollectionPlace place = collectionPlaceQueryService.getCollectionPlace(collection, collectionPlaceId);

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

		return collectionPlaceQueryService.getPickPass(collectionPlacePk, me.getExternalId());
	}

	private Collection getCollection(String collectionId) {
		return collectionRepository.findByExternalId(collectionId)
			.orElseThrow(() -> new BusinessException(CollectionError.COLLECTION_NOT_FOUND));
	}

}
