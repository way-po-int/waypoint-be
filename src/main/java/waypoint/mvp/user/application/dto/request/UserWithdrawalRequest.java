package waypoint.mvp.user.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserWithdrawalRequest(
	@NotBlank
	@Size(max = 500)
	String reason
) {
}
