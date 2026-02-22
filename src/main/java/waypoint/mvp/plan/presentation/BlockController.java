package waypoint.mvp.plan.presentation;

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
import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.global.auth.annotations.AuthLevel;
import waypoint.mvp.global.auth.annotations.Authorize;
import waypoint.mvp.plan.application.BlockService;
import waypoint.mvp.plan.application.dto.request.BlockCreateByPlaceRequest;
import waypoint.mvp.plan.application.dto.request.BlockCreateRequest;
import waypoint.mvp.plan.application.dto.request.BlockUpdateRequest;
import waypoint.mvp.plan.application.dto.request.CandidateBlockCreateRequest;
import waypoint.mvp.plan.application.dto.request.CandidateBlockSelectRequest;
import waypoint.mvp.plan.application.dto.response.BlockDetailResponse;
import waypoint.mvp.plan.application.dto.response.BlockListResponse;
import waypoint.mvp.plan.application.dto.response.BlockResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/plans/{planId}/blocks")
public class BlockController {
	private final BlockService blockService;

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@PostMapping
	public ResponseEntity<BlockResponse> createBlock(
		@PathVariable String planId,
		@RequestBody @Valid BlockCreateRequest request,
		@AuthenticationPrincipal UserPrincipal user
	) {
		BlockResponse response = blockService.createBlock(planId, request, user);

		URI location = URI.create("/plans/" + planId + "/blocks/" + response.timeBlockId());
		return ResponseEntity.created(location).body(response);
	}

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@PostMapping("/by-place")
	public ResponseEntity<BlockResponse> createBlockByPlace(
		@PathVariable String planId,
		@RequestBody @Valid BlockCreateByPlaceRequest request,
		@AuthenticationPrincipal UserPrincipal user
	) {
		BlockResponse response = blockService.createBlock(planId, request, user);

		URI location = URI.create("/plans/" + planId + "/blocks/" + response.timeBlockId());
		return ResponseEntity.created(location).body(response);
	}

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@PostMapping("/{timeBlockId}/candidates")
	public ResponseEntity<BlockResponse> addCandidates(
		@PathVariable String planId,
		@PathVariable String timeBlockId,
		@RequestBody @Valid CandidateBlockCreateRequest request,
		@AuthenticationPrincipal UserPrincipal user
	) {
		BlockResponse response = blockService.addCandidates(planId, timeBlockId, request, user);
		return ResponseEntity.ok(response);
	}

	@Authorize(level = AuthLevel.GUEST_OR_MEMBER)
	@GetMapping
	public ResponseEntity<BlockListResponse> getBlocks(
		@PathVariable String planId,
		@RequestParam int day,
		@AuthenticationPrincipal AuthPrincipal user,
		Pageable pageable
	) {
		BlockListResponse response = blockService.findBlocks(planId, day, user, pageable);
		return ResponseEntity.ok(response);
	}

	@Authorize(level = AuthLevel.GUEST_OR_MEMBER)
	@GetMapping("/{blockId}")
	public ResponseEntity<BlockDetailResponse> getBlockDetail(
		@PathVariable String planId,
		@PathVariable String blockId,
		@AuthenticationPrincipal AuthPrincipal user
	) {
		BlockDetailResponse response = blockService.findBlockDetail(planId, blockId, user);
		return ResponseEntity.ok(response);
	}

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@PatchMapping("/{blockId}")
	public ResponseEntity<BlockDetailResponse> updateBlock(
		@PathVariable String planId,
		@PathVariable String blockId,
		@RequestBody @Valid BlockUpdateRequest request,
		@AuthenticationPrincipal UserPrincipal user
	) {
		BlockDetailResponse response = blockService.updateBlock(planId, blockId, request, user);
		return ResponseEntity.ok(response);
	}

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@PatchMapping("/{timeBlockId}/fix")
	public ResponseEntity<BlockResponse> fixCandidateBlock(
		@PathVariable String planId,
		@PathVariable String timeBlockId,
		@RequestBody @Valid CandidateBlockSelectRequest request,
		@AuthenticationPrincipal UserPrincipal user
	) {
		BlockResponse response = blockService.fixCandidate(planId, timeBlockId, request, user);
		return ResponseEntity.ok(response);
	}

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@PatchMapping("/{timeBlockId}/unfix")
	public ResponseEntity<BlockResponse> unfixCandidateBlock(
		@PathVariable String planId,
		@PathVariable String timeBlockId,
		@RequestBody @Valid CandidateBlockSelectRequest request,
		@AuthenticationPrincipal UserPrincipal user
	) {
		BlockResponse response = blockService.unfixCandidate(planId, timeBlockId, request, user);
		return ResponseEntity.ok(response);
	}

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@DeleteMapping("/{timeBlockId}")
	public ResponseEntity<Void> deleteTimeBlock(
		@PathVariable String planId,
		@PathVariable String timeBlockId,
		@AuthenticationPrincipal UserPrincipal user
	) {
		blockService.deleteTimeBlock(planId, timeBlockId, user);

		return ResponseEntity.noContent().build();
	}
}
