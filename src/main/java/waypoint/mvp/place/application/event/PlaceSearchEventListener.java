package waypoint.mvp.place.application.event;

import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import waypoint.mvp.place.application.PlacesLookupService;
import waypoint.mvp.place.application.SocialMediaService;
import waypoint.mvp.place.application.dto.PlaceIdLookupCommand;
import waypoint.mvp.place.domain.SocialMedia;
import waypoint.mvp.place.domain.event.PlaceSearchRequestedEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlaceSearchEventListener {

	private final SocialMediaService socialMediaService;
	private final PlacesLookupService placesLookupService;

	@Async("placeSearchTaskExecutor")
	@EventListener
	public void handlePlaceSearchRequestedEvent(PlaceSearchRequestedEvent event) {
		Long socialMediaId = event.socialMediaId();

		try {
			log.info("장소 검색 이벤트 수신: socialMediaId={}", socialMediaId);

			SocialMedia socialMedia = socialMediaService.getSocialMedia(socialMediaId);
			List<String> searchQueries = socialMedia.getSearchQueries();

			PlaceIdLookupCommand command = new PlaceIdLookupCommand(searchQueries);
			placesLookupService.lookupAndSavePlaces(command);

		} catch (Exception e) {
			log.error("장소 검색 이벤트 실패: socialMediaId={}", socialMediaId);
		}
	}
}
