package waypoint.mvp.plan.application.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import waypoint.mvp.plan.domain.BlockStatus;
import waypoint.mvp.plan.domain.ExpenseType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExpenseGroupResponse(
	String timeBlockId,
	ExpenseType type,
	BlockStatus blockStatus,
	Integer candidateCount,
	List<ExpenseResponse> candidates,
	ExpenseResponse selected
) {
	public static ExpenseGroupResponse ofAdditional(ExpenseResponse selected) {
		return new ExpenseGroupResponse(null, ExpenseType.ADDITIONAL, null, null, null, selected);
	}
}
