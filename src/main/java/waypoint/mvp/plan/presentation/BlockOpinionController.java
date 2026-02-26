package waypoint.mvp.plan.presentation;

import java.net.URI;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
import waypoint.mvp.global.common.sort.SortType;
import waypoint.mvp.plan.application.BlockOpinionService;
import waypoint.mvp.plan.application.dto.request.BlockOpinionCreateRequest;
import waypoint.mvp.plan.application.dto.request.BlockOpinionUpdateRequest;
import waypoint.mvp.plan.application.dto.response.BlockOpinionResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/plans/{planId}/blocks/{blockId}/opinions")
public class BlockOpinionController {

	private final BlockOpinionService blockOpinionService;

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@PostMapping
	public ResponseEntity<BlockOpinionResponse> createOpinion(
		@PathVariable String planId,
		@PathVariable String blockId,
		@RequestBody @Valid BlockOpinionCreateRequest request,
		@AuthenticationPrincipal UserPrincipal user
	) {
		BlockOpinionResponse response = blockOpinionService.createOpinion(planId, blockId, request, user);

		URI location = URI.create("/plans/" + planId + "/blocks/" + blockId + "/opinions/" + response.opinionId());
		return ResponseEntity.created(location).body(response);
	}

	@Authorize(level = AuthLevel.GUEST_OR_MEMBER)
	@GetMapping
	public ResponseEntity<List<BlockOpinionResponse>> findOpinions(
		@PathVariable String planId,
		@PathVariable String blockId,
		@AuthenticationPrincipal AuthPrincipal user,
		@RequestParam(defaultValue = "CREATED_AT_DESC") SortType sortType,
		Pageable pageable
	) {
		Pageable sortedPageable = sortType.toPageable(pageable);
		List<BlockOpinionResponse> responses = blockOpinionService.findOpinions(planId, blockId, user, sortedPageable);
		return ResponseEntity.ok(responses);
	}

	@Authorize(level = AuthLevel.GUEST_OR_MEMBER)
	@GetMapping("/{opinionId}")
	public ResponseEntity<BlockOpinionResponse> findOpinion(
		@PathVariable String planId,
		@PathVariable String blockId,
		@PathVariable String opinionId,
		@AuthenticationPrincipal AuthPrincipal user
	) {
		BlockOpinionResponse response = blockOpinionService.findOpinion(planId, blockId, opinionId, user);
		return ResponseEntity.ok(response);
	}

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@PutMapping("/{opinionId}")
	public ResponseEntity<BlockOpinionResponse> updateOpinion(
		@PathVariable String planId,
		@PathVariable String blockId,
		@PathVariable String opinionId,
		@RequestBody @Valid BlockOpinionUpdateRequest request,
		@AuthenticationPrincipal UserPrincipal user
	) {
		BlockOpinionResponse response = blockOpinionService.updateOpinion(planId, blockId, opinionId, request, user);
		return ResponseEntity.ok(response);
	}

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@DeleteMapping("/{opinionId}")
	public ResponseEntity<Void> deleteOpinion(
		@PathVariable String planId,
		@PathVariable String blockId,
		@PathVariable String opinionId,
		@AuthenticationPrincipal UserPrincipal user
	) {
		blockOpinionService.deleteOpinion(planId, blockId, opinionId, user);
		return ResponseEntity.noContent().build();
	}
}
