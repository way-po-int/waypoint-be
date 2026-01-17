package waypoint.mvp.collection.presentation;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.UserInfo;
import waypoint.mvp.collection.application.CollectionService;
import waypoint.mvp.collection.application.dto.request.CollectionCreateRequest;
import waypoint.mvp.collection.application.dto.request.CollectionUpdateRequest;
import waypoint.mvp.collection.application.dto.response.CollectionResponse;
import waypoint.mvp.sharelink.application.dto.response.ShareLinkResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/collections")
public class CollectionController {

	private final CollectionService collectionService;

	@PostMapping
	public ResponseEntity<CollectionResponse> createCollection(@RequestBody @Valid CollectionCreateRequest request,
		@AuthenticationPrincipal UserInfo userInfo) {
		CollectionResponse response = collectionService.createCollection(request, userInfo);
		return ResponseEntity.created(URI.create("/collections/" + response.id()))
			.body(response);
	}

	@GetMapping
	public ResponseEntity<Page<CollectionResponse>> findCollections(Pageable pageable) {
		Page<CollectionResponse> collections = collectionService.findCollections(pageable);
		return ResponseEntity.ok(collections);
	}

	@GetMapping("/{collectionId}")
	public ResponseEntity<CollectionResponse> findCollectionById(
		@PathVariable Long collectionId,
		@AuthenticationPrincipal UserInfo userInfo,
		@CookieValue(name = "waypoint-guest", required = false) String guestToken
	) {
		CollectionResponse collection = collectionService.findCollectionById(collectionId, userInfo, guestToken);
		return ResponseEntity.ok(collection);
	}

	@PutMapping("/{collectionId}")
	public ResponseEntity<CollectionResponse> updateCollection(@PathVariable Long collectionId,
		@RequestBody @Valid CollectionUpdateRequest request) {
		CollectionResponse response = collectionService.updateCollection(collectionId, request);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{collectionId}")
	public ResponseEntity<Void> deleteCollection(@PathVariable Long collectionId) {
		collectionService.deleteCollection(collectionId);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{collectionId}/invitations")
	public ResponseEntity<ShareLinkResponse> createInvitation(
		@PathVariable Long collectionId,
		@AuthenticationPrincipal UserInfo userInfo
	) {
		ShareLinkResponse response = collectionService.createInvitation(collectionId, userInfo.id());
		return ResponseEntity.ok(response);
	}

}
