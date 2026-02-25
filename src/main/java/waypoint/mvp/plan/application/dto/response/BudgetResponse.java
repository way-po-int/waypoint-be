package waypoint.mvp.plan.application.dto.response;

import waypoint.mvp.plan.domain.Budget;
import waypoint.mvp.plan.domain.BudgetType;

public record BudgetResponse(
	String budgetId,
	BudgetType type,
	Long totalBudget,
	Long totalCost,
	Long remainingBudget,
	Long costPerPerson,
	Integer travelerCount
) {
	public static BudgetResponse of(Budget budget, long totalCost) {
		Long remainingBudget = budget.getTotalBudget() - totalCost;
		return new BudgetResponse(
			budget.getExternalId(),
			budget.getType(),
			budget.getTotalBudget(),
			totalCost,
			remainingBudget,
			budget.getCostPerPerson(totalCost),
			budget.getTravelerCount()
		);
	}
}
