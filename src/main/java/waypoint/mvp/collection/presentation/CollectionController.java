package waypoint.mvp.collection.presentation;

import java.net.URI;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.AuthPrincipal;
import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.collection.application.CollectionService;
import waypoint.mvp.collection.application.dto.request.ChangeCollectionOwnerRequest;
import waypoint.mvp.collection.application.dto.request.CollectionCreateRequest;
import waypoint.mvp.collection.application.dto.request.CollectionUpdateRequest;
import waypoint.mvp.collection.application.dto.response.CollectionMemberResponse;
import waypoint.mvp.collection.application.dto.response.CollectionResponse;
import waypoint.mvp.global.auth.annotations.AuthLevel;
import waypoint.mvp.global.auth.annotations.Authorize;
import waypoint.mvp.global.common.SliceResponse;
import waypoint.mvp.sharelink.application.dto.response.ShareLinkResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/collections")
public class CollectionController {

	private final CollectionService collectionService;

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@PostMapping
	public ResponseEntity<CollectionResponse> createCollection(
		@RequestBody @Valid CollectionCreateRequest request,
		@AuthenticationPrincipal UserPrincipal user
	) {
		CollectionResponse response = collectionService.createCollection(request, user);

		return ResponseEntity.created(URI.create("/collections/" + response.id()))
			.body(response);
	}

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@GetMapping
	public ResponseEntity<SliceResponse<CollectionResponse>> findCollections(
		@AuthenticationPrincipal UserPrincipal user,
		Pageable pageable
	) {
		SliceResponse<CollectionResponse> collections = collectionService.findCollections(user, pageable);

		return ResponseEntity.ok(collections);
	}

	@Authorize(level = AuthLevel.GUEST_OR_MEMBER)
	@GetMapping("/{collectionId}")
	public ResponseEntity<CollectionResponse> findCollectionById(
		@PathVariable String collectionId,
		@AuthenticationPrincipal AuthPrincipal user
	) {
		CollectionResponse collection = collectionService.findCollectionByExternalId(collectionId, user);

		return ResponseEntity.ok(collection);
	}

	@Authorize(level = AuthLevel.GUEST_OR_MEMBER)
	@GetMapping("/{collectionId}/members")
	public ResponseEntity<List<CollectionMemberResponse>> getCollectionMembers(
		@PathVariable String collectionId,
		@AuthenticationPrincipal AuthPrincipal user
	) {
		List<CollectionMemberResponse> members = collectionService.getCollectionMembers(collectionId, user);

		return ResponseEntity.ok(members);
	}

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@PutMapping("/{collectionId}")
	public ResponseEntity<CollectionResponse> updateCollection(
		@PathVariable String collectionId,
		@RequestBody @Valid CollectionUpdateRequest request,
		@AuthenticationPrincipal UserPrincipal user
	) {
		CollectionResponse response = collectionService.updateCollection(collectionId, request, user);

		return ResponseEntity.ok(response);
	}

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@PatchMapping("/{collectionId}/owner")
	public ResponseEntity<Void> changeOwner(
		@PathVariable String collectionId,
		@RequestBody @Valid ChangeCollectionOwnerRequest request,
		@AuthenticationPrincipal UserPrincipal user
	) {
		collectionService.changeOwner(collectionId, request.collectionMemberId(), user);

		return ResponseEntity.noContent().build();
	}

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@DeleteMapping("/{collectionId}")
	public ResponseEntity<Void> deleteCollection(
		@PathVariable String collectionId,
		@AuthenticationPrincipal UserPrincipal user
	) {
		collectionService.deleteCollection(collectionId, user);

		return ResponseEntity.noContent().build();
	}

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@DeleteMapping("/{collectionId}/members/me")
	public ResponseEntity<Void> withdrawMember(
		@PathVariable String collectionId,
		@AuthenticationPrincipal UserPrincipal user
	) {
		collectionService.withdrawCollectionMember(collectionId, user);

		return ResponseEntity.noContent().build();
	}

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@DeleteMapping("/{collectionId}/members/{memberId}")
	public ResponseEntity<Void> expelMember(
		@PathVariable String collectionId,
		@PathVariable String memberId,
		@AuthenticationPrincipal UserPrincipal user
	) {
		collectionService.expelCollectionMember(collectionId, memberId, user);

		return ResponseEntity.noContent().build();
	}

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@PostMapping("/{collectionId}/invitations")
	public ResponseEntity<ShareLinkResponse> createInvitation(
		@PathVariable String collectionId,
		@AuthenticationPrincipal UserPrincipal user
	) {
		ShareLinkResponse response = collectionService.createInvitation(collectionId, user);

		return ResponseEntity.ok(response);
	}

}
