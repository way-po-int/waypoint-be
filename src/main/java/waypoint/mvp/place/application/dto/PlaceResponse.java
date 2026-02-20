package waypoint.mvp.place.application.dto;

import java.util.List;

import org.locationtech.jts.geom.Point;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import waypoint.mvp.place.domain.ManualPlace;
import waypoint.mvp.place.domain.Place;
import waypoint.mvp.place.domain.PlaceDetail;

public record PlaceResponse(
	PlaceType placeType,
	String placeId,
	String googlePlaceId,
	String name,
	String address,
	PlaceCategoryResponse category,
	String primaryType,
	String googleMapsUri,
	List<String> photos,
	PointResponse point
) {

	@Getter
	@RequiredArgsConstructor
	public enum PlaceType {
		NORMAL,
		MANUAL;
	}

	public static PlaceResponse from(Place place, PlaceCategoryResponse category, List<String> photos) {
		PlaceDetail detail = place.getDetail();
		Point location = place.getLocation();

		Double latitude = (location != null) ? location.getY() : null;
		Double longitude = (location != null) ? location.getX() : null;

		return new PlaceResponse(
			PlaceType.NORMAL,
			place.getExternalId(),
			(detail != null) ? detail.getPlaceId() : null,
			place.getName(),
			place.getAddress(),
			category,
			(detail != null) ? detail.getPrimaryType() : null,
			(detail != null) ? detail.getGoogleMapsUri() : null,
			photos,
			PointResponse.of(latitude, longitude)
		);
	}

	public static PlaceResponse fromManual(ManualPlace manualPlace) {
		return new PlaceResponse(
			PlaceType.MANUAL,
			null,
			null,
			manualPlace.getName(),
			manualPlace.getAddress(),
			null, // TODO category 나중에 추가
			null,
			null,
			null,
			null
		);
	}
}
