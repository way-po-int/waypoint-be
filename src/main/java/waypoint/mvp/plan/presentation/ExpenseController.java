package waypoint.mvp.plan.presentation;

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
import waypoint.mvp.plan.application.ExpenseService;
import waypoint.mvp.plan.application.dto.request.ExpenseCreateRequest;
import waypoint.mvp.plan.application.dto.response.ExpenseGroupResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/plans/{planId}/expenses")
public class ExpenseController {

	private final ExpenseService expenseService;

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@PostMapping
	public ResponseEntity<ExpenseGroupResponse> createAdditionalExpense(
		@PathVariable String planId,
		@RequestBody @Valid ExpenseCreateRequest request,
		@AuthenticationPrincipal UserPrincipal user
	) {
		ExpenseGroupResponse response = expenseService.createAdditionalExpense(planId, request, user);
		return ResponseEntity.ok(response);
	}
}
