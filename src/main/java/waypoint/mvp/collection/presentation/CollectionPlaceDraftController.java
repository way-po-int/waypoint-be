package waypoint.mvp.collection.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.AuthPrincipal;
import waypoint.mvp.collection.application.CollectionPlaceDraftService;
import waypoint.mvp.collection.application.dto.request.CollectionPlaceDraftCreateRequest;
import waypoint.mvp.collection.application.dto.response.ExtractionJobDetailResponse;
import waypoint.mvp.collection.application.dto.response.ExtractionJobResponse;
import waypoint.mvp.global.auth.annotations.AuthLevel;
import waypoint.mvp.global.auth.annotations.Authorize;

@RestController
@RequiredArgsConstructor
@RequestMapping("/collections/{collectionId}/drafts")
public class CollectionPlaceDraftController {

	private final CollectionPlaceDraftService draftService;

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@PostMapping
	public ResponseEntity<ExtractionJobResponse> createDraft(
		@PathVariable String collectionId,
		@RequestBody @Valid CollectionPlaceDraftCreateRequest request,
		@AuthenticationPrincipal AuthPrincipal user
	) {
		ExtractionJobResponse response = draftService.createDraft(collectionId, request, user);
		return ResponseEntity.accepted().body(response);
	}

	@GetMapping("/{jobId}")
	public ResponseEntity<ExtractionJobDetailResponse> getDraft(
		@PathVariable String collectionId,
		@PathVariable String jobId,
		@AuthenticationPrincipal AuthPrincipal user
	) {
		ExtractionJobDetailResponse response = draftService.getDraft(collectionId, jobId, user);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/latest")
	public ResponseEntity<ExtractionJobDetailResponse> getLatestDraft(
		@PathVariable String collectionId,
		@AuthenticationPrincipal AuthPrincipal user
	) {
		ExtractionJobDetailResponse response = draftService.getLatestDraft(collectionId, user);
		return ResponseEntity.ok(response);
	}
}
