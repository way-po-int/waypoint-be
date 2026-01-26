package waypoint.mvp.place.application.dto;

import waypoint.mvp.place.domain.ExtractStatus;
import waypoint.mvp.place.domain.SocialMedia;

public record ExtractionJobInfo(
	Long socialMediaId,
	ExtractStatus status
) {
	public static ExtractionJobInfo from(SocialMedia socialMedia) {
		return new ExtractionJobInfo(socialMedia.getId(), socialMedia.getStatus());
	}
}
