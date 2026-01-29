package waypoint.mvp.place.application;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.error.exception.BusinessException;
import waypoint.mvp.place.application.dto.SocialMediaInfo;
import waypoint.mvp.place.application.dto.llm.PlaceAnalysis;
import waypoint.mvp.place.application.dto.llm.PlaceExtractionResult;
import waypoint.mvp.place.domain.ExtractFailureCode;
import waypoint.mvp.place.domain.SocialMedia;
import waypoint.mvp.place.domain.content.ContentSnapshot;
import waypoint.mvp.place.domain.event.PlaceExtractionRequestedEvent;
import waypoint.mvp.place.error.SocialMediaError;
import waypoint.mvp.place.infrastructure.persistence.SocialMediaRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SocialMediaService {

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
	 * 소셜 미디어의 상태를 '처리 중'으로 변경합니다.
	 *
	 * @param socialMediaId 처리할 소셜 미디어의 ID
	 */
	@Transactional
	public void process(Long socialMediaId) {
		SocialMedia socialMedia = getSocialMedia(socialMediaId);
		socialMedia.process();
	}

	/**
	 * 장소 추출이 완료된 후 소셜 미디어의 상태를 '완료'로 변경하고, LLM 분석 결과와 검색 쿼리를 저장합니다.
	 * 이 메서드가 호출될 때, LLM으로부터 받은 장소 분석 결과(요약, 검색 쿼리)가 SocialMedia 엔티티에 저장됩니다.
	 *
	 * @param socialMediaId 완료할 소셜 미디어의 ID
	 * @param result 장소 추출 결과
	 */
	@Transactional
	public void complete(Long socialMediaId, PlaceExtractionResult result) {
		SocialMedia socialMedia = getSocialMedia(socialMediaId);

		PlaceAnalysis analysis = result.placeAnalysis();
		ContentSnapshot snapshot = result.rawContent().toSnapshot();

		socialMedia.complete(analysis.summary(), analysis.searchQueries(), snapshot);
	}

	/**
	 * 장소 추출 실패 시 소셜 미디어의 상태를 '실패'로 변경하고, 실패 코드를 기록합니다.
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
