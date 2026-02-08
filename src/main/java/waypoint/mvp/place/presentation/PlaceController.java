package waypoint.mvp.place.presentation;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.auth.annotations.AuthLevel;
import waypoint.mvp.global.auth.annotations.Authorize;
import waypoint.mvp.place.application.PlacePhotoService;
import waypoint.mvp.place.application.PlaceSearchService;
import waypoint.mvp.place.application.PlaceService;
import waypoint.mvp.place.application.dto.PlaceResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/places")
public class PlaceController {

	private final PlaceSearchService placeSearchService;
	private final PlaceService placeService;
	private final PlacePhotoService placePhotoService;

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@GetMapping("/search")
	public ResponseEntity<List<PlaceResponse>> search(@RequestParam String query) {
		if (!StringUtils.hasText(query)) {
			return ResponseEntity.ok(List.of());
		}

		String q = query.trim();

		return placeSearchService.searchTop1PlaceId(q)
			.flatMap(placeId -> placeService.getPlace(placeId)
				.or(() -> placeSearchService.fetchPlaceDetails(placeId).map(placeService::createOrGetPlace)))
			.map(place -> PlaceResponse.from(place, placePhotoService.resolveRepresentativePhotoUris(place)))
			.map(resp -> ResponseEntity.ok(List.of(resp)))
			.orElseGet(() -> ResponseEntity.ok(List.of()));
	}
}
