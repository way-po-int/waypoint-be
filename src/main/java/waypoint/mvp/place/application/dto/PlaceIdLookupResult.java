package waypoint.mvp.place.application.dto;

/** 쿼리 1건에 대한 placeId 조회 결과 */

public record PlaceIdLookupResult(
	String query,
	String placeId,
	String error
) {
	public static PlaceIdLookupResult success(String query, String placeId) {
		return new PlaceIdLookupResult(query, placeId, null);
	}

	public static PlaceIdLookupResult failure(String query, String error) {
		return new PlaceIdLookupResult(query, null, error);
	}
}
