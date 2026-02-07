package waypoint.mvp.place.application;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.place.application.dto.SocialMediaInfo;
import waypoint.mvp.place.application.dto.llm.PlaceAnalysis;
import waypoint.mvp.place.application.dto.llm.PlaceExtractionResult;
import waypoint.mvp.place.domain.SocialMedia;
import waypoint.mvp.place.domain.content.ContentSnapshot;
import waypoint.mvp.place.domain.event.PlaceExtractionRequestedEvent;
import waypoint.mvp.place.error.ExtractFailureCode;
import waypoint.mvp.place.error.SocialMediaError;
import waypoint.mvp.place.infrastructure.persistence.SocialMediaRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SocialMediaService {

	private final SocialMediaPlaceService socialMediaPlaceService;
	private final SocialMediaRepository socialMediaRepository;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public SocialMediaInfo addJob(String url) {
		SocialMedia socialMedia = createOrGetSocialMedia(url);
		return SocialMediaInfo.from(socialMedia);
	}

	public SocialMedia getSocialMedia(Long socialMediaId) {
		return socialMediaRepository.findById(socialMediaId)
			.orElseThrow(() -> new BusinessException(SocialMediaError.SOCIAL_MEDIA_NOT_FOUND));
	}

	/**
	 * 소셜 미디어의 상태를 {@code EXTRACTING}으로 변경합니다.
	 *
	 * @param socialMediaId 소셜 미디어 ID
	 */
	@Transactional
	public void startExtraction(Long socialMediaId) {
		SocialMedia socialMedia = getSocialMedia(socialMediaId);
		socialMedia.startExtraction();
	}

	/**
	 * 장소 추출이 완료되면 소셜 미디어의 상태를 {@code SEARCHING}으로 변경하고, LLM 분석 결과와 콘텐츠 스냅샷을 저장합니다.
	 * 그리고 검색 쿼리를 {@code SocialMediaPlace}에 저장하고 검색 이벤트를 발행합니다.
	 *
	 * @param socialMediaId 소셜 미디어 ID
	 * @param result 장소 추출 결과
	 */
	@Transactional
	public void completeExtraction(Long socialMediaId, PlaceExtractionResult result) {
		SocialMedia socialMedia = getSocialMedia(socialMediaId);

		PlaceAnalysis analysis = result.placeAnalysis();
		ContentSnapshot snapshot = result.rawContent().toSnapshot();

		socialMedia.completeExtraction(analysis.summary(), snapshot);

		// 검색 쿼리 저장
		socialMediaPlaceService.addPlaces(socialMedia, analysis.searchQueries());
	}

	/**
	 * 장소 추출 실패 시 소셜 미디어의 상태를 {@code FAILED}로 변경하고, 실패 코드를 기록합니다.
	 *
	 * @param socialMediaId 실패 처리할 소셜 미디어의 ID
	 * @param failureCode 추출 실패 코드
	 */
	@Transactional
	public void fail(Long socialMediaId, ExtractFailureCode failureCode) {
		SocialMedia socialMedia = getSocialMedia(socialMediaId);
		socialMedia.fail(failureCode);
	}

	private SocialMedia createOrGetSocialMedia(String url) {
		return socialMediaRepository.findByUrl(url)
			.orElseGet(() -> {
				SocialMedia socialMedia = socialMediaRepository.save(SocialMedia.create(url));

				// 장소 추출 이벤트 발행
				PlaceExtractionRequestedEvent event = new PlaceExtractionRequestedEvent(socialMedia.getId());
				eventPublisher.publishEvent(event);

				return socialMedia;
			});
	}
}
