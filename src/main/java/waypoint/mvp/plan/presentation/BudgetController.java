package waypoint.mvp.plan.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.AuthPrincipal;
import waypoint.mvp.auth.security.principal.UserPrincipal;
import waypoint.mvp.global.auth.annotations.AuthLevel;
import waypoint.mvp.global.auth.annotations.Authorize;
import waypoint.mvp.plan.application.BudgetService;
import waypoint.mvp.plan.application.dto.request.BudgetUpdateRequest;
import waypoint.mvp.plan.application.dto.response.BudgetResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/plans/{planId}/budgets")
public class BudgetController {

	private final BudgetService budgetService;

	@Authorize(level = AuthLevel.GUEST_OR_MEMBER)
	@GetMapping
	public ResponseEntity<BudgetResponse> getBudget(
		@PathVariable String planId,
		@AuthenticationPrincipal AuthPrincipal user
	) {
		BudgetResponse response = budgetService.findBudget(planId, user);
		return ResponseEntity.ok(response);
	}

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@PatchMapping
	public ResponseEntity<BudgetResponse> updateBudget(
		@PathVariable String planId,
		@RequestBody @Valid BudgetUpdateRequest request,
		@AuthenticationPrincipal UserPrincipal user
	) {
		BudgetResponse response = budgetService.updateBudget(planId, request, user);
		return ResponseEntity.ok(response);
	}
}
