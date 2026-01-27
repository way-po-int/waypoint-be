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

	@Transactional
	public void startAnalysis(Long socialMediaId) {
		SocialMedia socialMedia = getSocialMedia(socialMediaId);
		socialMedia.startAnalysis();
	}

	@Transactional
	public void completeAnalysis(Long socialMediaId, PlaceExtractionResult result) {
		SocialMedia socialMedia = getSocialMedia(socialMediaId);

		PlaceAnalysis analysis = result.placeAnalysis();
		ContentSnapshot snapshot = result.rawContent().toSnapshot();

		socialMedia.completeAnalysis(analysis.summary(), analysis.searchQueries(), snapshot);
	}

	@Transactional
	public void failAnalysis(Long socialMediaId, ExtractFailureCode failureCode) {
		SocialMedia socialMedia = getSocialMedia(socialMediaId);
		socialMedia.failAnalysis(failureCode);
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
