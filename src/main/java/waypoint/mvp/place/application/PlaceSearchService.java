package waypoint.mvp.place.application;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import waypoint.mvp.place.application.dto.GooglePlaceDetailsDto;
import waypoint.mvp.place.application.dto.PlaceResponse;
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
	private static final int DEFAULT_PAGE_SIZE = 10;

	private final GooglePlacesClient googlePlacesClient;
	private final PlaceService placeService;
	private final PlacePhotoService placePhotoService;
	private final PlaceCategoryService placeCategoryService;

	public List<PlaceResponse> search(String query) {
		String q = (query == null) ? "" : query.trim();
		if (!StringUtils.hasText(q)) {
			return List.of();
		}

		List<String> googlePlaceIds = googlePlacesClient.searchPlaceIds(q, DEFAULT_PAGE_SIZE);
		if (googlePlaceIds.isEmpty()) {
			return List.of();
		}

		List<Place> places = loadOrCreatePlaces(googlePlaceIds);

		return places.stream()
			.map(place -> PlaceResponse.from(
				place,
				placeCategoryService.toCategoryResponse(place.getCategoryId()),
				placePhotoService.getCachedRepresentativePhotoUris(place)
			))
			.toList();
	}

	public Optional<String> searchTop1PlaceId(String query) {
		return googlePlacesClient.searchTop1PlaceId(query);
	}

	public Optional<Place> fetchPlaceDetails(String googlePlaceId) {
		return googlePlacesClient.getPlaceDetails(googlePlaceId)
			.map(this::mapToPlace);
	}

	private List<Place> loadOrCreatePlaces(List<String> googlePlaceIds) {
		Map<String, Place> savedPlaceMap = placeService.getPlacesByGooglePlaceIds(googlePlaceIds).stream()
			.filter(place -> place.getDetail() != null)
			.filter(place -> StringUtils.hasText(place.getDetail().getPlaceId()))
			.collect(Collectors.toMap(
				place -> place.getDetail().getPlaceId(),
				Function.identity(),
				(existing, replacement) -> existing
			));

		return googlePlaceIds.stream()
			.map(googlePlaceId -> loadOrCreatePlace(googlePlaceId, savedPlaceMap))
			.flatMap(Optional::stream)
			.toList();
	}

	private Optional<Place> loadOrCreatePlace(String googlePlaceId, Map<String, Place> savedPlaceMap) {
		Place saved = savedPlaceMap.get(googlePlaceId);
		if (saved != null) {
			return Optional.of(saved);
		}

		return fetchPlaceDetails(googlePlaceId)
			.map(placeService::createOrGetPlace);
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

		return Place.create(
			dto.getName(),
			dto.formattedAddress(),
			location,
			detail,
			placeCategoryService.getCategoryId(dto.primaryType())
		);
	}
}
