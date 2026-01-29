package waypoint.mvp.collection.application.dto.response;

import waypoint.mvp.place.domain.SocialMedia;

public record SocialMediaResponse(
	String mediaType,
	String url,
	String title,
	String summary
) {
	public static SocialMediaResponse from(SocialMedia socialMedia) {
		return new SocialMediaResponse(
			socialMedia.getType().name(),
			socialMedia.getUrl(),
			socialMedia.getTitle(),
			socialMedia.getSummary()
		);
	}
}
