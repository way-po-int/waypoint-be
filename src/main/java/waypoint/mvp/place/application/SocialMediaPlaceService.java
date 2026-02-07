package waypoint.mvp.place.application;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.place.domain.Place;
import waypoint.mvp.place.domain.PlaceSearchStatus;
import waypoint.mvp.place.domain.SocialMedia;
import waypoint.mvp.place.domain.SocialMediaPlace;
import waypoint.mvp.place.domain.event.PlaceSearchRequestedEvent;
import waypoint.mvp.place.error.SearchFailureCode;
import waypoint.mvp.place.error.SocialMediaError;
import waypoint.mvp.place.infrastructure.persistence.SocialMediaPlaceRepository;
import waypoint.mvp.place.infrastructure.persistence.SocialMediaRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SocialMediaPlaceService {

	private final PlaceService placeService;
	private final SocialMediaRepository socialMediaRepository;
	private final SocialMediaPlaceRepository socialMediaPlaceRepository;
	private final ApplicationEventPublisher eventPublisher;

	/**
	 * 추출된 검색 쿼리들을 저장하고, 비동기 이벤트를 발행합니다.
	 *
	 * @param socialMedia 소셜 미디어
	 * @param searchQueries 검색 쿼리들
	 */
	@Transactional
	public void addPlaces(SocialMedia socialMedia, List<String> searchQueries) {
		List<SocialMediaPlace> places = searchQueries.stream()
			.map(query -> SocialMediaPlace.create(socialMedia, query))
			.toList();

		socialMediaPlaceRepository.saveAll(places);

		// 장소 검색 비동기 이벤트 발행
		places.forEach(place -> {
			PlaceSearchRequestedEvent event = new PlaceSearchRequestedEvent(place.getId());
			eventPublisher.publishEvent(event);
		});
	}

	/**
	 * 장소 검색 작업의 상태를 {@code PROCESSING}으로 변경하고, 검색할 쿼리를 반환합니다.
	 *
	 * @param socialMediaPlaceId 소셜 미디어 장소 ID
	 * @return 검색 쿼리
	 */
	@Transactional
	public String startSearch(Long socialMediaPlaceId) {
		SocialMediaPlace socialMediaPlace = getSocialMediaPlace(socialMediaPlaceId);
		socialMediaPlace.process();

		return socialMediaPlace.getSearchQuery();
	}

	/**
	 * 장소 검색이 완료되면 검색된 장소를 저장하고 상태를 {@code COMPLETED}로 변경합니다.
	 * 그리고 모든 작업이 완료되었는지 확인합니다.
	 *
	 * @param socialMediaPlaceId 소셜 미디어 장소 ID
	 * @param place 장소
	 */
	@Transactional
	public void completeSearch(Long socialMediaPlaceId, Place place) {
		SocialMediaPlace socialMediaPlace = getSocialMediaPlace(socialMediaPlaceId);
		Place savedPlace = placeService.createOrGetPlace(place);

		socialMediaPlace.complete(savedPlace);

		Long socialMediaId = socialMediaPlace.getSocialMedia().getId();
		completeSocialMediaIfFinished(socialMediaId);
	}

	/**
	 * 검색 결과가 존재하지 않는다면 상태를 {@code NOT_FOUND}로 변경합니다.
	 * 그리고 모든 작업이 완료되었는지 확인합니다.
	 *
	 * @param socialMediaPlaceId 소셜 미디어 장소 ID
	 */
	@Transactional
	public void completeAsNotFound(Long socialMediaPlaceId) {
		SocialMediaPlace socialMediaPlace = getSocialMediaPlace(socialMediaPlaceId);
		socialMediaPlace.notFound();

		Long socialMediaId = socialMediaPlace.getSocialMedia().getId();
		completeSocialMediaIfFinished(socialMediaId);
	}

	/**
	 * 장소 검색 실패 시 재시도가 가능한 실패 코드라면 {@code RETRY_WAITING}, 아니라면 {@code FAILED}로 상태를 변경합니다.
	 * {@code FAILED}인 경우 모든 작업이 완료되었는지 확인합니다.
	 *
	 * @param socialMediaPlaceId 소셜 미디어 장소 ID
	 * @param failureCode 검색 실패 코드
	 */
	@Transactional
	public void fail(Long socialMediaPlaceId, SearchFailureCode failureCode) {
		SocialMediaPlace socialMediaPlace = getSocialMediaPlace(socialMediaPlaceId);
		socialMediaPlace.fail(failureCode);

		if (socialMediaPlace.isFinished()) {
			Long socialMediaId = socialMediaPlace.getSocialMedia().getId();
			completeSocialMediaIfFinished(socialMediaId);
		}
	}

	/**
	 * 모든 장소 검색 작업이 완료되었는지 확인하고,
	 * 모든 작업이 종료되었다면 소셜 미디어의 상태를 {@code COMPLETED}로 변경합니다.
	 * <p>
	 * 여러 장소 검색 작업이 동시에 완료될 때 발생할 수 있는 레이스 컨디션을 방지하기 위해,
	 * 소셜 미디어 조회에 비관적 락을 사용하여 트랜잭션을 순차적으로 처리합니다.
	 *
	 * @param socialMediaId 소셜 미디어 ID
	 */
	private void completeSocialMediaIfFinished(Long socialMediaId) {
		SocialMedia socialMedia = getSocialMediaWithLock(socialMediaId);
		if (socialMedia.isFinished()) {
			return;
		}

		if (existsInProgressPlaces(socialMediaId)) {
			return;
		}

		socialMedia.complete();
	}

	private boolean existsInProgressPlaces(Long socialMediaId) {
		return socialMediaPlaceRepository.existsBySocialMediaIdAndStatusIn(socialMediaId,
			PlaceSearchStatus.IN_PROGRESS);
	}

	private SocialMedia getSocialMediaWithLock(Long socialMediaId) {
		return socialMediaRepository.lockById(socialMediaId)
			.orElseThrow(() -> new BusinessException(SocialMediaError.SOCIAL_MEDIA_NOT_FOUND));
	}

	private SocialMediaPlace getSocialMediaPlace(Long socialMediaPlaceId) {
		return socialMediaPlaceRepository.findById(socialMediaPlaceId)
			.orElseThrow(() -> new BusinessException(SocialMediaError.SOCIAL_MEDIA_PLACE_NOT_FOUND));
	}
}
