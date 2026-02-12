package waypoint.mvp.user.application.dto.request;

import waypoint.mvp.global.validation.annotation.NicknamePolicy;

public record UserUpdateRequest(
	@NicknamePolicy
	String nickname
) {
}
