package waypoint.mvp.place.application.dto.llm;

import waypoint.mvp.place.application.dto.content.RawContent;

public record PlaceExtractionResult(
	RawContent rawContent,
	PlaceAnalysis placeAnalysis
) {
}
