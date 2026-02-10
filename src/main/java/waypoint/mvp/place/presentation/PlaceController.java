package waypoint.mvp.place.presentation;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.global.auth.annotations.AuthLevel;
import waypoint.mvp.global.auth.annotations.Authorize;
import waypoint.mvp.place.application.PlaceSearchService;
import waypoint.mvp.place.application.dto.PlaceResponse;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/places")
public class PlaceController {

	private final PlaceSearchService placeSearchService;

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@GetMapping("/search")
	public ResponseEntity<List<PlaceResponse>> search(
		@RequestParam
		@Size(max = 50)
		String query
	) {
		return ResponseEntity.ok(placeSearchService.search(query));
	}
}
