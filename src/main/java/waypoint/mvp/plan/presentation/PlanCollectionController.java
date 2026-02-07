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
import waypoint.mvp.plan.application.PlanCollectionService;
import waypoint.mvp.plan.application.dto.request.CreatePlanCollectionRequest;
import waypoint.mvp.plan.application.dto.response.PlanCollectionResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/plans/{planId}/collections")
public class PlanCollectionController {
	private final PlanCollectionService planCollectionService;

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@PostMapping
	public ResponseEntity<PlanCollectionResponse> connectCollection(
		@PathVariable String planId,
		@RequestBody @Valid CreatePlanCollectionRequest request,
		@AuthenticationPrincipal UserPrincipal user
	) {
		PlanCollectionResponse response = planCollectionService.createPlanCollection(planId, request, user);

		URI location = URI.create("/plans/" + planId + "/collections");
		return ResponseEntity.created(location).body(response);
	}

	@Authorize(level = AuthLevel.GUEST_OR_MEMBER)
	@GetMapping
	public ResponseEntity<List<PlanCollectionResponse>> findConnectedCollections(
		@PathVariable String planId,
		@AuthenticationPrincipal AuthPrincipal user
	) {
		List<PlanCollectionResponse> response = planCollectionService.findPlanCollectionResponses(planId, user);

		return ResponseEntity.ok(response);
	}

}
