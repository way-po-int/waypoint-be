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

	@Async("extractionTaskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handlePlaceExtractionRequestedEvent(PlaceExtractionRequestedEvent event) {
		Long socialMediaId = event.socialMediaId();

		try {
			log.info("장소 추출 이벤트 수신: id={}", socialMediaId);

			// 상태 변경 PENDING → ANALYZING
			socialMediaService.startAnalysis(socialMediaId);

			SocialMedia socialMedia = socialMediaService.getSocialMedia(socialMediaId);
			PlaceExtractionResult result = placeExtractService.extract(socialMedia.getType(), socialMedia.getUrl());

			// 장소를 찾지 못했다면 예외 발생
			if (result.placeAnalysis().searchQueries().isEmpty()) {
				throw new ExtractionException(ExtractFailureCode.NO_PLACE_EXTRACTED);
			}

			// 상태 변경 ANALYZING → VERIFYING
			socialMediaService.completeAnalysis(socialMediaId, result);

			log.info("장소 추출 이벤트 성공: id={}", socialMediaId);

		} catch (Exception e) {
			ExtractFailureCode failureCode = e instanceof ExtractionException ex
				? ex.getFailureCode()
				: ExtractFailureCode.GENAI_ERROR;

			log.atError()
				.setMessage("장소 추출 이벤트 실패: socialMediaId={}, failureCode={}")
				.addArgument(socialMediaId)
				.addArgument(failureCode)
				.setCause(failureCode.isRetryable() ? e : null)
				.log();

			// 상태 변경 ANALYZING → FAILED
			socialMediaService.failAnalysis(socialMediaId, failureCode);
		}
	}
}
