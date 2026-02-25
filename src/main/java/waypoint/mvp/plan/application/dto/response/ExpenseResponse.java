package waypoint.mvp.plan.application.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import waypoint.mvp.place.application.dto.PlaceCategoryResponse;
import waypoint.mvp.plan.domain.Block;
import waypoint.mvp.plan.domain.Expense;
import waypoint.mvp.plan.domain.ExpenseItem;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExpenseResponse(
	String expenseId,
	BlockInfo block,
	List<ItemInfo> items
) {
	public static ExpenseResponse of(Expense expense, List<ExpenseItem> items, PlaceCategoryResponse category) {
		return new ExpenseResponse(
			expense.getExternalId(),
			BlockInfo.from(expense.getBlock(), category),
			items != null ? items.stream().map(ItemInfo::from).toList() : List.of()
		);
	}

	record BlockInfo(String blockId, String name, PlaceCategoryResponse category) {
		static BlockInfo from(Block block, PlaceCategoryResponse category) {
			if (block == null) {
				return null;
			}
			return new BlockInfo(block.getExternalId(), block.getPlace().getName(), category);
		}
	}

	record ItemInfo(String itemId, String name, Long cost) {
		static ItemInfo from(ExpenseItem item) {
			return new ItemInfo(item.getExternalId(), item.getName(), item.getCost());
		}
	}
}
