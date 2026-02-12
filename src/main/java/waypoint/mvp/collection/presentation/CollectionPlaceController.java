package waypoint.mvp.collection.presentation;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.AuthPrincipal;
import waypoint.mvp.collection.application.CollectionPlaceService;
import waypoint.mvp.collection.application.dto.request.CollectionPlaceCreateRequest;
import waypoint.mvp.collection.application.dto.request.CollectionPlaceUpdateRequest;
import waypoint.mvp.collection.application.dto.response.CollectionPlaceDetailResponse;
import waypoint.mvp.collection.application.dto.response.CollectionPlaceResponse;
import waypoint.mvp.collection.application.dto.response.PickPassResponse;
import waypoint.mvp.collection.domain.CollectionPlacePreference;
import waypoint.mvp.collection.domain.PlaceSortType;
import waypoint.mvp.global.auth.annotations.AuthLevel;
import waypoint.mvp.global.auth.annotations.Authorize;
import waypoint.mvp.global.common.SliceResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/collections/{collectionId}/places")
public class CollectionPlaceController {

	private final CollectionPlaceService collectionPlaceService;

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@PostMapping
	public ResponseEntity<CollectionPlaceResponse> addPlace(
		@PathVariable String collectionId,
		@RequestBody @Valid CollectionPlaceCreateRequest request,
		@AuthenticationPrincipal AuthPrincipal principal
	) {
		CollectionPlaceResponse response = collectionPlaceService.addPlace(collectionId, request, principal);

		URI location = URI.create("/collections/" + collectionId + "/places/" + response.collectionPlaceId());
		return ResponseEntity.created(location).body(response);
	}

	@Authorize(level = AuthLevel.GUEST_OR_MEMBER)
	@GetMapping
	public ResponseEntity<SliceResponse<CollectionPlaceResponse>> getPlaces(
		@PathVariable String collectionId,
		@RequestParam(required = false) String addedByMemberId,
		@RequestParam(defaultValue = "LATEST") PlaceSortType sortType,
		Pageable pageable,
		@AuthenticationPrincipal AuthPrincipal principal
	) {
		SliceResponse<CollectionPlaceResponse> response =
			collectionPlaceService.getPlaces(collectionId, addedByMemberId, sortType, pageable, principal);

		return ResponseEntity.ok(response);
	}

	@Authorize(level = AuthLevel.GUEST_OR_MEMBER)
	@GetMapping("/{collectionPlaceId}")
	public ResponseEntity<CollectionPlaceDetailResponse> getPlaceDetail(
		@PathVariable String collectionId,
		@PathVariable String collectionPlaceId,
		@AuthenticationPrincipal AuthPrincipal principal
	) {
		CollectionPlaceDetailResponse response =
			collectionPlaceService.getPlaceDetail(collectionId, collectionPlaceId, principal);

		return ResponseEntity.ok(response);
	}

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@PatchMapping("/{collectionPlaceId}/memo")
	public ResponseEntity<Void> updateMemo(
		@PathVariable String collectionId,
		@PathVariable String collectionPlaceId,
		@RequestBody @Valid CollectionPlaceUpdateRequest request,
		@AuthenticationPrincipal AuthPrincipal principal
	) {
		collectionPlaceService.updateMemo(collectionId, collectionPlaceId, request, principal);
		return ResponseEntity.noContent().build();
	}

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@DeleteMapping("/{collectionPlaceId}")
	public ResponseEntity<Void> deletePlace(
		@PathVariable String collectionId,
		@PathVariable String collectionPlaceId,
		@AuthenticationPrincipal AuthPrincipal principal
	) {
		collectionPlaceService.deletePlace(collectionId, collectionPlaceId, principal);
		return ResponseEntity.noContent().build();
	}

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@PostMapping("/{collectionPlaceId}/preference")
	public ResponseEntity<PickPassResponse> pickOrPass(
		@PathVariable String collectionId,
		@PathVariable String collectionPlaceId,
		@RequestParam CollectionPlacePreference.Type type,
		@AuthenticationPrincipal AuthPrincipal principal
	) {
		PickPassResponse response = collectionPlaceService.pickOrPass(collectionId, collectionPlaceId, type, principal);
		return ResponseEntity.ok(response);
	}
}
