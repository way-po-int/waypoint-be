package waypoint.mvp.plan.application.dto.response;

import waypoint.mvp.plan.domain.ExpenseItem;

public record ExpenseItemResponse(
	String expenseItemId,
	String name,
	Long cost
) {
	public static ExpenseItemResponse from(ExpenseItem item) {
		return new ExpenseItemResponse(item.getExternalId(), item.getName(), item.getCost());
	}
}
