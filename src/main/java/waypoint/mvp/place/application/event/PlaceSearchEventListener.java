package waypoint.mvp.place.application.event;

import java.util.Optional;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import waypoint.mvp.place.application.PlaceSearchService;
import waypoint.mvp.place.application.PlaceService;
import waypoint.mvp.place.application.SocialMediaPlaceService;
import waypoint.mvp.place.domain.Place;
import waypoint.mvp.place.domain.event.PlaceSearchRequestedEvent;
import waypoint.mvp.place.error.SearchFailureCode;
import waypoint.mvp.place.error.exception.PlaceSearchException;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlaceSearchEventListener {

	private final SocialMediaPlaceService socialMediaPlaceService;
	private final PlaceSearchService placeSearchService;
	private final PlaceService placeService;

	@Async("placeSearchTaskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handlePlaceSearchRequestedEvent(PlaceSearchRequestedEvent event) {
		Long socialMediaPlaceId = event.socialMediaPlaceId();

		try {
			log.info("장소 검색 이벤트 수신: socialMediaPlaceId={}", socialMediaPlaceId);

			executePlaceSearch(socialMediaPlaceId);

			log.info("장소 검색 이벤트 성공: socialMediaPlaceId={}", socialMediaPlaceId);

		} catch (PlaceSearchException e) {
			log.error("장소 검색 이벤트 실패: socialMediaPlaceId={}, failureCode={}",
				socialMediaPlaceId, e.getFailureCode());

			// 상태 변경 PROCESSING → RETRY_WAITING, FAILED
			socialMediaPlaceService.fail(socialMediaPlaceId, e.getFailureCode());
		} catch (Exception e) {
			SearchFailureCode code = SearchFailureCode.UNEXPECTED_ERROR;

			log.error("장소 검색 이벤트 실패(예기치 못한 오류): socialMediaPlaceId={}, failureCode={}",
				socialMediaPlaceId, code, e);

			// 상태 변경 PROCESSING → FAILED
			socialMediaPlaceService.fail(socialMediaPlaceId, code);
		}
	}

	private void executePlaceSearch(Long socialMediaPlaceId) {
		// 상태 변경 PENDING → PROCESSING 및 쿼리 조회
		String query = socialMediaPlaceService.startSearch(socialMediaPlaceId);
		log.info("장소 검색 이벤트 시작: socialMediaPlaceId={}, query={}", socialMediaPlaceId, query);

		// 검색 쿼리에 대한 placeId 조회
		Optional<String> placeId = placeSearchService.searchTop1PlaceId(query);
		if (placeId.isEmpty()) {

			// 상태 변경 PROCESSING → NOT_FOUND
			socialMediaPlaceService.completeAsNotFound(socialMediaPlaceId);
			log.info("장소 검색 결과 없음(placeId): socialMediaPlaceId={}, query={}", socialMediaPlaceId, query);
			return;
		}

		// 이미 DB에 있는 장소라면 연결하고 즉시 종료
		Optional<Place> existingPlace = placeService.getPlace(placeId.get());
		if (existingPlace.isPresent()) {

			// 상태 변경 PROCESSING → COMPLETED
			socialMediaPlaceService.completeSearch(socialMediaPlaceId, existingPlace.get());
			log.info("장소 검색 이벤트 성공(이미 있는 장소): socialMediaPlaceId={}, placeId={}",
				socialMediaPlaceId, placeId.get());
			return;
		}

		// 상세 정보 조회
		Optional<Place> place = placeSearchService.fetchPlaceDetails(placeId.get());
		if (place.isEmpty()) {

			// 상태 변경 PROCESSING → NOT_FOUND
			socialMediaPlaceService.completeAsNotFound(socialMediaPlaceId);
			log.info("장소 검색 결과 없음(detail): socialMediaPlaceId={}, placeId={}", socialMediaPlaceId, placeId);
			return;
		}

		// 상태 변경 PROCESSING → COMPLETED
		socialMediaPlaceService.completeSearch(socialMediaPlaceId, place.get());
	}
}
