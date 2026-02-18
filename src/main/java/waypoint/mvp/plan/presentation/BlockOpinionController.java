package waypoint.mvp.plan.presentation;

import java.net.URI;
import java.util.List;

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
import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.global.auth.annotations.AuthLevel;
import waypoint.mvp.global.auth.annotations.Authorize;
import waypoint.mvp.plan.application.BlockOpinionService;
import waypoint.mvp.plan.application.dto.request.BlockOpinionCreateRequest;
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
		@AuthenticationPrincipal AuthPrincipal user
	) {
		List<BlockOpinionResponse> responses = blockOpinionService.findOpinions(planId, blockId, user);
		return ResponseEntity.ok(responses);
	}
}
