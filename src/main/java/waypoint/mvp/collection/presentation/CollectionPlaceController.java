package waypoint.mvp.collection.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.UserInfo;
import waypoint.mvp.collection.application.CollectionPlaceService;
import waypoint.mvp.collection.application.dto.request.CollectionPlaceFromUrlRequest;
import waypoint.mvp.collection.application.dto.response.ExtractionJobResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/collections/{collectionId}/places")
public class CollectionPlaceController {

	private final CollectionPlaceService collectionPlaceService;

	@PostMapping("/from-url")
	public ResponseEntity<ExtractionJobResponse> fromUrl(
		@PathVariable Long collectionId,
		@Valid @RequestBody CollectionPlaceFromUrlRequest request,
		@AuthenticationPrincipal UserInfo userInfo
	) {
		ExtractionJobResponse response = collectionPlaceService.addPlacesFromUrl(collectionId, request, userInfo);
		return ResponseEntity
			.accepted()
			.body(response);
	}
}
