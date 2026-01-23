package waypoint.mvp.collection.application.dto.response;

import waypoint.mvp.place.domain.ExtractStatus;

public record ExtractionJobResponse(
	Long jobId,
	ExtractStatus status
) {
}
