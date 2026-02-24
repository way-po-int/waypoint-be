package waypoint.mvp.plan.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ExpenseItemUpdateRequest(
	String expenseItemId,

	@NotBlank(message = "항목 이름은 비어있을 수 없습니다.")
	String name,

	@NotNull(message = "금액은 필수입니다.")
	@Min(value = 0, message = "금액은 0 이상이어야 합니다.")
	Long cost
) {
}
