package waypoint.mvp.place.application;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import waypoint.mvp.place.domain.Place;
import waypoint.mvp.place.domain.PlaceDetail;
import waypoint.mvp.place.infrastructure.google.GooglePlacesClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlacePhotoService {

	private final GooglePlacesClient googlePlacesClient;

	/**
	 * 대표 1장만 조회한다.
	 * - photoName이 없거나 외부 호출이 실패하면 빈 리스트를 반환한다.
	 */
	public List<String> resolveRepresentativePhotoUris(Place place) {
		if (place == null) {
			return List.of();
		}

		PlaceDetail detail = place.getDetail();
		String photoName = (detail != null) ? detail.getPhotoName() : null;

		if (!StringUtils.hasText(photoName)) {
			return List.of();
		}

		try {
			Optional<String> photoUriOpt = googlePlacesClient.getPhotoUri(photoName);

			return photoUriOpt
				.filter(StringUtils::hasText)
				.map(List::of)
				.orElseGet(List::of);
		} catch (Exception e) {
			log.warn(
				"Failed to fetch photoUri. placeExternalId={}, googlePlaceId={}, photoName={}",
				place.getExternalId(),
				detail.getPlaceId(),
				photoName,
				e
			);
			return List.of();
		}
	}
}
