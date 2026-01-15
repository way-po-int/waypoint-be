package waypoint.mvp.user.application.dto;

import waypoint.mvp.user.domain.SocialAccount;

public record SocialUserProfile(
	SocialAccount socialAccount,
	String nickname,
	String picture,
	String email
) {
	public static SocialUserProfile of(SocialAccount socialAccount, String nickname, String picture, String email) {
		return new SocialUserProfile(socialAccount, nickname, picture, email);
	}
}
