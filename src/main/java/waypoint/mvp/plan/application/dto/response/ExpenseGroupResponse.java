package waypoint.mvp.plan.application.dto.response;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import waypoint.mvp.plan.domain.BlockStatus;
import waypoint.mvp.plan.domain.Expense;
import waypoint.mvp.plan.domain.ExpenseItem;
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
	public static ExpenseGroupResponse ofAdditional(Expense expense, List<ExpenseItem> items) {
		ExpenseResponse selected = ExpenseResponse.of(expense, items);
		return new ExpenseGroupResponse(null, ExpenseType.ADDITIONAL, null, null, null, selected);
	}

	public static ExpenseGroupResponse ofBlock(
		String timeBlockId,
		Expense selected,
		List<Expense> candidates,
		Map<Long, List<ExpenseItem>> itemMap
	) {
		ExpenseResponse selectedResponse = selected != null
			? ExpenseResponse.of(selected, itemMap.getOrDefault(selected.getId(), List.of()))
			: null;
		List<ExpenseResponse> candidateResponses = candidates.stream()
			.map(e -> ExpenseResponse.of(e, itemMap.getOrDefault(e.getId(), List.of())))
			.toList();

		return new ExpenseGroupResponse(
			timeBlockId,
			ExpenseType.BLOCK,
			determineBlockStatus(selected, candidates),
			candidates.size(),
			candidateResponses,
			selectedResponse
		);
	}

	private static BlockStatus determineBlockStatus(Expense selected, List<Expense> candidates) {
		if (selected == null) {
			return BlockStatus.PENDING;
		}
		if (candidates.isEmpty()) {
			return BlockStatus.DIRECT;
		}
		return BlockStatus.FIXED;
	}
}
