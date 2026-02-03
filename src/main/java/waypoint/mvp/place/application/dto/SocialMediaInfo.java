package waypoint.mvp.place.application.dto;

import waypoint.mvp.place.domain.SocialMedia;
import waypoint.mvp.place.domain.SocialMediaStatus;

public record SocialMediaInfo(
	Long id,
	SocialMediaStatus status
) {
	public static SocialMediaInfo from(SocialMedia socialMedia) {
		return new SocialMediaInfo(socialMedia.getId(), socialMedia.getStatus());
	}
}
