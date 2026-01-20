package waypoint.mvp.place.application.dto;

public record PlaceDetailsResponse(
	String placeId,
	String name,
	String address,
	Double latitude,
	Double longitude,
	String primaryType,
	String primaryTypeDisplayName,
	String googleMapsUri,
	String photoName
) {
	public static PlaceDetailsResponse of(
		String placeId,
		String name,
		String address,
		Double latitude,
		Double longitude,
		String primaryType,
		String primaryTypeDisplayName,
		String googleMapsUri,
		String photoName
	) {
		return new PlaceDetailsResponse(
			placeId,
			name,
			address,
			latitude,
			longitude,
			primaryType,
			primaryTypeDisplayName,
			googleMapsUri,
			photoName
		);
	}
}
