package waypoint.mvp.user.domain.event;

import waypoint.mvp.user.domain.User;

public record ProfileUpdateEvent(
	Long userId,
	String nickname,
	String picture
) {
	public static ProfileUpdateEvent from(User user) {
		return new ProfileUpdateEvent(
			user.getId(),
			user.getNickname(),
			user.getPicture()
		);
	}
}
