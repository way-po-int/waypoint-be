package waypoint.mvp.collection.application.dto.response;

import waypoint.mvp.place.domain.SocialMedia;
import waypoint.mvp.place.domain.content.ContentSnapshot;

public record SocialMediaResponse(
	String socialMediaId,
	String mediaType,
	String url,
	String authorName,
	String title,
	String summary
) {
	public static SocialMediaResponse from(SocialMedia socialMedia) {
		ContentSnapshot contentSnapshot = socialMedia.getSnapshot();

		return new SocialMediaResponse(
			socialMedia.getExternalId(),
			socialMedia.getType().name(),
			socialMedia.getUrl(),
			socialMedia.getSnapshot().getAuthorName(),
			contentSnapshot.getTitle(),
			socialMedia.getSummary()
		);
	}
}
