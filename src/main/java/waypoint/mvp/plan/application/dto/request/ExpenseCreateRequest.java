package waypoint.mvp.plan.application.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ExpenseCreateRequest(
	@NotBlank(message = "이전 지출 항목 Id는 필수 입니다.")
	String prevExpenseId,

	@NotEmpty(message = "지출 항목은 최소 1개 이상이어야 합니다.")
	@Valid
	List<ItemRequest> items
) {
	public record ItemRequest(
		@NotBlank(message = "항목 이름은 비어있을 수 없습니다.")
		String name,

		@NotNull(message = "금액은 필수입니다.")
		@Min(value = 0, message = "금액은 0 이상이어야 합니다.")
		Long cost
	) {
	}
}
