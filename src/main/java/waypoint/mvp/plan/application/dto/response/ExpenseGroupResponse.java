package waypoint.mvp.plan.application.dto.response;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonInclude;

import waypoint.mvp.place.application.dto.PlaceCategoryResponse;
import waypoint.mvp.place.domain.Place;
import waypoint.mvp.plan.domain.Block;
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
		ExpenseResponse selected = ExpenseResponse.of(expense, items, null);
		return new ExpenseGroupResponse(null, ExpenseType.ADDITIONAL, null, null, null, selected);
	}

	public static ExpenseGroupResponse ofBlock(
		String timeBlockId,
		Expense selected,
		List<Expense> candidates,
		Map<Long, List<ExpenseItem>> itemMap,
		Function<Long, PlaceCategoryResponse> categoryProvider
	) {
		ExpenseResponse selectedResponse = mapToResponse(selected, itemMap, categoryProvider);
		List<ExpenseResponse> candidateResponses = candidates.stream()
			.map(e -> mapToResponse(e, itemMap, categoryProvider))
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

	private static ExpenseResponse mapToResponse(
		Expense expense,
		Map<Long, List<ExpenseItem>> itemMap,
		Function<Long, PlaceCategoryResponse> categoryProvider
	) {
		if (expense == null) {
			return null;
		}

		PlaceCategoryResponse category = Optional.ofNullable(expense.getBlock())
			.map(Block::getPlace)
			.map(Place::getCategoryId)
			.map(categoryProvider)
			.orElse(null);

		return ExpenseResponse.of(
			expense,
			itemMap.getOrDefault(expense.getId(), List.of()),
			category
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
