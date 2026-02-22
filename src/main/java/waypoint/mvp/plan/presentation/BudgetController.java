package waypoint.mvp.plan.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.AuthPrincipal;
import waypoint.mvp.global.auth.annotations.AuthLevel;
import waypoint.mvp.global.auth.annotations.Authorize;
import waypoint.mvp.plan.application.BudgetService;
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
}
