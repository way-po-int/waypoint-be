package waypoint.mvp.user.application.dto;

import waypoint.mvp.global.util.MaskingUtils;
import waypoint.mvp.user.domain.Provider;
import waypoint.mvp.user.domain.User;

public record UserResponse(
	String userId,
	Provider provider,
	String nickname,
	String picture,
	String email
) {
	public static UserResponse from(User user) {
		return new UserResponse(
			user.getExternalId(),
			user.getSocialAccount().getProvider(),
			user.getNickname(),
			user.getPicture(),
			MaskingUtils.maskEmail(user.getEmail())
		);
	}
}
