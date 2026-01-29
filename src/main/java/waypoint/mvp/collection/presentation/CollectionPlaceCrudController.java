package waypoint.mvp.collection.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.AuthPrincipal;
import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.collection.application.CollectionPlaceCrudService;
import waypoint.mvp.collection.application.CollectionPlaceCrudService.CollectionPlaceSort;
import waypoint.mvp.collection.application.dto.request.CollectionPlaceCreateRequest;
import waypoint.mvp.collection.application.dto.request.CollectionPlaceUpdateRequest;
import waypoint.mvp.collection.application.dto.response.CollectionPlaceDetailResponse;
import waypoint.mvp.collection.application.dto.response.CollectionPlaceListResponse;
import waypoint.mvp.collection.application.dto.response.CollectionPlaceResponse;
import waypoint.mvp.collection.application.dto.response.PickPassResponse;
import waypoint.mvp.collection.domain.CollectionPlacePreference;

@RestController
@RequiredArgsConstructor
@RequestMapping("/collections/{collectionId}/places")
public class CollectionPlaceCrudController {

	private final CollectionPlaceCrudService collectionPlaceService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CollectionPlaceResponse addPlace(
		@PathVariable Long collectionId,
		@RequestBody @Valid CollectionPlaceCreateRequest request,
		UserPrincipal principal
	) {
		return collectionPlaceService.addPlace(collectionId, request, principal);
	}

	@GetMapping
	public CollectionPlaceListResponse getPlaces(
		@PathVariable Long collectionId,
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "LATEST") CollectionPlaceSort sort,
		@RequestParam(defaultValue = "false") boolean othersOnly,
		AuthPrincipal principal
	) {
		return collectionPlaceService.getPlaces(collectionId, page, size, sort, othersOnly, principal);
	}

	@GetMapping("/{collectionPlaceId}")
	public CollectionPlaceDetailResponse getPlaceDetail(
		@PathVariable Long collectionId,
		@PathVariable Long collectionPlaceId,
		AuthPrincipal principal
	) {
		return collectionPlaceService.getPlaceDetail(collectionId, collectionPlaceId, principal);
	}

	@PatchMapping("/{collectionPlaceId}/memo")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateMemo(
		@PathVariable Long collectionId,
		@PathVariable Long collectionPlaceId,
		@RequestBody @Valid CollectionPlaceUpdateRequest request,
		UserPrincipal principal
	) {
		collectionPlaceService.updateMemo(collectionId, collectionPlaceId, request, principal);
	}

	@DeleteMapping("/{collectionPlaceId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deletePlace(
		@PathVariable Long collectionId,
		@PathVariable Long collectionPlaceId,
		UserPrincipal principal
	) {
		collectionPlaceService.deletePlace(collectionId, collectionPlaceId, principal);
	}

	@PostMapping("/{collectionPlaceId}/preference")
	public PickPassResponse pickOrPass(
		@PathVariable Long collectionId,
		@PathVariable Long collectionPlaceId,
		@RequestParam CollectionPlacePreference.Type type,
		UserPrincipal principal
	) {
		return collectionPlaceService.pickOrPass(collectionId, collectionPlaceId, type, principal);
	}
}
