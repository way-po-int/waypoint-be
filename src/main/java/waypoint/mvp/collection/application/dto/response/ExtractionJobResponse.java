package waypoint.mvp.collection.application.dto.response;

import waypoint.mvp.place.domain.SocialMediaStatus;

public record ExtractionJobResponse(
	String jobId,
	SocialMediaStatus status
) {
}
