package waypoint.mvp.place.application;

import java.util.Optional;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import waypoint.mvp.place.application.dto.GooglePlaceDetailsDto;
import waypoint.mvp.place.domain.Place;
import waypoint.mvp.place.domain.PlaceDetail;
import waypoint.mvp.place.error.SearchFailureCode;
import waypoint.mvp.place.error.exception.PlaceSearchException;
import waypoint.mvp.place.infrastructure.google.GooglePlacesClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceSearchService {

	private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

	private final GooglePlacesClient googlePlacesClient;

	public Optional<String> searchTop1PlaceId(String query) {
		return googlePlacesClient.searchTop1PlaceId(query);
	}

	public Optional<Place> fetchPlaceDetails(String googlePlaceId) {
		return googlePlacesClient.getPlaceDetails(googlePlaceId)
			.map(this::mapToPlace);
	}

	private Place mapToPlace(GooglePlaceDetailsDto dto) {
		if (!StringUtils.hasText(dto.getName()) || dto.location() == null) {
			log.warn("유효하지 않은 장소 데이터: placeId={}", dto.id());
			throw new PlaceSearchException(SearchFailureCode.PLACE_DATA_INVALID);
		}

		Point location = GEOMETRY_FACTORY.createPoint(
			new Coordinate(dto.location().longitude(), dto.location().latitude()));

		PlaceDetail detail = PlaceDetail.create(
			dto.id(),
			dto.primaryType(),
			dto.googleMapsUri(),
			dto.getFirstPhotoName()
		);

		return Place.create(dto.getName(), dto.formattedAddress(), location, detail);
	}
}
