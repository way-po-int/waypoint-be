package waypoint.mvp.collection.application;

import static java.util.stream.Collectors.*;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.collection.application.dto.response.CollectionMemberResponse;
import waypoint.mvp.collection.application.dto.response.CollectionPlaceDetailResponse;
import waypoint.mvp.collection.application.dto.response.CollectionPlaceResponse;
import waypoint.mvp.collection.application.dto.response.PickPassResponse;
import waypoint.mvp.collection.application.dto.response.SocialMediaResponse;
import waypoint.mvp.collection.domain.Collection;
import waypoint.mvp.collection.domain.CollectionPlace;
import waypoint.mvp.collection.domain.CollectionPlacePreference;
import waypoint.mvp.collection.error.CollectionError;
import waypoint.mvp.collection.error.CollectionPlaceError;
import waypoint.mvp.collection.infrastructure.persistence.CollectionPlacePreferenceRepository;
import waypoint.mvp.collection.infrastructure.persistence.CollectionPlaceRepository;
import waypoint.mvp.collection.infrastructure.persistence.CollectionRepository;
import waypoint.mvp.global.common.SliceResponse;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.place.application.PlacePhotoService;
import waypoint.mvp.place.application.dto.PlaceResponse;
import waypoint.mvp.place.domain.Place;
import waypoint.mvp.plan.application.PlanCollectionService;
import waypoint.mvp.plan.domain.Plan;

/**
 * {@link CollectionPlaceQueryService} : 순수 조회 전용 서비스
 *
 * 이 서비스는 verifyMember()와 같은 권한 검증 로직을 포함하지 않습니다.
 * 오직 데이터 조회에만 집중하며, 다른 서비스에서 공통으로 사용할 수 있습니다.
 *
 * <h3>주요 사용처:</h3>
 * <li> {@link CollectionPlaceService}에서 기본적으로 사용 </li>
 * <li> {@link PlanCollectionService}에서 {@link Plan} 멤버가 {@link Collection} 멤버가 아닌 경우에도 {@link CollectionPlace} 정보 조회 </li>
 * <li> 권한 검증이 이미 완료된 상태에서 순수 데이터 조회가 필요한 경우 </li>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollectionPlaceQueryService {

	private final CollectionRepository collectionRepository;
	private final CollectionPlaceRepository collectionPlaceRepository;
	private final CollectionPlacePreferenceRepository preferenceRepository;
	private final PlacePhotoService placePhotoService;

	/**
	 * CollectionPlace 정보 조회 (권한 검증 없음)
	 */
	public CollectionPlace getCollectionPlace(Collection collection, String collectionPlaceId) {
		return collectionPlaceRepository.findByExternalIdAndCollectionId(collectionPlaceId, collection.getId())
			.orElseThrow(() -> new BusinessException(CollectionPlaceError.COLLECTION_PLACE_NOT_FOUND));
	}

	/**
	 * Collection의 장소 목록 조회 (권한 검증 없음)
	 */
	public SliceResponse<CollectionPlaceResponse> getPlacesByCollectionId(
		Long collectionId,
		String addedByMemberId,
		Pageable pageable
	) {
		Slice<CollectionPlace> result;
		if (addedByMemberId != null) {
			result = collectionPlaceRepository.findAllByCollectionIdAndAddedByExternalId(
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

		return SliceResponse.from(result, content);
	}

	/**
	 * CollectionPlace 상세 정보 조회 (권한 검증 없음)
	 */
	public CollectionPlaceDetailResponse getPlaceDetail(
		String collectionId,
		String collectionPlaceId
	) {
		Collection collection = getCollectionById(collectionId);
		CollectionPlace collectionPlace = getCollectionPlace(collection, collectionPlaceId);

		PlaceResponse placeResponse = PlaceResponse.from(
			collectionPlace.getPlace(),
			extractPhotos(collectionPlace.getPlace())
		);
		SocialMediaResponse socialMediaResponse = toSocialMediaResponse(collectionPlace);

		return CollectionPlaceDetailResponse.of(collectionPlace, placeResponse, socialMediaResponse);
	}

	/**
	 * CollectionPlace의 Pick/Pass 현재 상태 조회 (권한 검증 없음)
	 */
	public PickPassResponse getPickPass(Long collectionPlaceId) {
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

	/**
	 * Collection ID로 Collection 정보 조회 (권한 검증 없음)
	 */
	private Collection getCollectionById(String collectionId) {
		return collectionRepository.findByExternalId(collectionId)
			.orElseThrow(() -> new BusinessException(CollectionError.COLLECTION_NOT_FOUND));
	}

	/**
	 * CollectionPlacePreference 정보 그룹화 (권한 검증 없음)
	 */
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

	/**
	 * Place 사진 정보 추출
	 */
	private List<String> extractPhotos(Place place) {
		return placePhotoService.resolveRepresentativePhotoUris(place);
	}

	/**
	 * SocialMedia 정보 변환
	 */
	private SocialMediaResponse toSocialMediaResponse(CollectionPlace collectionPlace) {
		if (collectionPlace.getSocialMedia() == null) {
			return null;
		}
		return SocialMediaResponse.from(collectionPlace.getSocialMedia());
	}
}
