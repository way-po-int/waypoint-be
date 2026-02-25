package waypoint.mvp.plan.application.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public record CandidateExpenseResponse(
	@JsonUnwrapped
	CandidateBlockResponse candidate,

	List<ExpenseItemResponse> expenseItems
) {
	public static CandidateExpenseResponse of(
		CandidateBlockResponse candidate,
		List<ExpenseItemResponse> expenseItems
	) {
		return new CandidateExpenseResponse(candidate, expenseItems);
	}
}
