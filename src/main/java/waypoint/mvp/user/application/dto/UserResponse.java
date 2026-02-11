package waypoint.mvp.user.application.dto;

import waypoint.mvp.user.domain.Provider;
import waypoint.mvp.user.domain.User;

public record UserResponse(
	Long userId,
	Provider provider,
	String nickname,
	String picture,
	String email
) {
	public static UserResponse from(User user) {
		return new UserResponse(
			user.getId(),
			user.getSocialAccount().getProvider(),
			user.getNickname(),
			user.getPicture(),
			user.getEmail()
		);
	}
}
