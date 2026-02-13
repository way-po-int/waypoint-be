package waypoint.mvp.collection.presentation;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.AuthPrincipal;
import waypoint.mvp.collection.application.PlaceExtractionJobService;
import waypoint.mvp.collection.application.dto.request.AddExtractedPlacesRequest;
import waypoint.mvp.collection.application.dto.request.PlaceExtractionJobCreateRequest;
import waypoint.mvp.collection.application.dto.response.CollectionPlaceResponse;
import waypoint.mvp.collection.application.dto.response.ExtractionJobDetailResponse;
import waypoint.mvp.collection.application.dto.response.ExtractionJobResponse;
import waypoint.mvp.global.auth.annotations.AuthLevel;
import waypoint.mvp.global.auth.annotations.Authorize;

@RestController
@RequiredArgsConstructor
@RequestMapping("/collections/{collectionId}/extraction-jobs")
public class PlaceExtractionJobController {

	private final PlaceExtractionJobService extractionJobService;

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@PostMapping
	public ResponseEntity<ExtractionJobResponse> createExtractionJob(
		@PathVariable String collectionId,
		@RequestBody @Valid PlaceExtractionJobCreateRequest request,
		@AuthenticationPrincipal AuthPrincipal user
	) {
		ExtractionJobResponse response = extractionJobService.createExtractionJob(collectionId, request, user);
		return ResponseEntity.accepted().body(response);
	}

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@GetMapping("/{jobId}")
	public ResponseEntity<ExtractionJobDetailResponse> getExtractionJob(
		@PathVariable String collectionId,
		@PathVariable String jobId,
		@AuthenticationPrincipal AuthPrincipal user
	) {
		ExtractionJobDetailResponse response = extractionJobService.getExtractionJob(collectionId, jobId, user);
		return ResponseEntity.ok(response);
	}

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@GetMapping("/latest")
	public ResponseEntity<ExtractionJobDetailResponse> getLatestExtractionJob(
		@PathVariable String collectionId,
		@AuthenticationPrincipal AuthPrincipal user
	) {
		ExtractionJobDetailResponse response = extractionJobService.getLatestExtractionJob(collectionId, user);
		return ResponseEntity.ok(response);
	}

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@PostMapping("/{jobId}/places")
	public ResponseEntity<List<CollectionPlaceResponse>> addExtractedPlaces(
		@PathVariable String collectionId,
		@PathVariable String jobId,
		@RequestBody @Valid AddExtractedPlacesRequest request,
		@AuthenticationPrincipal AuthPrincipal user
	) {
		List<CollectionPlaceResponse> responses =
			extractionJobService.addExtractedPlaces(collectionId, jobId, request, user);
		return ResponseEntity.ok(responses);
	}

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@DeleteMapping("/{jobId}")
	public ResponseEntity<Void> ignoreExtractionJob(
		@PathVariable String collectionId,
		@PathVariable String jobId,
		@AuthenticationPrincipal AuthPrincipal user
	) {
		extractionJobService.ignoreExtractionJob(collectionId, jobId, user);
		return ResponseEntity.noContent().build();
	}
}
