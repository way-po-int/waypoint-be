package waypoint.mvp.plan.application.dto.response;

import waypoint.mvp.plan.domain.Budget;

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
			BudgetType.from(budget),
			budget.getTotalBudget(),
			totalCost,
			remainingBudget,
			budget.getCostPerPerson(totalCost),
			budget.getTravelerCount()
		);
	}

	enum BudgetType {
		INITIAL,
		BUDGET,
		EXPENSE;

		public static BudgetType from(Budget budget) {
			if (budget.isInitial()) {
				return INITIAL;
			}
			return switch (budget.getType()) {
				case BUDGET -> BUDGET;
				case EXPENSE -> EXPENSE;
			};
		}
	}
}
