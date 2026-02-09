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
	private final PlaceService placeService;

	/**
	 * 대표 1장만 조회한다.
	 * - photoUri가 캐시되어 있으면 캐시를 반환한다.
	 * - photoName이 없거나 외부 호출이 실패하면 빈 리스트를 반환한다.
	 * - 외부 호출 성공 시 photoUri를 DB에 캐싱한다.
	 */
	public List<String> resolveRepresentativePhotoUris(Place place) {
		if (place == null) {
			return List.of();
		}

		PlaceDetail detail = place.getDetail();
		if (detail == null) {
			return List.of();
		}

		String cached = detail.getPhotoUri();
		if (StringUtils.hasText(cached)) {
			return List.of(cached);
		}

		String photoName = detail.getPhotoName();
		if (!StringUtils.hasText(photoName)) {
			return List.of();
		}

		try {
			Optional<String> fetched = googlePlacesClient.getPhotoUri(photoName)
				.filter(StringUtils::hasText);

			fetched.ifPresent(uri -> {
				detail.updatePhotoUri(uri);
				placeService.cachePhotoUri(detail.getPlaceId(), uri);
			});

			return fetched.map(List::of).orElseGet(List::of);
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
