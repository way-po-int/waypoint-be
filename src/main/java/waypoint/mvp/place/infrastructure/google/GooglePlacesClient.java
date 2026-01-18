package waypoint.mvp.place.infrastructure.google;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import waypoint.mvp.place.error.PlaceError;
import waypoint.mvp.place.error.exception.PlaceException;

@Component
public class GooglePlacesClient {

	private static final String TEXT_SEARCH_FIELD_MASK = "places.id";
	private static final String DETAILS_FIELD_MASK = "id,displayName,formattedAddress,location";

	private final RestClient restClient;
	private final String apiKey;
	private final String textSearchPath;
	private final String detailsPath;

	public GooglePlacesClient(
		RestClient.Builder builder,
		@Value("${google.places.base-url:https://places.googleapis.com}") String baseUrl,
		@Value("${google.places.api-key}") String apiKey,
		@Value("${google.places.text-search-path}") String textSearchPath,
		@Value("${google.places.details-path}") String detailsPath
	) {
		this.restClient = builder.baseUrl(baseUrl).build();
		this.apiKey = apiKey;
		this.textSearchPath = textSearchPath;
		this.detailsPath = detailsPath;
	}

	/** textQuery로 Top1 placeId만 조회 */
	public String searchTop1PlaceId(String textQuery) {
		Map<String, Object> raw = restClient.post()
			.uri(textSearchPath)
			.header("X-Goog-Api-Key", apiKey)
			.header("X-Goog-FieldMask", TEXT_SEARCH_FIELD_MASK)
			.body(Map.of("textQuery", textQuery, "pageSize", 1))
			.retrieve()
			.onStatus(HttpStatusCode::isError, (req, res) -> {
				throw new PlaceException(PlaceError.PLACE_EXTERNAL_API_ERROR);
			})
			.body(new ParameterizedTypeReference<Map<String, Object>>() {
			});

		return extractTop1PlaceId(raw);
	}

	/** placeId로 장소 상세 조회 (raw Map) */
	public Map<String, Object> getPlaceDetails(String placeId) {
		return restClient.get()
			.uri(detailsPath, placeId)
			.header("X-Goog-Api-Key", apiKey)
			.header("X-Goog-FieldMask", DETAILS_FIELD_MASK)
			.retrieve()
			.onStatus(HttpStatusCode::isError, (req, res) -> {
				throw new PlaceException(PlaceError.PLACE_EXTERNAL_API_ERROR);
			})
			.body(new ParameterizedTypeReference<Map<String, Object>>() {
			});
	}

	private String extractTop1PlaceId(Map<String, Object> raw) {
		if (raw == null)
			return null;

		Object placesObj = raw.get("places");
		if (!(placesObj instanceof List<?> places) || places.isEmpty())
			return null;

		Object firstObj = places.get(0);
		if (!(firstObj instanceof Map<?, ?> first))
			return null;

		Object idObj = first.get("id");
		return (idObj instanceof String id) ? id : null;
	}
}
