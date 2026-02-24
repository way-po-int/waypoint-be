package waypoint.mvp.plan.presentation;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
import waypoint.mvp.plan.application.dto.request.ExpenseItemUpdateRequest;
import waypoint.mvp.plan.application.dto.response.ExpenseGroupResponse;
import waypoint.mvp.plan.application.dto.response.ExpenseResponse;

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

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@PutMapping("/{expenseId}")
	public ResponseEntity<ExpenseResponse> updateExpenseItems(
		@PathVariable String planId,
		@PathVariable String expenseId,
		@RequestBody @Valid List<ExpenseItemUpdateRequest> requests,
		@AuthenticationPrincipal UserPrincipal user
	) {
		ExpenseResponse response = expenseService.updateExpenseItems(planId, expenseId, requests, user);
		return ResponseEntity.ok(response);
	}

	@Authorize(level = AuthLevel.AUTHENTICATED)
	@DeleteMapping("/{expenseId}")
	public ResponseEntity<Void> deleteAdditionalExpense(
		@PathVariable String planId,
		@PathVariable String expenseId,
		@AuthenticationPrincipal UserPrincipal user
	) {
		expenseService.deleteExpense(planId, expenseId, user);
		return ResponseEntity.noContent().build();
	}
}
