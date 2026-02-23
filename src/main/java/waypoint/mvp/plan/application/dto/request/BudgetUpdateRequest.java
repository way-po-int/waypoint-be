package waypoint.mvp.plan.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import waypoint.mvp.plan.domain.BudgetType;

public record BudgetUpdateRequest(
	@NotNull(message = "예산 타입은 필수입니다.")
	BudgetType type,

	@Min(value = 0, message = "총 예산은 0 이상이어야 합니다.")
	Long totalBudget,

	@Min(value = 1, message = "여행 인원은 1 이상이어야 합니다.")
	Integer travelerCount
) {
}
