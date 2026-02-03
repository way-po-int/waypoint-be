package waypoint.mvp.place.application.event;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import waypoint.mvp.place.application.PlaceExtractService;
import waypoint.mvp.place.application.SocialMediaService;
import waypoint.mvp.place.application.dto.llm.PlaceExtractionResult;
import waypoint.mvp.place.domain.ExtractFailureCode;
import waypoint.mvp.place.domain.SocialMedia;
import waypoint.mvp.place.domain.event.PlaceExtractionRequestedEvent;
import waypoint.mvp.place.error.exception.ExtractionException;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlaceExtractionEventListener {

	private final SocialMediaService socialMediaService;
	private final PlaceExtractService placeExtractService;

	@Async("placeExtractionTaskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handlePlaceExtractionRequestedEvent(PlaceExtractionRequestedEvent event) {
		Long socialMediaId = event.socialMediaId();

		try {
			log.info("장소 추출 이벤트 수신: socialMediaId={}", socialMediaId);

			// 상태 변경 PENDING → EXTRACTING
			socialMediaService.startExtraction(socialMediaId);

			SocialMedia socialMedia = socialMediaService.getSocialMedia(socialMediaId);
			PlaceExtractionResult result = placeExtractService.extract(socialMedia.getType(), socialMedia.getUrl());

			// 장소를 찾지 못했다면 예외 발생
			if (result.placeAnalysis().searchQueries().isEmpty()) {
				throw new ExtractionException(ExtractFailureCode.NO_PLACE_EXTRACTED);
			}

			// 상태 변경 EXTRACTING → SEARCHING
			socialMediaService.completeExtraction(socialMediaId, result);

			log.info("장소 추출 이벤트 성공: socialMediaId={}", socialMediaId);

		} catch (ExtractionException e) {
			log.atError()
				.setMessage("장소 추출 이벤트 실패: socialMediaId={}, failureCode={}")
				.addArgument(socialMediaId)
				.addArgument(e.getFailureCode())
				.setCause(e.isRetryable() ? e : null)
				.log();

			// 상태 변경 EXTRACTING → FAILED
			socialMediaService.fail(socialMediaId, e.getFailureCode());
		} catch (Exception e) {
			ExtractFailureCode code = ExtractFailureCode.UNEXPECTED_ERROR;

			log.error("장소 추출 이벤트 실패(예기치 못한 오류): socialMediaId={}, failureCode={}",
				socialMediaId, code, e);

			// 상태 변경 EXTRACTING → FAILED
			socialMediaService.fail(socialMediaId, code);
		}
	}
}
