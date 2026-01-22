package waypoint.mvp.place.application.dto;

import java.util.List;

/** 쿼리별 Top1 placeId 결과 목록 */

public record PlaceIdLookupResponse(
	List<PlaceIdLookupResult> results
) {
	public static PlaceIdLookupResponse of(List<PlaceIdLookupResult> results) {
		return new PlaceIdLookupResponse(results);
	}
}
