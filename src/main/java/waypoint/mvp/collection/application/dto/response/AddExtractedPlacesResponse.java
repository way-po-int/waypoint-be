package waypoint.mvp.collection.application.dto.response;

import java.util.List;

public record AddExtractedPlacesResponse(
	int totalCount,
	int addedCount,
	int skippedCount,
	List<CollectionPlaceResponse> places
) {
	public static AddExtractedPlacesResponse of(int totalCount, List<CollectionPlaceResponse> addedPlaces) {
		return new AddExtractedPlacesResponse(
			totalCount,
			addedPlaces.size(),
			totalCount - addedPlaces.size(),
			addedPlaces
		);
	}
}
