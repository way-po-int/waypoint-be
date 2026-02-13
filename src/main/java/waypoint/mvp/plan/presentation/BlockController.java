package waypoint.mvp.plan.presentation;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.global.auth.annotations.AuthLevel;
import waypoint.mvp.global.auth.annotations.Authorize;
import waypoint.mvp.plan.application.BlockService;
import waypoint.mvp.plan.application.dto.request.BlockCreateRequest;
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
}
