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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.AuthPrincipal;
import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.global.auth.annotations.AuthLevel;
import waypoint.mvp.global.auth.annotations.Authorize;
import waypoint.mvp.global.common.SliceResponse;
import waypoint.mvp.plan.application.PlanService;
import waypoint.mvp.plan.application.dto.request.ChangePlanOwnerRequest;
import waypoint.mvp.plan.application.dto.request.PlanCreateRequest;
import waypoint.mvp.plan.application.dto.request.PlanUpdateRequest;
import waypoint.mvp.plan.application.dto.response.PlanResponse;
import waypoint.mvp.sharelink.application.dto.response.ShareLinkResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/plans")
public class PlanController {
	private final PlanService planService;

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@PostMapping
	public ResponseEntity<PlanResponse> createPlan(
		@RequestBody @Valid PlanCreateRequest request,
		@AuthenticationPrincipal UserPrincipal user
	) {
		PlanResponse response = planService.createPlan(request, user);

		return ResponseEntity.created(URI.create("/plans/" + response.planId()))
			.body(response);
	}

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@GetMapping
	public ResponseEntity<SliceResponse<PlanResponse>> findPlans(
		@AuthenticationPrincipal UserPrincipal user,
		Pageable pageable
	) {
		SliceResponse<PlanResponse> plans = planService.findPlans(user, pageable);
		return ResponseEntity.ok(plans);
	}

	@Authorize(level = AuthLevel.GUEST_OR_MEMBER)
	@GetMapping("/{planId}")
	public ResponseEntity<PlanResponse> findPlanById(
		@PathVariable String planId,
		@AuthenticationPrincipal AuthPrincipal user
	) {
		PlanResponse plan = planService.findPlanByExternalId(planId, user);
		return ResponseEntity.ok(plan);
	}

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@PutMapping("/{planId}")
	public ResponseEntity<PlanResponse> updatePlan(
		@PathVariable String planId,
		@RequestBody @Valid PlanUpdateRequest request,
		@AuthenticationPrincipal UserPrincipal user) {
		PlanResponse response = planService.updatePlan(planId, request, user);

		return ResponseEntity.ok(response);
	}

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@PatchMapping("/{planId}/owner")
	public ResponseEntity<Void> changeOwner(
		@PathVariable String planId,
		@RequestBody @Valid ChangePlanOwnerRequest request,
		@AuthenticationPrincipal UserPrincipal user
	) {
		planService.changeOwner(planId, request.planMemberId(), user);

		return ResponseEntity.noContent().build();
	}

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@DeleteMapping("/{planId}")
	public ResponseEntity<Void> deletePlan(
		@PathVariable String planId,
		@AuthenticationPrincipal UserPrincipal user
	) {
		planService.deletePlan(planId, user);
		return ResponseEntity.noContent().build();
	}

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@DeleteMapping("/{planId}/member/me")
	public ResponseEntity<Void> withdrawMember(
		@PathVariable String planId,
		@AuthenticationPrincipal UserPrincipal user
	) {
		planService.withdrawPlanMember(planId, user);

		return ResponseEntity.noContent().build();
	}

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@DeleteMapping("/{planId}/member/{memberId}")
	public ResponseEntity<Void> expelMember(
		@PathVariable String planId,
		@PathVariable String memberId,
		@AuthenticationPrincipal UserPrincipal user
	) {
		planService.expelPlanMember(planId, memberId, user);

		return ResponseEntity.noContent().build();
	}

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@PostMapping("/{planId}/invitations")
	public ResponseEntity<ShareLinkResponse> createInvitation(
		@PathVariable String planId,
		@AuthenticationPrincipal UserPrincipal user
	) {
		ShareLinkResponse response = planService.createInvitation(planId, user);

		return ResponseEntity.ok(response);
	}
}


