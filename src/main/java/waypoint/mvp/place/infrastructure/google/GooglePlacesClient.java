package waypoint.mvp.place.infrastructure.google;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import waypoint.mvp.place.application.dto.GooglePlaceDetailsDto;
import waypoint.mvp.place.error.SearchFailureCode;
import waypoint.mvp.place.error.exception.PlaceSearchException;

@Component
public class GooglePlacesClient {

	private static final String TEXT_SEARCH_FIELD_MASK = "places.id";
	private static final String DETAILS_FIELD_MASK = "id,displayName,formattedAddress,location,primaryType,googleMapsUri,photos.name";

	private final RestClient restClient;
	private final String textSearchPath;
	private final String detailsPath;

	public GooglePlacesClient(
		RestClient.Builder builder,
		@Value("${google.places.base-url:https://places.googleapis.com}") String baseUrl,
		@Value("${google.places.api-key}") String apiKey,
		@Value("${google.places.text-search-path}") String textSearchPath,
		@Value("${google.places.details-path}") String detailsPath
	) {
		this.restClient = builder
			.baseUrl(baseUrl)
			.defaultHeader("X-Goog-Api-Key", apiKey)
			.build();
		this.textSearchPath = textSearchPath;
		this.detailsPath = detailsPath;
	}

	public Optional<String> searchTop1PlaceId(String textQuery) {
		if (!StringUtils.hasText(textQuery)) {
			return Optional.empty();
		}

		var response = restClient.post()
			.uri(textSearchPath)
			.header("X-Goog-FieldMask", TEXT_SEARCH_FIELD_MASK)
			.body(Map.of("textQuery", textQuery, "pageSize", 1))
			.retrieve()
			.onStatus(HttpStatusCode::isError, (req, res) -> {
				throw new PlaceSearchException(SearchFailureCode.PLACES_API_ERROR);
			})
			.body(GooglePlaceIdResponse.class);

		if (ObjectUtils.isEmpty(response) || ObjectUtils.isEmpty(response.places())) {
			return Optional.empty();
		}

		var placeItem = response.places().getFirst();
		return Optional.of(placeItem.id());
	}

	public Optional<GooglePlaceDetailsDto> getPlaceDetails(String placeId) {
		var response = restClient.get()
			.uri(detailsPath, placeId)
			.header("X-Goog-FieldMask", DETAILS_FIELD_MASK)
			.retrieve()
			.onStatus(HttpStatusCode::isError, (req, res) -> {
				throw new PlaceSearchException(SearchFailureCode.PLACES_API_ERROR);
			}).body(GooglePlaceDetailsDto.class);

		if (ObjectUtils.isEmpty(response)) {
			return Optional.empty();
		}

		return Optional.of(response);
	}

	private record GooglePlaceIdResponse(
		List<PlaceItem> places
	) {

		private record PlaceItem(String id) {
		}
	}
}
