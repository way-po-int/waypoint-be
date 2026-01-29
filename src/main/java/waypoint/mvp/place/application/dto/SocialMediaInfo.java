package waypoint.mvp.place.application.dto;

import waypoint.mvp.place.domain.ExtractStatus;
import waypoint.mvp.place.domain.SocialMedia;

public record SocialMediaInfo(
	Long id,
	ExtractStatus status
) {
	public static SocialMediaInfo from(SocialMedia socialMedia) {
		return new SocialMediaInfo(socialMedia.getId(), socialMedia.getStatus());
	}
}
