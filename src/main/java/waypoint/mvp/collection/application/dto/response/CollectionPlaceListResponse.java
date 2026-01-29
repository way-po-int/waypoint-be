package waypoint.mvp.collection.application.dto.response;

import java.util.List;

public record CollectionPlaceListResponse(
	List<CollectionPlaceResponse> content,
	boolean hasNext,
	int size,
	int page
) {
	public static CollectionPlaceListResponse of(
		List<CollectionPlaceResponse> content,
		boolean hasNext,
		int size,
		int page
	) {
		return new CollectionPlaceListResponse(content, hasNext, size, page);
	}
}
