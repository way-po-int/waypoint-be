package waypoint.mvp.plan.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import waypoint.mvp.auth.security.principal.AuthPrincipal;
import waypoint.mvp.global.auth.ResourceAuthorizer;
import waypoint.mvp.plan.application.dto.response.BudgetResponse;
import waypoint.mvp.plan.domain.Budget;
import waypoint.mvp.plan.domain.Plan;
import waypoint.mvp.plan.infrastructure.persistence.BudgetRepository;
import waypoint.mvp.plan.infrastructure.persistence.ExpenseItemRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BudgetService {

	private final BudgetQueryService budgetQueryService;
	private final ResourceAuthorizer planAuthorizer;

	private final BudgetRepository budgetRepository;
	private final ExpenseItemRepository expenseItemRepository;

	@Transactional
	public void createBudget(Plan plan) {
		Budget budget = Budget.create(plan);
		budgetRepository.save(budget);
	}

	public BudgetResponse findBudget(String planExternalId, AuthPrincipal user) {
		Budget budget = budgetQueryService.getBudget(planExternalId);
		planAuthorizer.verifyAccess(user, budget.getPlan().getId());

		long totalCost = getTotalCost(budget.getId());
		return BudgetResponse.of(budget, totalCost);
	}

	private Long getTotalCost(Long budgetId) {
		long fixedCost = expenseItemRepository.calculateFixedCost(budgetId);
		long unfixedCost = expenseItemRepository.calculateUnfixedCost(budgetId);
		return fixedCost + unfixedCost;
	}
}
