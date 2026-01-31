package waypoint.mvp.place.application.dto;

import java.util.List;

import org.locationtech.jts.geom.Point;

import waypoint.mvp.place.domain.Place;
import waypoint.mvp.place.domain.PlaceDetail;

public record PlaceResponse(
	String placeId,
	String googlePlaceId,
	String name,
	String address,
	String category,
	String googleMapsUri,
	List<String> photos,
	PointResponse point
) {
	public static PlaceResponse from(Place place, List<String> photos) {
		PlaceDetail detail = place.getDetail();
		Point location = place.getLocation();

		Double latitude = (location != null) ? location.getY() : null;
		Double longitude = (location != null) ? location.getX() : null;

		return new PlaceResponse(
			place.getId().toString(),
			(detail != null) ? detail.getPlaceId() : null,
			place.getName(),
			place.getAddress(),
			(detail != null) ? detail.getPrimaryTypeDisplayName() : null,
			(detail != null) ? detail.getGoogleMapsUri() : null,
			photos,
			PointResponse.of(latitude, longitude)
		);
	}
}
